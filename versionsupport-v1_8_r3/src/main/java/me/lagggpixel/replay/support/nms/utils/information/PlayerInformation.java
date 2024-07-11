package me.lagggpixel.replay.support.nms.utils.information;


import lombok.Getter;
import me.lagggpixel.replay.api.utils.Camera;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.information.IPlayerInformation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
@Getter
public class PlayerInformation implements IPlayerInformation {

    private final UUID uniqueId;
    private final int entityId;
    private final Vector3d location;
    private final Camera camera;
    private final boolean isCrouching;
    private final boolean isBurning;
    private final boolean isSprinting;
    private final boolean isBlocking;
    private final boolean isSwimming;
    private final boolean isInvisible;
    private final int fireTicks;

    public PlayerInformation(Player player) {
        if (player == null) {
            throw new RuntimeException("Player cannot be null when creating a PlayerInformation instance");
        }
        this.uniqueId = player.getUniqueId();
        this.entityId = player.getEntityId();
        this.location = new Vector3d(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        this.camera = new Camera(player.getLocation().getYaw(), player.getLocation().getPitch());
        this.isCrouching = player.isSneaking();
        this.isBurning = player.getFireTicks() != 0;
        this.isSprinting = player.isSprinting();
        this.isBlocking = player.isBlocking();
        this.isSwimming = false;
        this.isInvisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
        this.fireTicks = player.getFireTicks();
    }

    public PlayerInformation(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new RuntimeException("Player cannot be null when creating a IPlayerInformation instance");
        }
        this.uniqueId = uuid;
        this.entityId = player.getEntityId();
        this.location = new Vector3d(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        this.camera = new Camera(player.getLocation().getYaw(), player.getLocation().getPitch());
        this.isCrouching = player.isSneaking();
        this.isBurning = player.getFireTicks() != 0;
        this.isSprinting = player.isSprinting();
        this.isBlocking = player.isBlocking();
        this.isSwimming = false;
        this.isInvisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
        this.fireTicks = player.getFireTicks();
    }
}