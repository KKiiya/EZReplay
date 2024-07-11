package me.lagggpixel.replay.api.replay.data.recordable.world.block;

import me.lagggpixel.replay.api.replay.data.recordable.world.IWorldRecordable;
import me.lagggpixel.replay.api.utils.block.IBlockData;

public interface IBlockRecordable extends IWorldRecordable {
    IBlockData getBlockData();
}
