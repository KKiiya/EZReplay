package me.lagggpixel.replay.replay.data;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public Frame(DataInputStream in, EntityIndex index) throws IOException {
        int recordableCount = in.readShort();
        this.recordables = new ArrayList<>(recordableCount);

        for (int i = 0; i < recordableCount; i++) {
            short typeId = in.readShort();
            Recordable recordable = RecordableRegistry.create(typeId, in, index);
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
    public void play(IReplaySession replaySession, Player player) {
        for (Recordable recordable : recordables) {
            try {
                recordable.play(replaySession, player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        for (Recordable recordable : recordables) {
            try {
                recordable.unplay(replaySession, player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeShort(recordables.size());
        for (Recordable recordable : recordables) {
            out.writeShort(recordable.getTypeId());
            recordable.write(out);
        }
    }

    @Override
    public void read(DataInputStream in, EntityIndex index) throws IOException {
        int recordableCount = in.readShort();
        this.recordables.clear();

        for (int i = 0; i < recordableCount; i++) {
            short typeId = in.readShort();
            Recordable recordable = RecordableRegistry.create(typeId, in, index);
            this.recordables.add(recordable);
        }
    }
}
