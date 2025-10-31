package me.lagggpixel.replay.support.nms.recordable.entity.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;

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
        vehicle.setPassenger(entity);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        Entity entity = replaySession.getSpawnedEntities().get(this.entityId);
        if (entity == null) return;
        entity.eject();
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.VEHICLE_RIDE; // Return the appropriate type ID for VehicleRide
    }
}
