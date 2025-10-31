package me.lagggpixel.replay.api.utils;

import lombok.Getter;
import me.lagggpixel.replay.api.data.Writeable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
@Getter
public class Vector3d {
    @Writeable protected final double x;
    @Writeable protected final double y;
    @Writeable protected final double z;
    @Writeable protected final float yaw;
    @Writeable protected final float pitch;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public Vector3d(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vector3d(Vector vector) {
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();
        this.yaw = 0;
        this.pitch = 0;
    }

    public static Vector3d fromBukkitLocation(Location loc) {
        return new Vector3d(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public Location toBukkitLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean equals(Object var1) {
        if (!(var1 instanceof Vector3d)) return false;
        Vector3d var2 = (Vector3d) var1;
        return this.x == var2.x && this.y == var2.y && this.z == var2.z && this.yaw == var2.yaw && this.pitch == var2.pitch;
    }
}
