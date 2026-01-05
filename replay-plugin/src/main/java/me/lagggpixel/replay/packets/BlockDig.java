package me.lagggpixel.replay.packets;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgePlayerDigging;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.replay.recordables.world.block.BlockDigRecordable;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BlockDig implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent e) {
        User user = e.getUser();
        if (e.getPacketType() != PacketType.Play.Server.ACKNOWLEDGE_PLAYER_DIGGING) return;
        WrapperPlayServerAcknowledgePlayerDigging packet = new WrapperPlayServerAcknowledgePlayerDigging(e);
        DiggingAction action = packet.getAction();
        Player p = e.getPlayer();
        World world = p.getWorld();
        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;
        if (!recording.isRecording()) return;
        if (action != DiggingAction.CANCELLED_DIGGING && action != DiggingAction.FINISHED_DIGGING && action != DiggingAction.START_DIGGING) return;
        Recordable recordable = new BlockDigRecordable(recording, p, , action);

    }
}
