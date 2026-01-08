package me.lagggpixel.replay.replay.content;

import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.lagggpixel.replay.api.replay.content.RecPlayer;
import me.lagggpixel.replay.utils.PacketUtils;

public class ReplayPlayer extends RecPlayer {
    
    public ReplayPlayer(short id, UUID uuid, String name) {
        super(id, uuid, EntityType.PLAYER, name, 20.0f);
    }

    @Override
    public ItemStack getSkinHead() {
        return PacketUtils.getSkull(getUuid());
    }
}
