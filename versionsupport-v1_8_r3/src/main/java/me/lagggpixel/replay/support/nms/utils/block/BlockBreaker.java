package me.lagggpixel.replay.support.nms.utils.block;

import me.lagggpixel.replay.api.utils.block.AbstractBlockBreaker;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

public class BlockBreaker extends AbstractBlockBreaker {

    public BlockBreaker(org.bukkit.entity.Entity entity, Location targetBlock) {
        super(entity, targetBlock);
    }

    private ItemStack getCurrentItem() {
        return getHandle() instanceof EntityLiving ? ((EntityLiving) getHandle()).getEquipment(0) : null;
    }

    protected Entity getHandle() {
        return ((CraftEntity) getEntity()).getHandle();
    }

    @Override
    public float getDamage(int tickDifference) {
        return getStrength(getHandle().world.getType(new BlockPosition(x, y, z)).getBlock()) * (tickDifference + 1);
    }

    @Override
    public void setBlockDamage(int entityId ,int damage, Player player) {
        PacketPlayOutBlockBreakAnimation animation = new PacketPlayOutBlockBreakAnimation(entityId, new BlockPosition(x, y, z), damage);
        v1_8_R3.sendPacket(player, animation);
    }

    private float getStrength(Block block) {
        float base = block.g(null, new BlockPosition(0, 0, 0));
        return base < 0.0F ? 0.0F : !isDestroyable(block) ? 1.0F / base / 100.0F : strengthMod(block) / base / 30.0F;
    }

    private boolean isDestroyable(Block block) {
        if (block.getMaterial().isAlwaysDestroyable())
            return true;
        else {
            ItemStack current = getCurrentItem();
            return current != null && current.b(block);
        }
    }

    private float strengthMod(Block block) {
        ItemStack itemstack = getCurrentItem();
        float f = itemstack.a(block);
        if (getHandle() instanceof EntityLiving) {
            EntityLiving handle = (EntityLiving) getHandle();
            if (f > 1.0F) {
                int i = EnchantmentManager.getDigSpeedEnchantmentLevel(handle);
                if (i > 0) {
                    f += i * i + 1;
                }
            }
            if (handle.hasEffect(MobEffectList.FASTER_DIG)) {
                f *= 1.0F + (handle.getEffect(MobEffectList.FASTER_DIG).getAmplifier() + 1) * 0.2F;
            }
            if (handle.hasEffect(MobEffectList.SLOWER_DIG)) {
                float f1;
                switch (handle.getEffect(MobEffectList.SLOWER_DIG).getAmplifier()) {
                    case 0:
                        f1 = 0.3F;
                        break;
                    case 1:
                        f1 = 0.09F;
                        break;
                    case 2:
                        f1 = 0.0027F;
                        break;
                    case 3:
                    default:
                        f1 = 8.1E-4F;
                }
                f *= f1;
            }
            if (handle.a(Material.WATER) && !EnchantmentManager.j(handle)) {
                f /= 5.0F;
            }
        }
        if (!getHandle().onGround) {
            f /= 5.0F;
        }
        return f;
    }
}
