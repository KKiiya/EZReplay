package me.lagggpixel.replay.api.utils.block;

import lombok.Getter;
import me.lagggpixel.replay.api.data.Writeable;
import org.bukkit.Chunk;

@Getter
public class ChunkPos {

    @Writeable private final int x;
    @Writeable private final int z;

    public ChunkPos(Chunk chunk) {
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    public ChunkPos(int x, int z) {
        this.x = x;
        this.z = z;
    }
}
