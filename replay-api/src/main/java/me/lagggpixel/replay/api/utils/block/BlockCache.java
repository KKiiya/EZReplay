package me.lagggpixel.replay.api.utils.block;

import lombok.Getter;
import lombok.Setter;
import me.lagggpixel.replay.api.data.Writeable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */

@Getter
@Setter
public class BlockCache {
    @Writeable private final Material material;
    @Writeable private final byte data;
    @Writeable private int x;
    @Writeable private int y;
    @Writeable private int z;

    public BlockCache(Block block) {
        this.material = block.getType();
        this.data = block.getData();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public BlockCache(Material material, byte data, Location location) {
        this.material = material;
        this.data = data;
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public BlockCache(Material material, byte data, int x, int y, int z) {
        this.material = material;
        this.data = data;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockCache addPosition(BlockCache position) {
        setX(position.getX() + getX());
        setY(position.getY() + getY());
        setZ(position.getZ() + getZ());
        return this;
    }

    public BlockCache addPosition(int x, int y, int z) {
        setX(x + getX());
        setY(y + getY());
        setZ(z + getZ());
        return this;
    }

    public BlockCache subtractPosition(BlockCache position) {
        setX(getX() - position.getX());
        setY(getY() - position.getY());
        setZ(getZ() - position.getZ());
        return this;
    }

    public BlockCache subtractPosition(int x, int y, int z) {
        setX(getX() - x);
        setY(getY() - y);
        setZ(getZ() - z);
        return this;
    }

    public static BlockCache fromBukkitLocation(Location location) {
        assert location.getWorld() != null;
        return new BlockCache(location.getBlock().getType(), location.getBlock().getData(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location toBukkitLocation(World world) {
        return new Location(world, x, y, z);
    }

    public boolean equals(Object var1) {
        if (!(var1 instanceof BlockCache)) {
            return false;
        } else {
            BlockCache var2 = (BlockCache) var1;
            return this.x == var2.x && this.y == var2.y && this.z == var2.z;
        }
    }
}
