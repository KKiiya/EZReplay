package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.item.ItemData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Equipment extends Recordable {

    @Writeable private final ItemData[] equipment = new ItemData[5];
    @Writeable private final short entityId;
    @Writeable private final boolean isPlayer;

    public Equipment(IRecording replay, @NotNull LivingEntity entity) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.equipment[0] = new ItemData(entity.getEquipment().getHelmet());
        this.equipment[1] = new ItemData(entity.getEquipment().getChestplate());
        this.equipment[2] = new ItemData(entity.getEquipment().getLeggings());
        this.equipment[3] = new ItemData(entity.getEquipment().getBoots());
        this.equipment[4] = new ItemData(entity.getEquipment().getItemInHand());
        this.isPlayer = entity instanceof Player;
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;
        
        org.bukkit.inventory.ItemStack mainHand = equipment[4].toItemStack();
        org.bukkit.inventory.ItemStack helmet = equipment[0].toItemStack();
        org.bukkit.inventory.ItemStack chestplate = equipment[1].toItemStack();
        org.bukkit.inventory.ItemStack leggings = equipment[2].toItemStack();
        org.bukkit.inventory.ItemStack boots = equipment[3].toItemStack();

        List<com.github.retrooper.packetevents.protocol.player.Equipment> equipmentList = new ArrayList<>();

        if (mainHand != null) equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(EquipmentSlot.MAIN_HAND, convertToPacketEventsItem(mainHand)));

        if (helmet != null) equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(EquipmentSlot.HELMET, convertToPacketEventsItem(helmet)));
        if (chestplate != null) equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(EquipmentSlot.CHEST_PLATE, convertToPacketEventsItem(chestplate)));
        if (leggings != null) equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(EquipmentSlot.LEGGINGS, convertToPacketEventsItem(leggings)));
        if (boots != null) equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(EquipmentSlot.BOOTS, convertToPacketEventsItem(boots)));
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(fakeEntityId, equipmentList);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            if (!equipmentList.isEmpty()) user.sendPacket(packet);
        }
    }
    
    private ItemStack convertToPacketEventsItem(org.bukkit.inventory.ItemStack bukkitStack) {
        if (bukkitStack == null) return ItemStack.EMPTY;
        return ItemStack.builder()
            .type(Objects.requireNonNull(ItemTypes.getByName("minecraft:" + bukkitStack.getType().name().toLowerCase())))
            .amount(bukkitStack.getAmount())
            .build();
    }


    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.EQUIPMENT;
    }
}