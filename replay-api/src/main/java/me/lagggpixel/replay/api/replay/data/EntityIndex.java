package me.lagggpixel.replay.api.replay.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityIndex {
    private final Map<UUID, Short> uuidToId = new HashMap<>();
    private final Map<Short, UUID> idToUuid = new HashMap<>();
    private short nextId = 1; // 0 can be reserved for "invalid"

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

    public void write(DataOutputStream out) throws IOException {
        out.writeShort(uuidToId.size());
        for (var entry : uuidToId.entrySet()) {
            UUID uuid = entry.getKey();
            short id = entry.getValue();
            out.writeShort(id);
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }
    }

    public void read(DataInputStream in) throws IOException {
        short size = in.readShort();
        for (int i = 0; i < size; i++) {
            short id = in.readShort();
            UUID uuid = new UUID(in.readLong(), in.readLong());
            uuidToId.put(uuid, id);
            idToUuid.put(id, uuid);
        }
    }
}
