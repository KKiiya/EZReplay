package me.lagggpixel.replay.api.utils.block;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
public abstract class AbstractBlockBreaker {

    private final Entity entity;
    private final Location location;
    private final int startDickTick;
    protected final int x;
    protected final int y;
    protected final int z;

    public AbstractBlockBreaker(Entity entity, Location targetBlock) {
        this.entity = entity;
        this.location = targetBlock;
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.startDickTick = (int) (System.currentTimeMillis() / 50);
    }

    /**
     * Get the damage applied to the block between
     * the starting dig tick and the inputted tick
     * @param tickDifference The ticks progressed between the starting tick (must be higher)
     * @return the damage applied
     */
    public abstract float getDamage(int tickDifference);

    /**
     * Set the block damage
     * @param entityId The entity that id that should be breaking the block
     * @param damage The damage to be applied (0-9)
     * @param player The player to show the block break stage
     */
    public abstract void setBlockDamage(int entityId, int damage, Player player);
}
