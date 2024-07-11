package me.lagggpixel.replay.support.nms.packets;

import me.lagggpixel.replay.api.packets.PacketPlayInEvent;
import me.lagggpixel.replay.api.utils.block.AbstractBlockBreaker;
import me.lagggpixel.replay.support.nms.utils.block.BlockBreaker;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible for handling packets.
 *
 * @author Kiiya
 * @since May 07, 2024
 */
public class PacketListener implements Listener {

    private final HashMap<Player, AbstractBlockBreaker> blockBreakers;

    public PacketListener() {
        this.blockBreakers = new HashMap<>();
    }

    @EventHandler
    public void onPacketPlayOut(PacketPlayInEvent event) {
        Object pac = event.getPacket();
        Player p = event.getPlayer();
        World world = ((CraftWorld) p).getHandle();
        if (!(pac instanceof PacketPlayInBlockDig)) return;
        PacketPlayInBlockDig packet = (PacketPlayInBlockDig) event.getPacket();

        BlockPosition position = packet.a();
        Block block = world.c(position);

        if (packet.c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
            Location loc = new Location(p.getWorld(), position.getX(), position.getY(), position.getZ());
            blockBreakers.put(p, new BlockBreaker(p, loc));
        } else if (packet.c() == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {

        }
    }
}
