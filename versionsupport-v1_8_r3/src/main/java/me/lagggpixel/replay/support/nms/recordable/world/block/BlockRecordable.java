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
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class BlockRecordable extends Recordable implements IBlockRecordable {

    private final World world;
    private final org.bukkit.Material material;
    private final byte data;
    private final BlockPosition blockPosition;
    private final BlockAction actionType;
    private final boolean playSound;

    public BlockRecordable(IRecording replay, World world, org.bukkit.Material material, byte data, Location location, BlockAction actionType, boolean playSound) {
        super(replay);
        this.world = world;
        this.material = material;
        this.data = data;
        this.blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.actionType = actionType;
        this.playSound = playSound;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Block nmsBlock = CraftMagicNumbers.getBlock(material);
        net.minecraft.server.v1_8_R3.Block.StepSound stepSounds = nmsBlock.stepSound;

        PacketPlayOutBlockChange blockChange = new PacketPlayOutBlockChange(world, blockPosition);
        blockChange.block = nmsBlock.fromLegacyData(data);

        v1_8_R3.sendPacket(player, blockChange);
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
    }

    @Override
    public World getWorld() {
        return world;
    }
}
