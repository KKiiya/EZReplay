package me.lagggpixel.replay.packets;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgePlayerDigging;

public class BlockDig implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent e) {
        User user = e.getUser();
        if (e.getPacketType() != PacketType.Play.Server.ACKNOWLEDGE_PLAYER_DIGGING) return;
        WrapperPlayServerAcknowledgePlayerDigging packet = new WrapperPlayServerAcknowledgePlayerDigging(e);
        DiggingAction action = packet.getAction();

        switch ()
    }
}
