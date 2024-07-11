package me.lagggpixel.replay.replay.content;

import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IHologramAdd;
import me.lagggpixel.replay.api.utils.entity.ReplayEntity;
import me.lagggpixel.replay.api.utils.entity.player.ReplayPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ReplaySession implements IReplaySession {
    @Getter
    private final World world;
    @Getter
    private final List<Player> playersWatching;
    private final IRecording replay;

    private final HashMap<String, ReplayEntity> fakeEntitiesByUUID;
    private final HashMap<Integer, ReplayEntity> fakeEntitiesById;

    private final HashMap<String, ReplayPlayer> fakePlayersByUUID;

    private final List<BukkitRunnable> startedTasks;

    public ReplaySession(World world, UUID replayId, Player... players) throws Exception {
        this.world = world;
        this.playersWatching = new ArrayList<>();
        playersWatching.addAll(List.of(players));
        this.replay = Replay.getInstance().getReplayManager().getReplayByID(replayId);

        this.fakeEntitiesByUUID = new HashMap<>();
        this.fakeEntitiesById = new HashMap<>();
        this.fakePlayersByUUID = new HashMap<>();
        this.startedTasks = new ArrayList<>();
    }

    public ReplaySession(World world, String replayId, Player... players) throws Exception {
        this.world = world;
        this.playersWatching = new ArrayList<>();
        playersWatching.addAll(List.of(players));
        this.replay = Replay.getInstance().getReplayManager().getReplayByID(replayId);

        this.fakeEntitiesByUUID = new HashMap<>();
        this.fakeEntitiesById = new HashMap<>();
        this.fakePlayersByUUID = new HashMap<>();
        this.startedTasks = new ArrayList<>();
    }

    public ReplaySession(World world, IRecording replay, Player... players) throws Exception {
        this.world = world;
        this.playersWatching = new ArrayList<>();
        playersWatching.addAll(List.of(players));
        this.replay = replay;

        this.fakeEntitiesByUUID = new HashMap<>();
        this.fakeEntitiesById = new HashMap<>();
        this.fakePlayersByUUID = new HashMap<>();
        this.startedTasks = new ArrayList<>();
    }


    @Override
    public IRecording getReplay() {
        return replay;
    }

    @Override
    public List<Player> getViewers() {
        return List.of();
    }

    @Override
    public ReplayEntity getFakeEntity(String UUID) {
        return fakeEntitiesByUUID.get(UUID);
    }

    @Override
    public ReplayEntity getFakeEntity(int entityId) {
        return fakeEntitiesById.get(entityId);
    }

    @Override
    public void addFakeEntity(ReplayEntity entity) {
        fakeEntitiesById.put(entity.getEntityId(), entity);
    }

    @Override
    public ReplayPlayer getFakePlayer(String UUID) {
        return fakePlayersByUUID.get(UUID);
    }

    @Override
    public List<BukkitRunnable> startedTasks() {
        return startedTasks;
    }

    @Override
    public List<IHologramAdd> createdHolograms() {
        return List.of();
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void rewind(int seconds) {

    }

    @Override
    public void fastForward(int seconds) {

    }

    @Override
    public void end() {

    }
}
