package me.lagggpixel.replay.api.replay.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.lagggpixel.replay.api.replay.serialize.BinarySerializable;
import me.lagggpixel.replay.api.utils.data.RecordingUtils;

public class EntityIndex implements BinarySerializable {
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
        RecordingUtils.writeVarInt(out, uuidToId.size());
        
        if (uuidToId.isEmpty()) return;
        
        // Analyze UUID patterns
        List<Map.Entry<UUID, Short>> sorted = new ArrayList<>(uuidToId.entrySet());
        sorted.sort(Map.Entry.comparingByValue());
        
        // Detect if UUIDs are sequential/similar
        boolean allVersion4 = sorted.stream()
                .allMatch(e -> RecordingUtils.getUuidVersion(e.getKey()) == 4);
        
        out.writeBoolean(allVersion4); // Optimization hint
        
        short previousId = 0;
        UUID previousUuid = null;
        
        for (var entry : sorted) {
            UUID uuid = entry.getKey();
            short id = entry.getValue();
            
            // Write delta ID (usually 1, so VarInt is optimal)
            int delta = (id & 0xFFFF) - (previousId & 0xFFFF);
            RecordingUtils.writeVarInt(out, delta);
            
            // Compress UUID based on pattern
            if (previousUuid == null) {
                // First UUID - write optimally
                RecordingUtils.writeCompressedUuid(out, uuid, allVersion4);
            } else {
                // Try delta compression
                RecordingUtils.writeDeltaUuid(out, uuid, previousUuid, allVersion4);
            }
            
            previousId = id;
            previousUuid = uuid;
        }
    }


    public void read(DataInputStream in) throws IOException {
        read(in, this);
    }

    @Override
    public void read(DataInputStream in, EntityIndex entityIndex) throws IOException {
        int size = RecordingUtils.readVarInt(in);
        
        if (size == 0) return;
        
        boolean allVersion4 = in.readBoolean();
        
        short currentId = 0;
        UUID previousUuid = null;
        
        for (int i = 0; i < size; i++) {
            // Read delta ID
            int delta = RecordingUtils.readVarInt(in);
            currentId = (short) ((currentId & 0xFFFF) + delta);
            
            // Read UUID
            UUID uuid;
            if (previousUuid == null) {
                uuid = RecordingUtils.readCompressedUuid(in, allVersion4);
            } else {
                uuid = RecordingUtils.readDeltaUuid(in, previousUuid, allVersion4);
            }
            
            uuidToId.put(uuid, currentId);
            idToUuid.put(currentId, uuid);
            
            previousUuid = uuid;
        }
    }
}
