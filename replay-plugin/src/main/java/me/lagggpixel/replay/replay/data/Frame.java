package me.lagggpixel.replay.replay.data;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.INT;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.SHORT;

public class Frame implements IFrame {

    private final List<Recordable> recordables;
    private final IRecording replay;

    public Frame(IRecording replay, List<Recordable> recordables) {
        this.replay = replay;
        this.recordables = recordables;
    }

    public Frame(IRecording replay, Recordable... recordables) {
        this(replay, new ArrayList<>(Arrays.asList(recordables)));
    }

    public Frame(ReplayByteBuffer reader) throws IOException {
        int recordableCount = reader.read(INT);
        this.recordables = new ArrayList<>(recordableCount);

        for (int i = 0; i < recordableCount; i++) {
            short typeId = reader.read(SHORT);
            Recordable recordable = RecordableRegistry.create(typeId, reader);
            this.recordables.add(recordable);
        }

        this.replay = null; // will be set when attached to a Recording
    }

    public Frame(IRecording replay) {
        this(replay, new ArrayList<>());
    }

    @Override
    public List<Recordable> getRecordables() {
        return this.recordables;
    }

    @Override
    public IRecording getReplay() {
        return replay;
    }

    @Override
    public void addRecordable(Recordable... recordables) {
        this.recordables.addAll(Arrays.asList(recordables));
    }

    @Override
    public void addAsList(List<Recordable> recordableList) {
        this.recordables.addAll(recordableList);
    }

    @Override
    public void play(IReplaySession replaySession) {
        for (Recordable recordable : recordables) {
            try {
                recordable.play(replaySession);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {
        for (Recordable recordable : recordables) {
            try {
                recordable.unplay(replaySession);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(INT, recordables.size());
        for (Recordable recordable : recordables) {
            writer.write(SHORT, recordable.getTypeId());
            recordable.write(writer);
        }
    }
}
