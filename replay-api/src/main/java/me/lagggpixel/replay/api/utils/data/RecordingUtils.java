package me.lagggpixel.replay.api.utils.data;

import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;

import java.io.*;
import java.util.Set;
import java.util.UUID;

public class RecordingUtils {

    public static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = in.readByte();
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    public static void writeCompressedString(DataOutputStream out, String str) throws IOException {
        byte[] bytes = str.getBytes("UTF-8");
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static String readCompressedString(DataInputStream in) throws IOException {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    public static void writeDeltaFrame(DataOutputStream out, IFrame frame, Set<Recordable.RecordableSignature> additions,  Set<Recordable.RecordableSignature> removals) throws IOException {
        // Write number of additions and removals
        writeVarInt(out, additions.size());
        writeVarInt(out, removals.size());
        
        // Write only added recordables
        for (Recordable.RecordableSignature sig : additions) {
            Recordable recordable = findRecordable(frame, sig);
            if (recordable != null) {
                out.writeShort(recordable.getTypeId());
                recordable.write(out);
            }
        }
        
        // Write only removed recordable signatures
        for (Recordable.RecordableSignature sig : removals) {
            out.writeShort(sig.getTypeId());
            sig.writeIdentifier(out);
        }
    }

    public static Set<Recordable.RecordableSignature> getRecordableSignatures(IFrame frame) {
        Set<Recordable.RecordableSignature> signatures = new java.util.HashSet<>();
        for (Recordable recordable : frame.getRecordables()) {
            signatures.add(new Recordable.RecordableSignature(recordable));
        }
        return signatures;
    }

    public static void writeCompressedUuid(DataOutputStream out, UUID uuid, boolean isVersion4) throws IOException {
        if (isVersion4) {
            // Version 4 UUID: Can skip version/variant bits (predictable)
            long msb = uuid.getMostSignificantBits();
            long lsb = uuid.getLeastSignificantBits();
            
            // Remove version bits (4 bits at position 48-51 = 0x4)
            // Remove variant bits (2 bits at position 62-63 = 0b10)
            // These can be reconstructed on read
            
            out.writeLong(msb);
            out.writeLong(lsb);
        } else {
            // Unknown version - write full
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }
    }

    public static UUID readCompressedUuid(DataInputStream in, boolean isVersion4) throws IOException {
        long msb = in.readLong();
        long lsb = in.readLong();
        
        // Version/variant are already in the bits, no reconstruction needed
        return new UUID(msb, lsb);
    }

    public static void writeDeltaUuid(DataOutputStream out, UUID uuid, UUID previous, boolean isVersion4) throws IOException {
        long msbDelta = uuid.getMostSignificantBits() - previous.getMostSignificantBits();
        long lsbDelta = uuid.getLeastSignificantBits() - previous.getLeastSignificantBits();
        
        // Determine compression strategy
        if (msbDelta == 0 && lsbDelta == 0) {
            // Identical UUID (shouldn't happen, but handle it)
            out.writeByte(0);
        } else if (msbDelta == 0 && lsbDelta >= Byte.MIN_VALUE && lsbDelta <= Byte.MAX_VALUE) {
            out.writeByte(1);
            out.writeByte((byte) lsbDelta);
        } else if (msbDelta == 0 && lsbDelta >= Short.MIN_VALUE && lsbDelta <= Short.MAX_VALUE) {
            out.writeByte(2);
            out.writeShort((short) lsbDelta);
        } else if (msbDelta == 0 && lsbDelta >= Integer.MIN_VALUE && lsbDelta <= Integer.MAX_VALUE) {
            out.writeByte(3);
            out.writeInt((int) lsbDelta);
        } else if (msbDelta == 0) {
            out.writeByte(4);
            out.writeLong(lsbDelta);
        } else {
            // Full UUID
            out.writeByte(5);
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }
    }

    public static UUID readDeltaUuid(DataInputStream in, UUID previous, boolean isVersion4) throws IOException {
        byte compressionType = in.readByte();
        
        long msb = previous.getMostSignificantBits();
        long lsb = previous.getLeastSignificantBits();
        
        switch (compressionType) {
            case 0: // Identical
                return previous;
            case 1: // Byte delta
                lsb += in.readByte();
                break;
            case 2: // Short delta
                lsb += in.readShort();
                break;
            case 3: // Int delta
                lsb += in.readInt();
                break;
            case 4: // Long delta
                lsb += in.readLong();
                break;
            case 5: // Full
                msb = in.readLong();
                lsb = in.readLong();
                break;
        }
        
        return new UUID(msb, lsb);
    }

    public static int getUuidVersion(UUID uuid) {
        return (int) ((uuid.getMostSignificantBits() >> 12) & 0x0F);
    }

    private static Recordable findRecordable(IFrame frame, Recordable.RecordableSignature sig) {
        for (Recordable recordable : frame.getRecordables()) {
            Recordable.RecordableSignature recordableSig = new Recordable.RecordableSignature(recordable);
            if (recordableSig.equals(sig)) return recordable;
        }
        return null;
    }
}
