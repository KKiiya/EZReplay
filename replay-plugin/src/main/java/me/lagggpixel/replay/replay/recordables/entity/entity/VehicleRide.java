package me.lagggpixel.replay.replay.recordables.entity.entity;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


import me.lagggpixel.replay.support.nms.v1_8_R3;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

public class VehicleRide extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final short vehicleId;

    public VehicleRide(IRecording replay, Entity vehicle, Entity entity) {
        super(replay);
        this.vehicleId = (short) vehicle.getEntityId();
        this.entityId = (short) entity.getEntityId();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity entity = replaySession.getSpawnedEntities().get(this.entityId);
        Entity vehicle = replaySession.getSpawnedEntities().get(this.vehicleId);
        if (entity == null || vehicle == null) return;
        CraftEntity craftVehicle = (CraftEntity) vehicle;
        craftVehicle.getHandle().passenger = ((CraftEntity) entity).getHandle();
        CraftEntity craftEntity = (CraftEntity) entity;
        craftEntity.getHandle().vehicle = craftVehicle.getHandle();

        PacketPlayOutEntityMetadata vehicleMetadata = new PacketPlayOutEntityMetadata(vehicle.getEntityId(), craftVehicle.getHandle().getDataWatcher(), true);
        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entity.getEntityId(), craftEntity.getHandle().getDataWatcher(), true);
        v1_8_R3.sendPackets(player, vehicleMetadata, entityMetadata);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        Entity entity = replaySession.getSpawnedEntities().get(this.entityId);
        Entity vehicle = replaySession.getSpawnedEntities().get(this.vehicleId);
        if (entity == null) return;
        CraftEntity craftEntity = (CraftEntity) entity;
        CraftEntity craftVehicle = (CraftEntity) vehicle;
        craftEntity.getHandle().vehicle = null;
        craftVehicle.getHandle().passenger = null;
        
        PacketPlayOutEntityMetadata vehicleMetadata = new PacketPlayOutEntityMetadata(vehicle.getEntityId(), craftVehicle.getHandle().getDataWatcher(), true);
        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entity.getEntityId(), craftEntity.getHandle().getDataWatcher(), true);
        v1_8_R3.sendPackets(player, vehicleMetadata, entityMetadata);
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.VEHICLE_RIDE; // Return the appropriate type ID for VehicleRide
    }
}
