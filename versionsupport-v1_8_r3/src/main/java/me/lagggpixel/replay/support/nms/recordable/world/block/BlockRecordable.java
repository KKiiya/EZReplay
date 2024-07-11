package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.IBlockRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.block.IBlockData;
import me.lagggpixel.replay.support.nms.utils.BlockData;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class BlockRecordable extends Recordable implements IBlockRecordable {

    private final IBlockData blockData;
    private final BlockAction actionType;
    private final boolean playSound;

    public BlockRecordable(IRecording replay, Block block, BlockAction actionType, boolean playSound) {
        super(replay);
        this.blockData = new BlockData(block);
        this.actionType = actionType;
        this.playSound = playSound;
    }

    @Override
    public IBlockData getBlockData() {
        return blockData;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        Location position = blockData.getPosition().toBukkitLocation();
        BlockPosition blockPosition = new BlockPosition(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        CraftBlockState state = new CraftBlockState(position.getBlock());
        MaterialData data = new MaterialData(blockData.getMaterial(), blockData.getData());
        state.setData(data);
        PacketPlayOutBlockChange blockChange = new PacketPlayOutBlockChange(world, blockPosition);

        v1_8_R3.sendPacket(player, blockChange);
        if (playSound) {
            PacketPlayOutNamedSoundEffect soundEffect = new PacketPlayOutNamedSoundEffect("", blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), 0, 0);
            switch (actionType) {
                case BREAK:
                    soundEffect = new PacketPlayOutNamedSoundEffect(blockData.getBreakSound(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockData.getSoundVolume(), blockData.getSoundPitch());
                    break;
                case PLACE:
                    soundEffect = new PacketPlayOutNamedSoundEffect(blockData.getPlaceSound(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), blockData.getSoundVolume(), blockData.getSoundPitch());
                    break;
            }
            v1_8_R3.sendPacket(player, soundEffect);
        }
    }

    @Override
    public World getWorld() {
        return blockData.getPosition().getWorld();
    }
}
