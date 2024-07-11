package me.lagggpixel.replay.api.utils.information;

import lombok.Getter;
import me.lagggpixel.replay.api.utils.Camera;
import me.lagggpixel.replay.api.utils.Vector3d;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public interface IPlayerInformation {

    UUID getUniqueId();

    int getEntityId();

    Vector3d getLocation();

    Camera getCamera();

    boolean isCrouching();

    boolean isBurning();

    boolean isSprinting();

    boolean isBlocking();

    boolean isSwimming();

    boolean isInvisible();

    int getFireTicks();
}
