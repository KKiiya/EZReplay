package me.lagggpixel.replay.support.nms.recordable.entity.entity;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEquipment;
import me.lagggpixel.replay.api.replay.data.IRecording;

import me.lagggpixel.replay.api.utils.item.ItemData;
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

import java.util.UUID;

public class Equipment extends Recordable implements IEquipment {

    @Writeable
    private final ItemData[] equipment = new ItemData[5];
    @Writeable
    private final UUID UUID;
    @Writeable
    private final boolean isPlayer;

    public Equipment(IRecording replay, @NotNull LivingEntity entity) {
        super(replay);
        this.UUID = entity.getUniqueId();
        this.equipment[0] = new ItemData(entity.getEquipment().getHelmet());
        this.equipment[1] = new ItemData(entity.getEquipment().getChestplate());
        this.equipment[2] = new ItemData(entity.getEquipment().getLeggings());
        this.equipment[3] = new ItemData(entity.getEquipment().getBoots());
        this.equipment[4] = new ItemData(entity.getEquipment().getItemInHand());
        this.isPlayer = entity instanceof Player;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) replaySession.getSpawnedEntities().get(UUID.toString())).getHandle();

        ItemStack mainHand = equipment[4].toItemStack();
        ItemStack helmet = equipment[0].toItemStack();
        ItemStack chestplate = equipment[1].toItemStack();
        ItemStack leggings = equipment[2].toItemStack();
        ItemStack boots = equipment[3].toItemStack();

        if (isPlayer) {
            EntityHuman human = (EntityHuman) entity;
            human.a(CraftItemStack.asNMSCopy(mainHand), 1);
        }
        if (helmet != null) entity.setEquipment(0, CraftItemStack.asNMSCopy(helmet));
        if (chestplate != null) entity.setEquipment(1, CraftItemStack.asNMSCopy(chestplate));
        if (leggings != null) entity.setEquipment(2, CraftItemStack.asNMSCopy(leggings));
        if (boots != null)  entity.setEquipment(3, CraftItemStack.asNMSCopy(boots));

        int entityId = entity.getId();
        if (mainHand != null) {
            PacketPlayOutEntityEquipment mainHandPacket = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.HAND.ordinal(), CraftItemStack.asNMSCopy(mainHand));
            v1_8_R3.sendPacket(player, mainHandPacket);
        }
        if (helmet != null) {
            PacketPlayOutEntityEquipment helmetPacket = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.HEAD.ordinal(), CraftItemStack.asNMSCopy(helmet));
            v1_8_R3.sendPacket(player, helmetPacket);
        }
        if (chestplate != null) {
            PacketPlayOutEntityEquipment chestplatePacket = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.CHEST.ordinal(), CraftItemStack.asNMSCopy(chestplate));
            v1_8_R3.sendPacket(player, chestplatePacket);
        }
        if (leggings != null) {
            PacketPlayOutEntityEquipment leggingsPacket = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.LEGS.ordinal(), CraftItemStack.asNMSCopy(leggings));
            v1_8_R3.sendPacket(player, leggingsPacket);
        }
        if (boots != null) {
            PacketPlayOutEntityEquipment bootsPacket = new PacketPlayOutEntityEquipment(entityId, EquipmentSlot.FEET.ordinal(), CraftItemStack.asNMSCopy(boots));
            v1_8_R3.sendPacket(player, bootsPacket);
        }

    }


    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public UUID getUUID() {
        return UUID;
    }
}