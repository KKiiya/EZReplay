package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class BlockUpdateRecordable extends Recordable {

    private final BlockCache blockCache;
    private BlockCache previousBlock;

    public BlockUpdateRecordable(IRecording replay, BlockCache cache) {
        super(replay);
        this.blockCache = cache;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        if (previousBlock == null) {
            org.bukkit.block.Block block = replaySession.getWorld().getBlockAt(blockCache.toBukkitLocation());
            this.previousBlock = new BlockCache(replaySession.getWorld(), block.getType(), block.getData(), blockCache.toBukkitLocation());
        }
        setBlockFast(replaySession.getWorld(), blockCache);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        if (previousBlock == null) return;
        setBlockFast(replaySession.getWorld(), previousBlock);
    }

    private void setBlockFast(org.bukkit.World world, BlockCache cache) {
        World w = ((CraftWorld) world).getHandle();
        Chunk chunk = w.getChunkAt(cache.getX() >> 4, cache.getZ() >> 4);
        BlockPosition bp = new BlockPosition(cache.getX(), cache.getY(), cache.getZ());
        Block block = CraftMagicNumbers.getBlock(cache.getMaterial());
        IBlockData ibd = block.fromLegacyData(cache.getData());
        chunk.a(bp, ibd);

        w.update(bp, ibd.getBlock());
        w.notify(bp);
    }

    /*
    private short getLocation(BlockCache position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return (short)(x << 12 | z << 8 | y);
    }
     */
}
