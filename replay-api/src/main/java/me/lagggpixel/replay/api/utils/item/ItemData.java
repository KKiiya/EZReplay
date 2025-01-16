package me.lagggpixel.replay.api.utils.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemData {

    private final Material material;
    private final byte data;
    private final boolean enchanted;

    public ItemData(ItemStack item) {
        if (item == null) {
            this.material = Material.AIR;
            this.data = 0;
            this.enchanted = false;
            return;
        }
        this.material = item.getType();
        this.data = item.getData().getData() != 0 ? item.getData().getData() : 0;
        this.enchanted = !item.getEnchantments().isEmpty();
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material, 1);
        item.setDurability(data);
        if (enchanted) item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        return item;
    }
}
