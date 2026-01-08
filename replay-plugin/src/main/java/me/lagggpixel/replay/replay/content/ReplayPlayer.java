package me.lagggpixel.replay.replay.content;

import me.lagggpixel.replay.api.replay.content.RecPlayer;
import me.lagggpixel.replay.utils.PacketUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ReplayPlayer extends RecPlayer {
    
    public ReplayPlayer(short id, UUID uuid, String name, float health) {
        super(id, uuid, EntityType.PLAYER, name, health);
    }

    @Override
    public ItemStack getSkinHead() {
        return PacketUtils.getSkull(getUuid());
    }
}
