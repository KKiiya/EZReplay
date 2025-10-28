package me.lagggpixel.replay.utils;

import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.replay.data.Frame;
import me.lagggpixel.replay.replay.data.Recording;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecordingUtils {

    public static class RecordingReader {
        public Recording read(File file) throws IOException {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                if (!in.readUTF().equals("RPL"))
                    throw new IOException("Invalid replay file");

                int version = in.readInt();

                // Metadata
                UUID id = new UUID(in.readLong(), in.readLong());
                String worldName = in.readUTF();

                // Entity index
                EntityIndex index = new EntityIndex();
                index.read(in);

                // Frames
                int frameCount = in.readInt();
                List<Frame> frames = new ArrayList<>(frameCount);
                for (int i = 0; i < frameCount; i++) {
                    frames.add(new Frame(in, index));
                }

                return new Recording(id, worldName, index, frames);
            }
        }
    }

    public static class RecordingWriter {

        public void write(Recording recording, File file) throws IOException {
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                out.writeUTF("RPL"); // Magic header
                out.writeInt(1);     // Version
                recording.getEntityIndex().write(out);

                out.writeInt(recording.getFrames().size());
                for (IFrame frame : recording.getFrames()) {
                    frame.write(out);
                }
            }
        }
    }
}
