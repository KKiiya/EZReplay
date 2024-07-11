package me.lagggpixel.replay.api.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
@Getter
public class Vector3d {
    private final World world;
    protected final double x;
    protected final double y;
    protected final double z;

    public Vector3d(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3d fromBukkitLocation(Location loc) {
        return new Vector3d(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    public Location toBukkitLocation() {
        return new Location(world, x, y, z);
    }

    public boolean equals(Object var1) {
        if (!(var1 instanceof Vector3d)) {
            return false;
        } else {
            Vector3d var2 = (Vector3d) var1;
            return this.x == var2.x && this.y == var2.y && this.z == var2.z;
        }
    }
}
