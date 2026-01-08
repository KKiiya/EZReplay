package me.lagggpixel.replay.api.replay.data;

import java.util.*;

import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.BOOLEAN;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.VAR_INT;


public class EntityIndex implements ReplayByteBuffer.Writer {
    private final Map<UUID, Short> uuidToId = new HashMap<>();
    private final Map<Short, UUID> idToUuid = new HashMap<>();
    private short nextId = 1; // 0 can be reserved for "invalid"

    public EntityIndex(ReplayByteBuffer reader) {
        int size = reader.read(VAR_INT);
        if (size <= 0) {
            return;
        }
        boolean allVersion4 = reader.read(BOOLEAN);
        short previousId = 0;
        for (int i = 0; i < size; i++) {
            int delta = reader.read(VAR_INT);
            short id = (short) ((previousId & 0xFFFF) + delta);
            UUID uuid = reader.read(ReplayByteBuffer.UUID);
            uuidToId.put(uuid, id);
            idToUuid.put(id, uuid);
            previousId = id;
            nextId = (short) Math.max(nextId, (id + 1));
        }
    }


    public short getOrRegister(UUID uuid) {
        return uuidToId.computeIfAbsent(uuid, key -> {
            short id = nextId++;
            idToUuid.put(id, key);
            return id;
        });
    }

    public UUID getUuid(short id) {
        return idToUuid.get(id);
    }


    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(VAR_INT, uuidToId.size());
        if (uuidToId.isEmpty()) return;
        List<Map.Entry<UUID, Short>> sorted = new ArrayList<>(uuidToId.entrySet());
        sorted.sort(Map.Entry.comparingByValue());
        boolean allVersion4 = sorted.stream()
                .allMatch(e -> getUuidVersion(e.getKey()) == 4);
        writer.write(BOOLEAN, allVersion4);
        short previousId = 0;
        UUID previousUuid = null;
        for (var entry : sorted) {
            UUID uuid = entry.getKey();
            short id = entry.getValue();
            int delta = (id & 0xFFFF) - (previousId & 0xFFFF);
            writer.write(VAR_INT, delta);
            writer.write(ReplayByteBuffer.UUID, uuid);
            previousId = id;
            previousUuid = uuid;
        }
    }

    public static int getUuidVersion(UUID uuid) {
        return (int) ((uuid.getMostSignificantBits() >> 12) & 0x0F);
    }
}
