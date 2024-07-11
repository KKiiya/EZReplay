package me.lagggpixel.replay.support.nms.utils;

import me.lagggpixel.replay.api.utils.block.BlockPosition;
import me.lagggpixel.replay.api.utils.block.IBlockData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;

public class BlockData implements IBlockData {
    private final BlockPosition loc;
    private final Material mat;
    private final BlockFace face;
    private final byte data;

    public BlockData(Block block) {
        this.loc = BlockPosition.fromBukkitLocation(block.getLocation());
        this.mat = block.getType();
        this.face = block.getFace(block);
        this.data = block.getData();
    }

    @Override
    public Material getMaterial() {
        return mat;
    }

    @Override
    public BlockPosition getPosition() {
        return loc;
    }

    @Override
    public BlockFace getFacing() {
        return face;
    }

    @Override
    public byte getData() {
        return data;
    }

    @Override
    public String getStepSound() {
        return CraftMagicNumbers.getBlock(mat).stepSound.getStepSound();
    }

    @Override
    public String getBreakSound() {
        return CraftMagicNumbers.getBlock(mat).stepSound.getBreakSound();
    }

    @Override
    public String getPlaceSound() {
        return CraftMagicNumbers.getBlock(mat).stepSound.getPlaceSound();
    }

    @Override
    public float getSoundVolume() {
        return CraftMagicNumbers.getBlock(mat).stepSound.getVolume1();
    }

    @Override
    public float getSoundPitch() {
        return CraftMagicNumbers.getBlock(mat).stepSound.getVolume2();
    }
}
