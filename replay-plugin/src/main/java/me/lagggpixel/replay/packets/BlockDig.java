package me.lagggpixel.replay.packets;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.replay.recordables.world.block.BlockDigRecordable;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BlockDig implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        if (e.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
        WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(e);

        Player p = e.getPlayer();
        World world = p.getWorld();

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;
        if (!recording.isRecording()) return;

        Vector3i packetPos = packet.getBlockPosition();
        Vector3d pos = new Vector3d(packetPos.getX(), packetPos.getY(), packetPos.getZ());
        Recordable recordable = new BlockDigRecordable(recording, p, pos.toVector3i(), packet.getSequence());
        recording.getLastFrame().addRecordable(recordable);
    }
}
