package me.lagggpixel.replay.replay.content;

import lombok.Getter;
import lombok.NonNull;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IControls;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ReplaySession implements IReplaySession {
    private final IVersionSupport vs;

    @Getter
    private final World world;
    @Getter
    private final List<Player> playersWatching;
    private final IRecording replay;
    private final HashMap<Player, IControls> playerControls;

    @Getter
    private final HashMap<String, Entity> spawnedEntities;

    private final List<BukkitRunnable> startedTasks;

    @Getter
    private boolean isPaused = false;
    private int currentFrameIndex = 0;
    private int speedMultiplier = 20;

    public ReplaySession(World world, UUID replayId, @NonNull Location spawnLocation, Player... players) {
        this(world, replayId.toString(), spawnLocation, players);
    }

    public ReplaySession(World world, String replayId, Location spawnLocation, Player... players) {
        this.vs = Replay.getInstance().getVersionSupport();
        this.world = world;
        this.playersWatching = new ArrayList<>();
        this.playerControls = new HashMap<>();
        playersWatching.addAll(List.of(players));
        this.replay = Replay.getInstance().getReplayManager().getReplayByID(replayId);
        if (replay == null) throw new NullPointerException("Tried loading replay with ID '" + replayId + "'. Replay doesn't exist");

        this.spawnedEntities = new HashMap<>();

        for (String player : replay.getPlayers()) {
            Player NPC = vs.createNPCCopy(this, Bukkit.getOfflinePlayer(UUID.fromString(player)));
            spawnedEntities.put(player, NPC);
        }

        this.startedTasks = new ArrayList<>();

        for (Player p : players) {
            Bukkit.getScheduler().runTask(Replay.getInstance(), () -> p.teleport(spawnLocation));
            Replay.getInstance().getReplaySessionManager().setReplaySessionByPlayer(p, this);
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
            }, 20L);
        }
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), this::start, 40L);
    }

    public ReplaySession(World world, IRecording replay, Player... players) {
        this.vs = Replay.getInstance().getVersionSupport();
        this.world = world;
        this.playersWatching = new ArrayList<>();
        this.playerControls = new HashMap<>();
        this.playersWatching.addAll(List.of(players));
        this.replay = replay;

        this.spawnedEntities = new HashMap<>();

        for (String player : replay.getPlayers()) {
            Player NPC = vs.createNPCCopy(this, Bukkit.getOfflinePlayer(UUID.fromString(player)));
            spawnedEntities.put(player, NPC);
        }

        this.startedTasks = new ArrayList<>();

        for (Player p : players) {
            Bukkit.getScheduler().runTask(Replay.getInstance(), () -> p.teleport(new Location(world, 0, 180, 0)));
            Replay.getInstance().getReplaySessionManager().setReplaySessionByPlayer(p, this);
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
            }, 20L);
        }
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), this::start, 40L);
    }


    @Override
    public IRecording getReplay() {
        return replay;
    }

    @Override
    public List<Player> getViewers() {
        return playersWatching;
    }

    @Override
    public HashMap<Player, IControls> getPlayerControls() {
        return playerControls;
    }

    @Override
    public List<BukkitRunnable> startedTasks() {
        return startedTasks;
    }

    @Override
    public int getCurrentTick() {
        return currentFrameIndex;
    }

    @Override
    public void start() {
        for (Player p : getViewers()) {
            playerControls.put(p, new Controls(this, p));
        }
        for (String replayPlayer : spawnedEntities.keySet()) {
            if (!(spawnedEntities.get(replayPlayer) instanceof Player)) continue;
            Player fakePlayer = (Player) spawnedEntities.get(replayPlayer);
            for (Player viewer : getViewers()) {
                vs.spawnFakePlayer(fakePlayer, viewer, replay.getSpawnLocation(replayPlayer));
            }
        }

        isPaused = false;
        currentFrameIndex = 0;  // Reset to the beginning
        scheduleNextFrame();
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {
        if (isPaused) {
            isPaused = false;
            scheduleNextFrame();
        }
    }

    @Override
    public void rewind(int seconds) {
        pause();
        int framesToRewind = seconds * 20;
        int targetFrameIndex = Math.max(currentFrameIndex - framesToRewind, 0);

        while (currentFrameIndex > targetFrameIndex) {
            IFrame frame = replay.getFrames().get(--currentFrameIndex);
            for (Player p : getViewers()) {
                frame.unplay(this, p);
            }
        }

        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), this::resume, 20L);
    }

    @Override
    public void fastForward(int seconds) {
        pause();
        int framesToSkip = seconds * 20;
        int targetFrameIndex = Math.min(currentFrameIndex + framesToSkip, replay.getFrames().size() - 1);

        while (currentFrameIndex < targetFrameIndex) {
            IFrame frame = replay.getFrames().get(currentFrameIndex++);
            for (Player p : getViewers()) {
                frame.play(this, p);
            }
        }

        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), this::resume, 20L);
    }


    @Override
    public int getSpeed() {
        return speedMultiplier;
    }

    @Override
    public double getSpeedAsDouble() {
        double speed = speedMultiplier / 20.0f;
        return Utils.round(speed, 2);
    }


    @Override
    public void setSpeed(int multiplier) {
        this.speedMultiplier = Math.max(multiplier, 5);
    }

    @Override
    public void resetSpeed() {
        this.speedMultiplier = 20;
    }

    @Override
    public void reset() {
        pause();
        this.currentFrameIndex = 0;

        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), this::resume, 20L);
    }

    @Override
    public void end() {
        isPaused = true;
        currentFrameIndex = 0;  // Optionally reset to the beginning
    }

    private void scheduleNextFrame() {
        if (isPaused || currentFrameIndex >= replay.getFrames().size()) return;
        IFrame frame = replay.getFrames().get(currentFrameIndex);
        int delay = (int) (20 / speedMultiplier);

        if (delay < 1) delay = 1;

        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
            if (isPaused) return;

            for (Player p : getViewers()) {
                frame.play(this, p);
                p.setLevel(currentFrameIndex/20);
                p.setExp((float) currentFrameIndex/replay.getFrames().size());
            }

            currentFrameIndex++;
            scheduleNextFrame();
        }, delay);
    }
}
