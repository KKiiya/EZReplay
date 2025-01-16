package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.IBlockRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class BlockInteractRecordable extends Recordable implements IBlockRecordable {

    private final World world;
    private final org.bukkit.Material material;
    private final BlockPosition blockPosition;
    private final BlockAction actionType;
    private final boolean playSound;

    public BlockInteractRecordable(IRecording replay, BlockCache cache, BlockAction actionType, boolean playSound) {
        super(replay);
        this.world = cache.getWorld();
        this.material = cache.getMaterial();
        this.blockPosition = new BlockPosition(cache.getX(), cache.getY(), cache.getZ());
        this.actionType = actionType;
        this.playSound = playSound;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        Block.StepSound stepSounds = nmsBlock.stepSound;

        if (actionType == BlockAction.INTERACT) {
            PacketPlayOutBlockAction blockAction = new PacketPlayOutBlockAction(blockPosition, nmsBlock, 1, 1);
            v1_8_R3.sendPackets(player, blockAction);
        } else if (actionType == BlockAction.PLACE || actionType == BlockAction.BREAK){
            if (playSound) {
                PacketPlayOutNamedSoundEffect soundEffect = new PacketPlayOutNamedSoundEffect("", blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), 0, 0);
                switch (actionType) {
                    case BREAK:
                        soundEffect = new PacketPlayOutNamedSoundEffect(stepSounds.getBreakSound(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), stepSounds.getVolume1(), (float) (stepSounds.getVolume2()/1.3));
                        break;
                    case PLACE:
                        soundEffect = new PacketPlayOutNamedSoundEffect(stepSounds.getPlaceSound(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), stepSounds.getVolume1(), (float) (stepSounds.getVolume2()/1.3));
                        break;
                }
                v1_8_R3.sendPacket(player, soundEffect);
            }
        }
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public World getWorld() {
        return world;
    }
}
