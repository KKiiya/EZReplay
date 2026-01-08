package me.lagggpixel.replay.utils;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.replay.data.Frame;
import me.lagggpixel.replay.replay.data.Recording;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public final class RecordingFileProcessor {

    private static final byte RECORDING_VERSION = 1;
    private static final String FILE_EXTENSION = ".replay";

    private final File directory =
            new File(Replay.getInstance().getDataFolder(), "recordings");

    public RecordingFileProcessor() {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create recordings directory: " + directory
            );
        }
    }


    public File createRecordingFile(IRecording recording) {
        ReplayByteBuffer writer =
                new ReplayByteBuffer(ByteBuffer.allocate(1024 * 1024));

        writer.write(BYTE, RECORDING_VERSION);
        writer.write(STRING, recording.getID().toString());

        List<IFrame> frames = recording.getFrames();
        for (int i = 0; i < frames.size(); i++) {
            writer.write(VAR_INT, i);
            frames.get(i).write(writer);
        }

        writer.write(VAR_INT, 0xFF);
        recording.getEntityIndex().write(writer);

        File file = new File(directory, recording.getID() + FILE_EXTENSION);
        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("The file of the recording could not be created");
            }
        } catch (Exception exception) {
            throw new IllegalStateException("There went someting wrong while creating the recording file.", exception);
        }

        writer.nioBuffer().limit(writer.writeIndex());

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(writer.readBytes(writer.writeIndex()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.out.println("Created recording file: " + file.getAbsolutePath());

        return file;
    }


    public IRecording loadRecording(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            ReplayByteBuffer reader = new ReplayByteBuffer(ByteBuffer.wrap(bytes));
            byte version = reader.read(BYTE);
            if (version != RECORDING_VERSION) {
                throw new IllegalStateException("Invalid recording version");
            }
            String id = reader.read(STRING);
            List<IFrame> frames = new ArrayList<>();
            int time;
            while ((time = reader.read(VAR_INT)) != 0xFF) {
                Frame frame = new Frame(reader);
                frames.add(time, frame);
            }
            return new Recording(RECORDING_VERSION, java.util.UUID.fromString(id), "world", new EntityIndex(reader), frames);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
