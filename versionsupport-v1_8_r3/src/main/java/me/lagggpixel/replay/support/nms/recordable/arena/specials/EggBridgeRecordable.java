package me.lagggpixel.replay.support.nms.recordable.arena.specials;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IEggBridge;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.entity.Player;

public class EggBridgeRecordable extends Recordable implements IEggBridge {

    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final BlockPosition blockPosition;

    public EggBridgeRecordable(IRecording replay, Block block, Sound sound, float volume, float pitch) {
        super(replay);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(EnumParticle.FLAME, false, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), 0.55f, 0.55f, 0.55f, 0, 20, 31);
        PacketPlayOutNamedSoundEffect soundEffect = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(sound), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), volume, pitch);

        v1_8_R3.sendPackets(player, particles, soundEffect);
    }

}
