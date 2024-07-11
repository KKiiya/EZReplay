package me.lagggpixel.replay.api.packets;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PacketPlayInEvent extends Event implements Cancellable {
    public static final HandlerList handlers = new HandlerList();

    @Getter
    private final Object packet;
    @Getter
    private final Player player;
    private boolean cancelled = false;

    public PacketPlayInEvent(Object packet, Player player) {
        this.packet = packet;
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
