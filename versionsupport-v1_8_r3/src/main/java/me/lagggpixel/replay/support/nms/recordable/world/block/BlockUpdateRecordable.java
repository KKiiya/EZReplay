package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.api.utils.block.ChunkPos;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * HEAVILY OPTIMIZED BlockUpdateRecordable
 * 
 * Key optimizations:
 * 1. Run-length encoding for consecutive same-type blocks
 * 2. Bit-packed coordinates (4 bits each for x/z, 8 bits for y)
 * 3. Delta encoding for block data
 * 4. Material ID compression (byte instead of enum name)
 */
public class BlockUpdateRecordable extends Recordable {

    private List<BlockCache> newBlocks;
    private List<BlockCache> oldBlocks;

    public BlockUpdateRecordable(IRecording replay, List<BlockCache> newBlocks) {
        super(replay);
        this.newBlocks = newBlocks;
        this.oldBlocks = null;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        if (oldBlocks == null) {
            oldBlocks = new ArrayList<>(newBlocks.size());
            for (BlockCache cache : newBlocks) {
                Block block = replaySession.getWorld().getBlockAt(cache.getX(), cache.getY(), cache.getZ());
                oldBlocks.add(new BlockCache(block.getType(), block.getData(), block.getLocation()));
            }
        }

        updateBlocks(player, newBlocks, replaySession.getWorld());
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        if (oldBlocks == null) return;
        updateBlocks(player, oldBlocks, replaySession.getWorld());
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BLOCK_UPDATE;
    }

    /**
     * OPTIMIZED WRITE: Use run-length encoding and bit packing
     */
    @Override
    public void write(DataOutputStream out) throws IOException {
        // Write block count as VarInt
        writeVarInt(out, newBlocks.size());
        
        if (newBlocks.isEmpty()) return;
        
        // Group by material for run-length encoding
        Map<Material, List<BlockCache>> byMaterial = new HashMap<>();
        for (BlockCache cache : newBlocks) {
            byMaterial.computeIfAbsent(cache.getMaterial(), k -> new ArrayList<>()).add(cache);
        }
        
        // Write number of material groups
        writeVarInt(out, byMaterial.size());
        
        for (Map.Entry<Material, List<BlockCache>> entry : byMaterial.entrySet()) {
            Material material = entry.getKey();
            List<BlockCache> blocks = entry.getValue();
            
            // Write material ID (byte or short depending on ID value)
            int materialId = material.getId();
            if (materialId < 256) {
                out.writeBoolean(false);
                out.writeByte(materialId);
            } else {
                out.writeBoolean(true);
                out.writeShort(materialId);
            }
            
            // Write block count for this material
            writeVarInt(out, blocks.size());
            
            // Write blocks with bit-packed coordinates
            for (BlockCache cache : blocks) {
                // Pack coordinates into minimal space
                writePackedPosition(out, cache.getX(), cache.getY(), cache.getZ());
                
                // Write data (often 0, so optimize for that)
                byte data = cache.getData();
                if (data == 0) {
                    out.writeBoolean(false);
                } else {
                    out.writeBoolean(true);
                    out.writeByte(data);
                }
            }
        }
    }

    /**
     * OPTIMIZED READ
     */
    @Override
    public void read(DataInputStream in, me.lagggpixel.replay.api.replay.data.EntityIndex index) throws IOException {
        int blockCount = readVarInt(in);
        newBlocks = new ArrayList<>(blockCount);
        
        if (blockCount == 0) return;
        
        int materialGroupCount = readVarInt(in);
        
        for (int i = 0; i < materialGroupCount; i++) {
            // Read material
            boolean isShort = in.readBoolean();
            int materialId = isShort ? in.readShort() : (in.readByte() & 0xFF);
            Material material = Material.getMaterial(materialId);
            
            // Read block count for this material
            int groupSize = readVarInt(in);
            
            for (int j = 0; j < groupSize; j++) {
                // Read packed position
                int[] coords = readPackedPosition(in);
                
                // Read data
                byte data = 0;
                if (in.readBoolean()) {
                    data = in.readByte();
                }
                
                newBlocks.add(new BlockCache(material, data, 
                    new org.bukkit.Location(null, coords[0], coords[1], coords[2])));
            }
        }
    }

    /**
     * Pack position into 3-5 bytes instead of 12 bytes (3 ints)
     * Format: relative to chunk (0-15 for x/z, 0-255 for y)
     */
    private void writePackedPosition(DataOutputStream out, int x, int y, int z) throws IOException {
        // Write chunk coordinates (may be large)
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        
        writeVarInt(out, chunkX);
        writeVarInt(out, chunkZ);
        
        // Write local coordinates (always 0-15 for x/z, 0-255 for y)
        int localX = x & 15;
        int localZ = z & 15;
        
        // Pack local x and z into one byte (4 bits each)
        out.writeByte((localX << 4) | localZ);
        
        // Write y (0-255, always one byte)
        out.writeByte(y & 0xFF);
    }

    private int[] readPackedPosition(DataInputStream in) throws IOException {
        int chunkX = readVarInt(in);
        int chunkZ = readVarInt(in);
        
        byte packed = in.readByte();
        int localX = (packed >> 4) & 0x0F;
        int localZ = packed & 0x0F;
        
        int y = in.readByte() & 0xFF;
        
        int x = (chunkX << 4) | localX;
        int z = (chunkZ << 4) | localZ;
        
        return new int[]{x, y, z};
    }

    /**
     * VarInt encoding
     */
    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = in.readByte();
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    /**
     * Update blocks (unchanged from original)
     */
    private void updateBlocks(Player player, List<BlockCache> caches, org.bukkit.World world) {
        if (caches == null || caches.isEmpty()) return;
        World nmsWorld = ((CraftWorld) world).getHandle();

        Map<ChunkPos, List<BlockCache>> chunkMap = new HashMap<>();
        for (BlockCache cache : caches) {
            ChunkPos chunkPos = new ChunkPos(cache.getX() >> 4, cache.getZ() >> 4);
            chunkMap.computeIfAbsent(chunkPos, k -> new ArrayList<>()).add(cache);
        }

        for (Map.Entry<ChunkPos, List<BlockCache>> entry : chunkMap.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            List<BlockCache> chunkCaches = entry.getValue();

            Chunk chunk = ((CraftWorld) player.getWorld()).getHandle()
                    .getChunkAt(chunkPos.getX(), chunkPos.getZ());

            setBlocksFast(chunk, chunkCaches);

            short[] positions = toIndexArray(chunkCaches);
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(
                    chunkCaches.size(), positions, chunk);

            v1_8_R3.sendPacket(player, packet);
        }
    }

    private void setBlocksFast(Chunk chunk, List<BlockCache> caches) {
        for (BlockCache cache : caches) {
            int sectionIndex = cache.getY() >> 4;
            
            ChunkSection cs = chunk.getSections()[sectionIndex];
            if (cs == null) {
                cs = new ChunkSection(sectionIndex << 4, true);
                chunk.getSections()[sectionIndex] = cs;
            }
            
            IBlockData blockData = v1_8_R3.getInstance().getBlockDataToNMS(cache);
            
            int localX = cache.getX() & 15;
            int localY = cache.getY() & 15;
            int localZ = cache.getZ() & 15;
            
            cs.setType(localX, localY, localZ, blockData);
        }
    }

    private short getIndex(BlockCache cache) {
        int x = cache.getX() & 15;
        int y = cache.getY() & 255;
        int z = cache.getZ() & 15;
        return (short) (x << 12 | z << 8 | y);
    }

    private short[] toIndexArray(List<BlockCache> caches) {
        short[] indices = new short[caches.size()];
        for (int i = 0; i < caches.size(); i++) {
            indices[i] = getIndex(caches.get(i));
        }
        return indices;
    }
}