package me.lagggpixel.replay.support.nms.recordable.world;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutExplosion;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ExplosionRecordable extends Recordable {

    private final double x;
    private final double y;
    private final double z;
    private final float strength;

    public ExplosionRecordable(IRecording replay, Location location, float radius) {
        super(replay);
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.strength = radius;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        PacketPlayOutExplosion explosion = new PacketPlayOutExplosion(x, y, z, strength, new ArrayList<>(), new Vec3D(0, 0, 0));
        PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(Sound.EXPLODE), x, y, z, 1.0f, 0.8f);
        v1_8_R3.sendPackets(player, explosion, sound);
    }
}
