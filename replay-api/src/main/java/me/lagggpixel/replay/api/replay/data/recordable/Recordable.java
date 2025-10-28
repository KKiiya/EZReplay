package me.lagggpixel.replay.api.replay.data.recordable;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.serialize.BinarySerializable;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public abstract class Recordable implements BinarySerializable {
    private final IRecording replay;

    public Recordable(IRecording replay) {
        this.replay = replay;
    }

    public IRecording getRecording() {
        return replay;
    }

    public abstract void play(IReplaySession replaySession, Player player);

    public abstract void unplay(IReplaySession replaySession, Player player);

    public abstract short getTypeId();

    public void read(DataInputStream in, EntityIndex index) throws IOException {
        // Implemented in subclasses
    }

    /**
     * Writes all @Writeable fields automatically.
     * Converts UUIDs registered in EntityIndex into their short ID form.
     */
    public void write(DataOutputStream out) throws IOException {
        EntityIndex index = replay.getEntityIndex();

        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Writeable.class)) continue;

            field.setAccessible(true);
            Object value;
            try {
                value = field.get(this);
            } catch (IllegalAccessException e) {
                throw new IOException("Failed to access @Writeable field: " + field.getName(), e);
            }

            // null safety
            if (value == null) {
                out.writeByte(0); // null marker
                continue;
            } else {
                out.writeByte(1); // non-null marker
            }

            Class<?> type = field.getType();

            // --- Handle common types ---
            if (type == int.class || type == Integer.class) out.writeInt((int) value);
            else if (type == long.class || type == Long.class) out.writeLong((long) value);
            else if (type == double.class || type == Double.class) out.writeDouble((double) value);
            else if (type == float.class || type == Float.class) out.writeFloat((float) value);
            else if (type == short.class || type == Short.class) out.writeShort((short) value);
            else if (type == byte.class || type == Byte.class) out.writeByte((byte) value);
            else if (type == boolean.class || type == Boolean.class) out.writeBoolean((boolean) value);
            else if (type == String.class) out.writeUTF((String) value);
            // --- EntityIndex UUID conversion ---
            else if (type == UUID.class) {
                short entityId = index.getOrRegister((UUID) value);
                out.writeShort(entityId);
            }
            // --- Enums ---
            else if (type.isEnum()) {
                out.writeShort(((Enum<?>) value).ordinal());
            }
            else writeNestedObject(out, value);
        }
    }

    /**
     * Recursively writes nested objects that have @Writeable fields.
     */
    private void writeNestedObject(DataOutputStream out, Object obj) throws IOException {
        if (obj == null) {
            out.writeByte(0);
            return;
        }

        out.writeByte(1);
        Class<?> clazz = obj.getClass();

        for (Field subField : clazz.getDeclaredFields()) {
            if (!subField.isAnnotationPresent(Writeable.class)) continue;

            subField.setAccessible(true);
            Object subValue;
            try {
                subValue = subField.get(obj);
            } catch (IllegalAccessException e) {
                throw new IOException("Failed to access nested field: " + subField.getName(), e);
            }

            if (subValue == null) {
                out.writeByte(0);
                continue;
            } else out.writeByte(1);


            Class<?> subType = subField.getType();

            if (subType == int.class || subType == Integer.class) out.writeInt((int) subValue);
            else if (subType == double.class || subType == Double.class) out.writeDouble((double) subValue);
            else if (subType == float.class || subType == Float.class) out.writeFloat((float) subValue);
            else if (subType == long.class || subType == Long.class) out.writeLong((long) subValue);
            else if (subType == boolean.class || subType == Boolean.class) out.writeBoolean((boolean) subValue);
            else if (subType == String.class) out.writeUTF((String) subValue);
            else if (subType.isEnum()) out.writeShort(((Enum<?>) subValue).ordinal());
            else if (subType == UUID.class) out.writeShort(replay.getEntityIndex().getOrRegister((UUID) subValue));
            else throw new IOException("Unsupported nested @Writeable field: " + subField.getName());
        }
    }
}
