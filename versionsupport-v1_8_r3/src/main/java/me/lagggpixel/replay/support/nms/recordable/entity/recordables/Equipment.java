package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEquipment;
import me.lagggpixel.replay.api.replay.data.IRecording;

import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Equipment extends Recordable implements IEquipment {
    private final String UUID;

    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;
    private final ItemStack mainHand ;

    private final boolean isPlayer;

    public Equipment(IRecording replay, @NotNull LivingEntity entity) {
        super(replay);
        this.UUID = entity.getUniqueId().toString();
        this.helmet = entity.getEquipment().getHelmet();
        this.chestplate = entity.getEquipment().getChestplate();
        this.leggings = entity.getEquipment().getLeggings();
        this.boots = entity.getEquipment().getBoots();
        this.mainHand = entity.getEquipment().getItemInHand();
        this.isPlayer = entity instanceof Player;
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
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) replaySession.getSpawnedEntities().get(UUID)).getHandle();

        if (isPlayer) {
            EntityHuman human = (EntityHuman) entity;
            human.a(CraftItemStack.asNMSCopy(getMainHand()), getMainHand().getAmount());
        }
        if (getHelmet() != null) entity.setEquipment(0, CraftItemStack.asNMSCopy(getHelmet()));
        if (getChestplate() != null) entity.setEquipment(1, CraftItemStack.asNMSCopy(getChestplate()));
        if (getLeggings() != null) entity.setEquipment(2, CraftItemStack.asNMSCopy(getLeggings()));
        if (getBoots() != null)  entity.setEquipment(3, CraftItemStack.asNMSCopy(getBoots()));

        int entityId = entity.getId();
        if (getMainHand() != null) {
            PacketPlayOutEntityEquipment mainHand = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.HAND.ordinal(), CraftItemStack.asNMSCopy(getMainHand()));
            v1_8_R3.sendPacket(player, mainHand);
        }
        if (getHelmet() != null) {
            PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.HEAD.ordinal(), CraftItemStack.asNMSCopy(getHelmet()));
            v1_8_R3.sendPacket(player, helmet);
        }
        if (getChestplate() != null) {
            PacketPlayOutEntityEquipment chestplate = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.CHEST.ordinal(), CraftItemStack.asNMSCopy(getChestplate()));
            v1_8_R3.sendPacket(player, chestplate);
        }
        if (getLeggings() != null) {
            PacketPlayOutEntityEquipment leggings = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.LEGS.ordinal(), CraftItemStack.asNMSCopy(getLeggings()));
            v1_8_R3.sendPacket(player, leggings);
        }
        if (getBoots() != null) {
            PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.FEET.ordinal(), CraftItemStack.asNMSCopy(getBoots()));
            v1_8_R3.sendPacket(player, boots);
        }

    }

    @Override
    public String getUUID() {
        return UUID;
    }
}