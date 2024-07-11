package me.lagggpixel.replay.api.utils.block;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public interface IBlockData {
    Material getMaterial();
    BlockPosition getPosition();
    BlockFace getFacing();
    byte getData();

    String getStepSound();
    String getBreakSound();
    String getPlaceSound();

    float getSoundVolume();
    float getSoundPitch();
}
