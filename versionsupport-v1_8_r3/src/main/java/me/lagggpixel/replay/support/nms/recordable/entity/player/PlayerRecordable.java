package me.lagggpixel.replay.support.nms.recordable.entity.player;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.IPlayerRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.Camera;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.entity.player.ReplayPlayer;
import me.lagggpixel.replay.api.utils.information.IPlayerInformation;
import me.lagggpixel.replay.support.nms.utils.information.PlayerInformation;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class PlayerRecordable extends Recordable implements IPlayerRecordable {
    private final IPlayerInformation playerInformation;

    public PlayerRecordable(IRecording replay, Player recordedPlayer) {
        super(replay);
        this.playerInformation = new PlayerInformation(recordedPlayer);
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        ReplayPlayer replayEntity = replaySession.getFakePlayer(getUniqueId().toString());
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) replayEntity.getAssociatedPlayer()).getHandle();

        Vector3d newPosition = playerInformation.getLocation();
        Camera newCamera = playerInformation.getCamera();

        double newX = newPosition.getX();
        double newY = newPosition.getY();
        double newZ = newPosition.getZ();

        float newYaw = newCamera.getYaw();
        float newPitch = newCamera.getPitch();

        entity.setPositionRotation(newX, newY, newZ, newYaw, newPitch);

        PacketPlayOutEntityTeleport positionPacket = new PacketPlayOutEntityTeleport(entity);

        v1_8_R3.sendPacket(player, positionPacket);
    }

    @Override
    public int getEntityId() {
        return playerInformation.getEntityId();
    }

    @Override
    public UUID getUniqueId() {
        return playerInformation.getUniqueId();
    }
}
