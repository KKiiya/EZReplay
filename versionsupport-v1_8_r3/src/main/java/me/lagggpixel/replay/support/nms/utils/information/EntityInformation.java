package me.lagggpixel.replay.support.nms.utils.information;

import lombok.Getter;
import me.lagggpixel.replay.api.utils.Camera;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.information.IEntityInformation;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
@Getter
public class EntityInformation implements IEntityInformation {

    private final UUID uniqueId;
    private final int entityId;
    private final Vector3d location;
    private final Camera camera;
    private final boolean isBurning;
    private final int fireTicks;

    public EntityInformation(Entity entity) {
        if (entity == null) {
            throw new RuntimeException("Entity cannot be null when creating a IEntityInformation instance");
        }
        this.uniqueId = entity.getUniqueId();
        this.entityId = entity.getEntityId();
        this.location = new Vector3d(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
        this.camera = new Camera(entity.getLocation().getYaw(), entity.getLocation().getPitch());
        this.isBurning = entity.getFireTicks() != 0;
        this.fireTicks = entity.getFireTicks();
    }
}
