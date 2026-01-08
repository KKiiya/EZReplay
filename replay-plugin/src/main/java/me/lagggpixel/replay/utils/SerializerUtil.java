package me.lagggpixel.replay.utils;

import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.api.utils.Vector3d;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.DOUBLE;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.FLOAT;

public class SerializerUtil {

    public static void write3DVector(ReplayByteBuffer writer, Vector3d vector) {
        writer.write(DOUBLE, vector.getX());
        writer.write(DOUBLE, vector.getY());
        writer.write(DOUBLE, vector.getZ());
        writer.write(FLOAT, vector.getPitch());
        writer.write(FLOAT, vector.getYaw());
    }

    public static Vector3d read3DVector(ReplayByteBuffer reader) {
        final double x = reader.read(DOUBLE);
        final double y = reader.read(DOUBLE);
        final double z = reader.read(DOUBLE);
        final float yaw = reader.read(FLOAT);
        final float pitch = reader.read(FLOAT);
        return new Vector3d(x, y, z, yaw, pitch);
    }
}
