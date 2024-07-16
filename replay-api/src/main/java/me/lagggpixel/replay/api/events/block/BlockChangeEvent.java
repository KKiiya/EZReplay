package me.lagggpixel.replay.api.events.block;

import me.lagggpixel.replay.api.utils.block.BlockEventType;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Block block;
    private final BlockEventType eventType;
    private final Entity player;
    private boolean isCancelled = false;

    public BlockChangeEvent(@Nullable Entity player, Block block, BlockEventType eventType) {
        this.block = block;
        this.player = player;
        this.eventType = eventType;
    }

    public Block getBlock() {
        return block;
    }

    public BlockEventType getEventType() {
        return eventType;
    }

    public Entity getEntity() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
