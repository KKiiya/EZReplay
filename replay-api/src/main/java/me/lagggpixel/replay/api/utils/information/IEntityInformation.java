package me.lagggpixel.replay.api.utils.information;

import me.lagggpixel.replay.api.utils.Camera;
import me.lagggpixel.replay.api.utils.Vector3d;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public interface IEntityInformation {

    UUID getUniqueId();

    int getEntityId();

    Vector3d getLocation();

    Camera getCamera();

    boolean isBurning();

    int getFireTicks();
}
