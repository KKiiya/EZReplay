package me.lagggpixel.replay.support.nms.recordable.entity.player.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.recordables.IEquipment;
import me.lagggpixel.replay.api.replay.data.IRecording;

import me.lagggpixel.replay.api.utils.entity.ReplayEntity;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Equipment extends Recordable implements IEquipment {
    private ItemStack helmet = null;
    private ItemStack chestplate = null;
    private ItemStack leggings = null;
    private ItemStack boots = null;
    private ItemStack mainHand = null;

    private String UUID = null;

    private final boolean isLiving;

    public Equipment(IRecording replay, @NotNull Entity entity) {
        super(replay);
        if (!(entity instanceof LivingEntity)) {
            this.isLiving = false;
            return;
        } else this.isLiving = true;

        LivingEntity livingEntity = (LivingEntity) entity;

        this.UUID = livingEntity.getUniqueId().toString();
        this.helmet = livingEntity.getEquipment().getHelmet();
        this.chestplate = livingEntity.getEquipment().getChestplate();
        this.leggings = livingEntity.getEquipment().getLeggings();
        this.boots = livingEntity.getEquipment().getBoots();
        this.mainHand = livingEntity.getEquipment().getItemInHand();
    }

    @Override
    public ItemStack getMainHand() {
        return mainHand;
    }

    @Override
    public ItemStack getOffhand() {
        return null;
    }

    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        if (!isLiving) return;
        ReplayEntity replayEntity = replaySession.getFakeEntity(getUUID());
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) replayEntity.getAssociatedEntity()).getHandle();
        if (entity instanceof EntityHuman) {
            EntityHuman human = (EntityHuman) entity;
            human.a(CraftItemStack.asNMSCopy(getMainHand()), getMainHand().getAmount());
        }
        entity.setEquipment(EquipmentSlot.HAND.ordinal(), CraftItemStack.asNMSCopy(getMainHand()));
        entity.setEquipment(EquipmentSlot.HEAD.ordinal(), CraftItemStack.asNMSCopy(getHelmet()));
        entity.setEquipment(EquipmentSlot.CHEST.ordinal(), CraftItemStack.asNMSCopy(getChestplate()));
        entity.setEquipment(EquipmentSlot.LEGS.ordinal(), CraftItemStack.asNMSCopy(getLeggings()));
        entity.setEquipment(EquipmentSlot.FEET.ordinal(), CraftItemStack.asNMSCopy(getBoots()));

        int entityId = replayEntity.getEntityId();
        PacketPlayOutEntityEquipment mainHand = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.HAND.ordinal(), CraftItemStack.asNMSCopy(getMainHand()));
        PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.HEAD.ordinal(), CraftItemStack.asNMSCopy(getHelmet()));
        PacketPlayOutEntityEquipment chestplate = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.CHEST.ordinal(), CraftItemStack.asNMSCopy(getChestplate()));
        PacketPlayOutEntityEquipment leggings = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.LEGS.ordinal(), CraftItemStack.asNMSCopy(getLeggings()));
        PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.FEET.ordinal(), CraftItemStack.asNMSCopy(getBoots()));
        v1_8_R3.sendPackets(player, mainHand, helmet, chestplate, leggings, boots);
    }

    @Override
    public String getUUID() {
        return UUID;
    }
}