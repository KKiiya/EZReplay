package me.lagggpixel.replay.replay;

import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.replay.data.Frame;
import me.lagggpixel.replay.replay.data.Recording;
import me.lagggpixel.replay.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ReplayManager implements IReplayManager {
    @Getter
    private static IReplayManager instance;
    private final List<IRecording> replays;
    private final HashMap<String, IRecording> replayById;
    private final HashMap<World, IRecording> activeRecordings;

    private ReplayManager() {
        this.replays = new ArrayList<>();
        this.replayById = new HashMap<>();
        this.activeRecordings = new HashMap<>();
        loadReplays();
    }

    public static IReplayManager init() {
        if (instance != null) throw new IllegalStateException("ReplayManager is already initialized");
        instance = new ReplayManager();
        // Load replays from file
        return instance;
    }

    public File saveReplay(IRecording replay) {
        replayById.put(replay.getID().toString(), replay);
        return replay.toFile();
    }

    public IRecording loadReplay(UUID id) {
        File folder = new File(Replay.getInstance().getDataFolder(), "replays");
        File file = new File(folder, id + ".ezrpl");
        if (!file.exists()) {
            throw new IllegalArgumentException("Replay file not found: " + file.getAbsolutePath());
        }

        try (DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(file)))) {
            // Recreate the recording object from file
            EntityIndex index = new EntityIndex();

            short codecVersion = in.readShort(); // Read version early to validate
            long mostSigBits = in.readLong();
            long leastSigBits = in.readLong();
            String worldName = in.readUTF();

            Recording recording = new Recording(codecVersion, new UUID(mostSigBits, leastSigBits), worldName, index, new ArrayList<>());
            recording.getEntityIndex().read(in);

            int frameCount = in.readShort();
            for (int i = 0; i < frameCount; i++) {
                Frame frame = new Frame(recording);
                frame.read(in, index);
                recording.getFrames().add(frame);
            }

            int playerCount = in.readShort();
            for (int i = 0; i < playerCount; i++) {
                long msb = in.readLong();
                long lsb = in.readLong();
                recording.getPlayers().add(new UUID(msb, lsb));
            }

            int spawnLocationCount = in.readShort();
            for (int i = 0; i < spawnLocationCount; i++) {
                short entityId = in.readShort();
                double x = in.readDouble();
                double y = in.readDouble();
                double z = in.readDouble();
                float yaw = in.readFloat();
                float pitch = in.readFloat();
                recording.getSpawnLocations().put(entityId, new Vector3d(x, y, z, yaw, pitch));
            }

            int customDataCount = in.readShort();
            for (int i = 0; i < customDataCount; i++) {
                String key = in.readUTF();
                String value = in.readUTF();
                recording.getCustomData().put(key, value);
            }

            // Add to replay manager
            Replay.getInstance().getReplayManager().getReplays().add(recording);
            replayById.put(id.toString(), recording);
            Bukkit.getLogger().info("Successfully loaded replay: " + id);
            return recording;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load replay: " + id, e);
        }
    }

    public IRecording loadReplay(String id) {
        return loadReplay(UUID.fromString(id));
    }

    @Override
    public List<IRecording> getReplays() {
        return replays;
    }

    @Nullable
    public IRecording getReplayByID(String uuid) {
        return replayById.get(uuid);
    }

    @Nullable
    @Override
    public IRecording getReplayByID(UUID uuid) {
        return replayById.get(uuid.toString());
    }

    @Nullable
    public IRecording getActiveRecording(World world) {
        return activeRecordings.get(world);
    }

    @Override
    public void removeFromActiveRecordings(World world) {
        activeRecordings.remove(world);
    }

    @Override
    public IRecording startRecording(World world) {
        if (activeRecordings.containsKey(world)) return null;
        IRecording recording = new Recording(world);
        activeRecordings.put(world, recording);
        recording.start();
        return recording;
    }

    @Override
    public IRecording pauseRecording(World world) {
        if (!activeRecordings.containsKey(world)) return null;
        IRecording recording = activeRecordings.get(world);
        recording.pause();
        return recording;
    }

    @Override
    public IRecording stopRecording(World world) {
        if (!activeRecordings.containsKey(world)) return null;
        IRecording recording = activeRecordings.get(world);
        recording.stop();
        replayById.put(recording.getID().toString(), recording);
        LogUtil.info(replayById.toString());
        activeRecordings.remove(world);
        return recording;
    }

    private void loadReplays() {
        // Load all replays from file

    }
}
