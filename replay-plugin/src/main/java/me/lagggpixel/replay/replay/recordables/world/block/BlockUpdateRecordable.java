package me.lagggpixel.replay.replay.recordables.world.block;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class BlockUpdateRecordable extends Recordable {

    private static final int MAX_BLOCKS_PER_PACKET = 4096; // PacketEvents limit
    private static final int BATCH_SIZE = 512; // Optimal batch size for smooth updates
    
    @Writeable private final List<BlockCache> newBlocks;
    private List<BlockCache> oldBlocks;
    
    // Cache for StateType lookups to improve performance
    private static final Map<String, StateType> STATE_TYPE_CACHE = new HashMap<>();

    public BlockUpdateRecordable(ReplayByteBuffer reader) {
        super(null);
        int size = reader.read(VAR_INT);
        this.newBlocks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Material material = Material.values()[reader.read(INT)];
            byte data = reader.read(BYTE);
            int x = reader.read(INT);
            int y = reader.read(INT);
            int z = reader.read(INT);
            this.newBlocks.add(new BlockCache(material, data, x, y, z));
        }
        this.oldBlocks = null;
    }

    public BlockUpdateRecordable(IRecording replay, List<BlockCache> newBlocks) {
        super(replay);
        this.newBlocks = newBlocks;
        this.oldBlocks = null;
    }

    @Override
    public void play(IReplaySession replaySession) {
        if (oldBlocks == null) {
            oldBlocks = new ArrayList<>(newBlocks.size());
            for (BlockCache cache : newBlocks) {
                Block block = replaySession.getWorld().getBlockAt(cache.getX(), cache.getY(), cache.getZ());
                oldBlocks.add(new BlockCache(block.getType(), block.getData(), block.getLocation()));
            }
        }

        updateBlocks(replaySession.getViewers(), newBlocks, replaySession.getWorld());
    }

    @Override
    public void unplay(IReplaySession replaySession) {
        if (oldBlocks == null) return;
        updateBlocks(replaySession.getViewers(), oldBlocks, replaySession.getWorld());
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BLOCK_UPDATE;
    }

    private void updateBlocks(Iterable<Player> players, List<BlockCache> caches, World world) {
        if (caches == null || caches.isEmpty()) return;

        // Group blocks by chunk coordinates (not chunk sections)
        Map<Long, List<BlockCache>> chunkMap = new HashMap<>();
        for (BlockCache cache : caches) {
            // Pack chunk coordinates into a long for efficient map key
            long chunkKey = packChunkCords(cache.getX() >> 4, cache.getZ() >> 4);
            chunkMap.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(cache);
        }

        // Process each chunk's blocks
        for (Map.Entry<Long, List<BlockCache>> entry : chunkMap.entrySet()) {
            long chunkKey = entry.getKey();
            int chunkX = unpackChunkX(chunkKey);
            int chunkZ = unpackChunkZ(chunkKey);
            List<BlockCache> chunkCaches = entry.getValue();

            // Update actual world blocks (non-blocking, without physics updates)
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            setBlocksFast(chunk, chunkCaches);
            
            // Send updates to players in batches for smooth rendering
            sendBlockUpdates(players, chunkX, chunkZ, chunkCaches);
        }
    }

    /**
     * Sends block updates to players in optimized batches
     * Splits large updates into smaller packets for smooth client-side rendering
     */
    private void sendBlockUpdates(Iterable<Player> players, int chunkX, int chunkZ, List<BlockCache> caches) {
        // Split into batches if needed
        for (int batchStart = 0; batchStart < caches.size(); batchStart += BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + BATCH_SIZE, caches.size());
            List<BlockCache> batch = caches.subList(batchStart, batchEnd);
            
            // Create encoded blocks with cached StateType lookups
            WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = createEncodedBlocks(batch);
            
            if (encodedBlocks.length == 0) continue;
            
            // Create packet with chunk position
            Vector3i chunkPos = new Vector3i(chunkX, 0, chunkZ);
            WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(chunkPos, false, encodedBlocks);

            // Send to all players
            for (Player player : players) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                user.sendPacket(packet);
            }
        }
    }

    /**
     * Creates encoded block array with cached StateType lookups for performance
     */
    private WrapperPlayServerMultiBlockChange.EncodedBlock[] createEncodedBlocks(List<BlockCache> caches) {
        List<WrapperPlayServerMultiBlockChange.EncodedBlock> encodedList = new ArrayList<>(caches.size());
        
        for (BlockCache cache : caches) {
            try {
                // Get StateType with caching for performance
                String materialKey = "minecraft:" + cache.getMaterial().name().toLowerCase();
                StateType stateType = STATE_TYPE_CACHE.computeIfAbsent(materialKey, StateTypes::getByName);
                
                if (stateType == null) continue; // Skip invalid blocks
                
                // Get wrapped block state
                WrappedBlockState blockState = WrappedBlockState.getDefaultState(stateType);
                
                // Calculate chunk-relative coordinates using bit masking (faster than modulo)
                int relativeX = cache.getX() & 0xF;  // x % 16
                int relativeY = cache.getY();
                int relativeZ = cache.getZ() & 0xF;  // z % 16
                
                // Create encoded block
                WrapperPlayServerMultiBlockChange.EncodedBlock encodedBlock = new WrapperPlayServerMultiBlockChange.EncodedBlock(blockState, relativeX, relativeY, relativeZ);
                encodedList.add(encodedBlock);
                
            } catch (Exception e) {
                // Skip problematic blocks instead of failing entire update
                continue;
            }
        }
        
        return encodedList.toArray(new WrapperPlayServerMultiBlockChange.EncodedBlock[0]);
    }

    /**
     * Fast block updates without physics or lighting recalculation
     * Uses direct chunk access for optimal performance
     */
    private void setBlocksFast(Chunk chunk, List<BlockCache> caches) {
        for (BlockCache cache : caches) {
            try {
                // Get relative coordinates within chunk
                int localX = cache.getX() & 0xF;
                int localY = cache.getY();
                int localZ = cache.getZ() & 0xF;
                
                // Update block without physics (false parameter)
                Block block = chunk.getBlock(localX, localY, localZ);
                block.setType(cache.getMaterial(), false);
                
            } catch (Exception e) {
                // Skip problematic blocks
                continue;
            }
        }
    }

    /**
     * Packs chunk coordinates into a single long for efficient map keys
     * Format: upper 32 bits = X, lower 32 bits = Z
     */
    private long packChunkCords(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    /**
     * Unpacks X coordinate from packed chunk key
     */
    private int unpackChunkX(long packed) {
        return (int) (packed >> 32);
    }

    /**
     * Unpacks Z coordinate from packed chunk key
     */
    private int unpackChunkZ(long packed) {
        return (int) packed;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(VAR_INT, newBlocks.size());
        for (BlockCache cache : newBlocks) {
            writer.write(INT, cache.getMaterial().ordinal());
            writer.write(BYTE, cache.getData());
            writer.write(INT, cache.getX());
            writer.write(INT, cache.getY());
            writer.write(INT, cache.getZ());
        }
    }
}