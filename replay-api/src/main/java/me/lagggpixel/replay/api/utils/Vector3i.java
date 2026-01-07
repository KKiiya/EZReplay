package me.lagggpixel.replay.api.utils;

import lombok.Getter;
import me.lagggpixel.replay.api.data.Writeable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Vector3i {
    @Getter @Writeable protected final int x;
    @Getter @Writeable protected final int y;
    @Getter @Writeable protected final int z;
    @Getter @Writeable protected final float yaw;
    @Getter @Writeable protected final float pitch;

    public Vector3i() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.yaw = 0;
        this.pitch = 0;
    }

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public Vector3i(int x, int y, int z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vector3i(Vector vector) {
        this.x = (int) vector.getX();
        this.y = (int) vector.getY();
        this.z = (int) vector.getZ();
        this.yaw = 0;
        this.pitch = 0;
    }

    public Location toBukkitLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Vector3d toVector3d() {
        return new Vector3d(this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public Vector toBukkitVector() {
        return new Vector(this.x, this.y, this.z);
    }

    public boolean equals(Object var1) {
        if (!(var1 instanceof Vector3d)) return false;
        Vector3d var2 = (Vector3d) var1;
        return this.x == var2.x && this.y == var2.y && this.z == var2.z && this.yaw == var2.yaw && this.pitch == var2.pitch;
    }

    public static Vector3i fromVector3d(Vector3d vec) {
        return new Vector3i((int) vec.getX(), (int) vec.getY(), (int) vec.getZ(), vec.getYaw(), vec.getPitch());
    }

    public static Vector3i fromBukkitVector(Vector vec) {
        return new Vector3i((int) vec.getX(), (int) vec.getY(), (int) vec.getZ());
    }

    public static Vector3d fromBukkitLocation(Location loc) {
        return new Vector3d(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
}
