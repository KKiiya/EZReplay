package me.lagggpixel.replay.support.nms.recordable.entity.player.recordables;

import lombok.Getter;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.recordables.IPlayerStatus;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.information.IPlayerInformation;
import me.lagggpixel.replay.support.nms.utils.information.PlayerInformation;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
@Getter
public class PlayerStatus extends Recordable implements IPlayerStatus {
    private final IPlayerInformation playerInformation;

    public PlayerStatus(IRecording replay, Player p) {
        super(replay);
        this.playerInformation = new PlayerInformation(p);
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        EntityPlayer fakePlayer = ((CraftPlayer) replaySession.getFakePlayer(playerInformation.getUniqueId().toString()).getAssociatedPlayer()).getHandle();

        byte blocking = (byte) (playerInformation.isBlocking() ? 0 : 16);
        fakePlayer.getDataWatcher().c().add(new DataWatcher.WatchableObject(0, 0, blocking));

        fakePlayer.setSneaking(playerInformation.isCrouching());
        if (playerInformation.isBurning()) fakePlayer.setOnFire(playerInformation.getFireTicks());
        fakePlayer.setSprinting(playerInformation.isSprinting());
        fakePlayer.setInvisible(playerInformation.isInvisible());

        PacketPlayOutEntityMetadata playerMetadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), fakePlayer.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, playerMetadata);
    }

    @Override
    public UUID getUniqueId() {
        return playerInformation.getUniqueId();
    }

    @Override
    public int getEntityId() {
        return playerInformation.getEntityId();
    }
}
