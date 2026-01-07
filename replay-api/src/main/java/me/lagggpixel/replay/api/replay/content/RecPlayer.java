package me.lagggpixel.replay.api.replay.content;

import org.bukkit.inventory.ItemStack;

public abstract class RecPlayer extends RecEntity {
    
    public RecPlayer(short id, java.util.UUID uuid, org.bukkit.entity.EntityType type, String name, float health) {
        super(id, uuid, type, name, health);
    }

    public abstract ItemStack getSkinHead();
}
