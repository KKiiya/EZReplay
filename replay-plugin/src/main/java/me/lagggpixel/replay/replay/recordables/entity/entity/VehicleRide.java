package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class VehicleRide extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final short vehicleId;

    public VehicleRide(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.vehicleId = reader.read(SHORT);
    }

    public VehicleRide(IRecording replay, Entity vehicle, Entity entity) {
        super(replay);
        this.vehicleId = replay.getEntityIndex().getOrRegister(vehicle.getUniqueId());
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
    }

    @Override
    public void play(IReplaySession replaySession) {
        UUID entity = replaySession.getSpawnedEntities().get(this.entityId).getUuid();
        UUID vehicle = replaySession.getSpawnedEntities().get(this.vehicleId).getUuid();
        if (entity == null || vehicle == null) return;
        
        int fakeVehicleId = vehicleId + 100000;
        int fakeEntityId = entityId + 100000;

        WrapperPlayServerSetPassengers passengersPacket = new WrapperPlayServerSetPassengers(fakeVehicleId, new int[]{fakeEntityId});
        // Set the entity as a passenger of the vehicle
        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(passengersPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {
        UUID entity = replaySession.getSpawnedEntities().get(this.entityId).getUuid();
        UUID vehicle = replaySession.getSpawnedEntities().get(this.vehicleId).getUuid();
        if (entity == null || vehicle == null) return;
        
        int fakeVehicleId = vehicleId + 100000;
        
        // Remove passengers
        WrapperPlayServerSetPassengers passengersPacket = new WrapperPlayServerSetPassengers(fakeVehicleId, new int[]{});
        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(passengersPacket);
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.VEHICLE_RIDE; // Return the appropriate type ID for VehicleRide
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(SHORT, vehicleId);
    }
}
