package me.lagggpixel.replay.api.utils.block;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */

@Getter
@Setter
public class BlockPosition {
    private final World world;
    private int x;
    private int y;
    private int z;

    public BlockPosition(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition add(BlockPosition position) {
        setX(position.getX() + getX());
        setY(position.getY() + getY());
        setZ(position.getZ() + getZ());
        return this;
    }

    public BlockPosition add(int x, int y, int z) {
        setX(x + getX());
        setY(y + getY());
        setZ(z + getZ());
        return this;
    }

    public BlockPosition subtract(BlockPosition position) {
        setX(position.getX() - getX());
        setY(position.getY() - getY());
        setZ(position.getZ() - getZ());
        return this;
    }

    public BlockPosition subtract(int x, int y, int z) {
        setX(getX() - x);
        setY(getY() - y);
        setZ(getZ() - z);
        return this;
    }

    public static BlockPosition fromBukkitLocation(Location location) {
        return new BlockPosition(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location toBukkitLocation() {
        return new Location(world, x, y, z);
    }

    public boolean equals(Object var1) {
        if (!(var1 instanceof BlockPosition)) {
            return false;
        } else {
            BlockPosition var2 = (BlockPosition) var1;
            return this.x == var2.x && this.y == var2.y && this.z == var2.z;
        }
    }
}
