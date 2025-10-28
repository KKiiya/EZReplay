package me.lagggpixel.replay.api.replay.serialize;

import me.lagggpixel.replay.api.replay.data.EntityIndex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface BinarySerializable {
    void write(DataOutputStream out) throws IOException;
    void read(DataInputStream in, EntityIndex index) throws IOException;
}
