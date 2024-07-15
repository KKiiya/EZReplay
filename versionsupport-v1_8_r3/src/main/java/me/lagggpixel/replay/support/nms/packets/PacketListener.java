package me.lagggpixel.replay.support.nms.packets;

import com.tomkeuper.bedwars.api.arena.IArena;
import me.lagggpixel.replay.api.packets.PacketPlayInEvent;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.block.AbstractBlockBreaker;
import me.lagggpixel.replay.api.utils.block.DigType;
import me.lagggpixel.replay.support.nms.recordable.world.block.BlockDigRecordable;
import me.lagggpixel.replay.support.nms.utils.block.BlockBreaker;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

/**
 * This class is responsible for handling packets.
 *
 * @author Kiiya
 * @since May 07, 2024
 */
public class PacketListener implements Listener {

    private final HashMap<Block, AbstractBlockBreaker> blocksBreaking;

    public PacketListener() {
        this.blocksBreaking = new HashMap<>();
    }

    @EventHandler
    public void onPacketPlayOut(PacketPlayInEvent event) {
        Object pac = event.getPacket();
        Player p = event.getPlayer();
        World world = ((CraftWorld) p).getHandle();
        if (!(pac instanceof PacketPlayInBlockDig)) return;
        PacketPlayInBlockDig packet = (PacketPlayInBlockDig) event.getPacket();

        IArena a = v1_8_R3.getInstance().getPlugin().getBedWarsAPI().getArenaUtil().getArenaByPlayer(p);
        if (a == null) return;

        IRecording recording = v1_8_R3.getInstance().getPlugin().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        BlockPosition position = packet.a();
        Block block = world.c(position);

        if (packet.c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
            Location loc = new Location(p.getWorld(), position.getX(), position.getY(), position.getZ());
            AbstractBlockBreaker blockBreaker = new BlockBreaker(p, loc);
            recording.getLastFrame().addRecordable(new BlockDigRecordable(recording, blockBreaker, DigType.DIG_START));
            blocksBreaking.put(block, blockBreaker);
        } else if (packet.c() == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK || packet.c() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {

        }
    }
}
