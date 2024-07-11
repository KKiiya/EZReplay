package me.lagggpixel.replay.api.replay.data.recordable.entity.player.recordables;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface IEquipment {

    ItemStack getMainHand();

    ItemStack getOffhand();

    ItemStack getHelmet();

    ItemStack getChestplate();

    ItemStack getLeggings();

    ItemStack getBoots();

    String getUUID();
}
