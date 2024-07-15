package me.lagggpixel.replay.replay.content;

import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IControls;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.support.IVersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
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

    private final HashMap<String, String> prefixes;
    private final HashMap<String, String> suffixes;
    private final HashMap<String, String> levelNames;

    private final List<IHologram> createdHolograms;

    private final List<BukkitRunnable> startedTasks;

    @Getter
    private boolean isPaused = false;
    private int currentFrameIndex = 0;
    private int speedMultiplier = 20;
    private boolean stopRecursivity = false;

    public ReplaySession(World world, UUID replayId, Player... players) {
        this.vs = Replay.getInstance().getVersionSupport();
        this.world = world;
        this.playersWatching = new ArrayList<>();
        this.playerControls = new HashMap<>();
        playersWatching.addAll(List.of(players));
        this.replay = Replay.getInstance().getReplayManager().getReplayByID(replayId);

        this.spawnedEntities = new HashMap<>();

        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        this.levelNames = new HashMap<>();

        for (String fakePlayer : replay.getPlayers()) {
            Player NPC = vs.createNPCCopy(this, Bukkit.getOfflinePlayer(UUID.fromString(fakePlayer)));
            spawnedEntities.put(fakePlayer, NPC);
            prefixes.put(fakePlayer, replay.getPrefix(fakePlayer));
            suffixes.put(fakePlayer, replay.getSuffix(fakePlayer));
            levelNames.put(fakePlayer, replay.getLevelName(fakePlayer));
        }

        this.startedTasks = new ArrayList<>();
        this.createdHolograms = new ArrayList<>();

        for (Player p : players) {
            p.teleport(new Location(world, 0, 100, 0));
            Replay.getInstance().getReplaySessionManager().setReplaySessionByPlayer(p, this);
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
            }, 20L);
        }
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), this::start, 40L);
    }

    public ReplaySession(World world, String replayId, Player... players) throws Exception {
        this.vs = Replay.getInstance().getVersionSupport();
        this.world = world;
        this.playersWatching = new ArrayList<>();
        this.playerControls = new HashMap<>();
        playersWatching.addAll(List.of(players));
        this.replay = Replay.getInstance().getReplayManager().getReplayByID(replayId);

        this.spawnedEntities = new HashMap<>();

        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        this.levelNames = new HashMap<>();

        for (String fakePlayer : replay.getPlayers()) {
            Player NPC = vs.createNPCCopy(this, Bukkit.getOfflinePlayer(UUID.fromString(fakePlayer)));
            spawnedEntities.put(fakePlayer, NPC);
            prefixes.put(fakePlayer, replay.getPrefix(fakePlayer));
            suffixes.put(fakePlayer, replay.getSuffix(fakePlayer));
            levelNames.put(fakePlayer, replay.getLevelName(fakePlayer));
        }

        this.startedTasks = new ArrayList<>();
        this.createdHolograms = new ArrayList<>();

        for (Player p : players) {
            p.teleport(new Location(world, 0, 100, 0));
            Replay.getInstance().getReplaySessionManager().setReplaySessionByPlayer(p, this);
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
            }, 20L);
        }
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), this::start, 40L);
    }

    public ReplaySession(World world, IRecording replay, Player... players) throws Exception {
        this.vs = Replay.getInstance().getVersionSupport();
        this.world = world;
        this.playersWatching = new ArrayList<>();
        this.playerControls = new HashMap<>();
        this.playersWatching.addAll(List.of(players));
        this.replay = replay;

        this.spawnedEntities = new HashMap<>();

        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        this.levelNames = new HashMap<>();

        for (String fakePlayer : replay.getPlayers()) {
            Player NPC = vs.createNPCCopy(this, Bukkit.getOfflinePlayer(UUID.fromString(fakePlayer)));
            spawnedEntities.put(fakePlayer, NPC);
            prefixes.put(fakePlayer, replay.getPrefix(fakePlayer));
            suffixes.put(fakePlayer, replay.getSuffix(fakePlayer));
            levelNames.put(fakePlayer, replay.getLevelName(fakePlayer));
        }

        this.startedTasks = new ArrayList<>();
        this.createdHolograms = new ArrayList<>();

        for (Player p : players) {
            p.teleport(new Location(world, 0, 100, 0));
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
    public String getPrefix(String UUID) {
        return prefixes.get(UUID);
    }

    @Override
    public String getSuffix(String UUID) {
        return suffixes.get(UUID);
    }

    @Override
    public String getLevelName(String UUID) {
        return levelNames.get(UUID);
    }

    @Override
    public List<BukkitRunnable> startedTasks() {
        return startedTasks;
    }

    @Override
    public List<IHologram> createdHolograms() {
        return createdHolograms;
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
                frame.play(this, p);
            }
        }

        resume();
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

        resume();
    }


    @Override
    public int getSpeed() {
        return speedMultiplier;
    }

    @Override
    public float getSpeedAsFloat() {
        float speed = speedMultiplier / 20.0f;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return Float.parseFloat(df.format(speed));
    }


    @Override
    public void setSpeed(int multiplier) {
        if (multiplier > 0) this.speedMultiplier = multiplier;
        else this.speedMultiplier = 1;
    }

    @Override
    public void resetSpeed() {
        this.speedMultiplier = 20;
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
            }

            currentFrameIndex++;
            scheduleNextFrame();
        }, delay);
    }
}
