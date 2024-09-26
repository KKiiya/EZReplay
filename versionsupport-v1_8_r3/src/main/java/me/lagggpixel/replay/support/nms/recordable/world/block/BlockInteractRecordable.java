package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.IBlockRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class BlockInteractRecordable extends Recordable implements IBlockRecordable {

    private static final HashMap<org.bukkit.Chunk, List<BlockPosition>> positionsToUpdate = new HashMap<>();
    private final World world;
    private final org.bukkit.Material material;
    private final org.bukkit.Chunk chunk;
    private final byte data;
    private final BlockPosition blockPosition;
    private final BlockAction actionType;
    private final boolean playSound;

    public BlockInteractRecordable(IRecording replay, World world, org.bukkit.Material material, byte data, Location location, BlockAction actionType, boolean playSound) {
        super(replay);
        this.world = world;
        this.material = material;
        this.chunk = location.getChunk();
        positionsToUpdate.putIfAbsent(chunk, new ArrayList<>());
        this.data = data;
        this.blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.actionType = actionType;
        this.playSound = playSound;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        IBlockData blockData = nmsBlock.fromLegacyData(data);
        Block.StepSound stepSounds = nmsBlock.stepSound;

        if (actionType == BlockAction.INTERACT) {
            PacketPlayOutBlockAction blockAction = new PacketPlayOutBlockAction(blockPosition, nmsBlock, 1, 1);
            v1_8_R3.sendPackets(player, blockAction);
        } else if (actionType == BlockAction.PLACE || actionType == BlockAction.BREAK){
            positionsToUpdate.get(world.getChunkAt(blockPosition.getX(), blockPosition.getZ())).add(blockPosition);
            if (playSound) {
                PacketPlayOutNamedSoundEffect soundEffect = new PacketPlayOutNamedSoundEffect("", blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), 0, 0);
                switch (actionType) {
                    case BREAK:
                        soundEffect = new PacketPlayOutNamedSoundEffect(stepSounds.getBreakSound(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), stepSounds.getVolume1(), (float) (stepSounds.getVolume2()/1.15));
                        break;
                    case PLACE:
                        soundEffect = new PacketPlayOutNamedSoundEffect(stepSounds.getPlaceSound(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), stepSounds.getVolume1(), (float) (stepSounds.getVolume2()/1.15));
                        break;
                }
                v1_8_R3.sendPacket(player, soundEffect);
            }
        } else if (actionType == BlockAction.UPDATE) {
            short[] locations = new short[positionsToUpdate.size()];
            for (int i = 0; i < positionsToUpdate.size(); i++) {
                locations[i] =
            }


            for (org.bukkit.Chunk chunk : positionsToUpdate.keySet()) {
                Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
                PacketPlayOutMultiBlockChange multiBlockChange = new PacketPlayOutMultiBlockChange();
                ChunkCoordIntPair intPair = new ChunkCoordIntPair(nmsChunk.locX, nmsChunk.locZ);
                short position = getLocation(blockPosition);
                PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] infos = new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo(position, i);
                List<BlockPosition> blockPositions = positionsToUpdate.get(chunk);
                v1_8_R3.sendPacket(player, multiBlockChange);
            }

            positionsToUpdate.clear();
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    private short getLocation(BlockPosition position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return (short)(x << 12 | z << 8 | y);
    }
}
