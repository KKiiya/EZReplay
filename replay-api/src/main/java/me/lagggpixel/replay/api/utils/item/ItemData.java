package me.lagggpixel.replay.api.utils.item;

import lombok.Getter;
import me.lagggpixel.replay.api.data.Writeable;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@Getter
public class ItemData {

    @Writeable private final Material material;
    @Writeable private final byte data;
    @Writeable private final boolean enchanted;
    @Writeable private final int amount;

    public ItemData(ItemStack item) {
        if (item == null) {
            this.material = Material.AIR;
            this.data = 0;
            this.enchanted = false;
            this.amount = 0;
            return;
        }
        this.material = item.getType();
        this.data = item.getData().getData() != 0 ? item.getData().getData() : 0;
        this.enchanted = !item.getEnchantments().isEmpty();
        this.amount = item.getAmount();
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material, 1);
        item.setDurability(data);
        if (enchanted) item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        return item;
    }
}
