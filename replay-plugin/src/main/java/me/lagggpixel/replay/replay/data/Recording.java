package me.lagggpixel.replay.replay.data;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.chat.IChat;
import com.tomkeuper.bedwars.api.levels.Level;
import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.replay.content.ReplaySession;
import me.lagggpixel.replay.replay.tasks.EquipmentTrackerTask;
import me.lagggpixel.replay.utils.LogUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Recording implements IRecording {
    @Getter
    public final UUID id;
    @Getter
    public final IArena arena;

    private final List<IFrame> frames;
    private final List<Entity> spawnedEntities;
    private final List<Item> droppedItems;
    private final List<String> playersThatPlayed;
    private final String worldCloneName;
    private final HashMap<String, TeamColor> playersTeamColor;
    private final HashMap<String, String> playersPrefix;
    private final HashMap<String, String> playersSuffix;
    private final HashMap<String, String> playersLevelName;
    private final HashMap<String, Location> spawnLocations;
    private int frameGeneratorTaskId = -1;
    private int equipmentTrackerTaskId = -1;
    private boolean isRecording = false;
    private boolean finished = false;

    public Recording(IArena arena) {
        this.worldCloneName = arena.getWorldName();
        this.frames = new ArrayList<>();
        this.id = UUID.randomUUID();
        this.arena = arena;
        this.spawnedEntities = new ArrayList<>();
        this.droppedItems = new ArrayList<>();
        this.playersThatPlayed = arena.getPlayers().stream().map(p -> p.getUniqueId().toString()).collect(Collectors.toList());
        this.playersTeamColor = new HashMap<>();
        this.playersPrefix = new HashMap<>();
        this.playersSuffix = new HashMap<>();
        this.playersLevelName = new HashMap<>();
        this.spawnLocations = new HashMap<>();

        IChat chatUtil = Replay.getInstance().getBedWarsAPI().getChatUtil();
        Level levelUtil = Replay.getInstance().getBedWarsAPI().getLevelsUtil();
        for (Player player : arena.getPlayers()) {
            playersTeamColor.put(player.getUniqueId().toString(), arena.getTeam(player).getColor());
            playersPrefix.put(player.getUniqueId().toString(), chatUtil.getPrefix(player));
            playersSuffix.put(player.getUniqueId().toString(), chatUtil.getSuffix(player));
            playersLevelName.put(player.getUniqueId().toString(), levelUtil.getLevel(player));
        }
    }

    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public void add(IFrame... frames) {
        this.frames.addAll(List.of(frames));
    }

    @Override
    public void add(List<IFrame> frames) {
        this.frames.addAll(frames);
    }

    @Override
    public IFrame getFrame(int tick) {
        return frames.get(tick);
    }

    @Override
    public IFrame getLastFrame() {
        return frames.get(frames.size() - 1);
    }

    @Override
    public IFrame getPreviousFrame() {
        return frames.get(frames.size() - 2);
    }

    @Override
    public List<IFrame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    @Override
    public File toFile() {
        return null;
    }

    @Override
    public List<Entity> getSpawnedEntities() {
        return spawnedEntities;
    }

    @Override
    public List<Item> getDroppedItems() {
        return droppedItems;
    }

    @Override
    public List<String> getPlayers() {
        return playersThatPlayed;
    }

    @Override
    public TeamColor getTeamColor(String player) {
        return playersTeamColor.get(player);
    }

    @Override
    public String getPrefix(String player) {
        return playersPrefix.get(player);
    }

    @Override
    public String getSuffix(String player) {
        return playersSuffix.get(player);
    }

    @Override
    public String getLevelName(String player) {
        return playersLevelName.get(player);
    }

    @Override
    public Location getSpawnLocation(String offlinePlayer) {
        return spawnLocations.get(offlinePlayer);
    }

    @Override
    public void start() {
        IVersionSupport vs = Replay.getInstance().getVersionSupport();
        LogUtil.info("STARTED RECORDING REPLAY WITH ID " + id.toString());
        isRecording = true;
        if (finished) throw new UnsupportedOperationException("Tried resuming replay with ID '" + id + "' while finished");

        frameGeneratorTaskId = Bukkit.getScheduler().runTaskTimer(Replay.getInstance(), () -> {
            frames.add(new Frame(this));

            for (Player player : arena.getPlayers()) {
                if (!player.isBlocking()) getLastFrame().addRecordable(vs.createSwordBlockRecordable(this, player));
                getLastFrame().addRecordable(vs.createEntityMovementRecordable(this, player));
                getLastFrame().addRecordable(vs.createSneakingRecordable(this, player));
                getLastFrame().addRecordable(vs.createSprintRecordable(this, player));
            }

            List<Entity> deadEntities = new ArrayList<>();
            for (Entity entity : getSpawnedEntities()) {
                if (entity.isDead()) deadEntities.add(entity);
                if (!(entity instanceof Item) && !(entity instanceof Projectile)) {
                    getLastFrame().addRecordable(vs.createEntityMovementRecordable(this, entity));
                }
                getLastFrame().addRecordable(vs.createEntityStatusRecordable(this, entity));
            }
            getSpawnedEntities().removeAll(deadEntities);
        }, 0, 1L).getTaskId();

        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
            for (IGenerator gen : arena.getOreGenerators()) {
                if (gen.getHologramHolder() == null) continue;
                getLastFrame().addRecordable(vs.createGeneratorRecordable(this, gen));
            }
        }, 40L);

        for (ITeam team : arena.getTeams()) {
            if (!team.isShopSpawned()) continue;

        }

        for (Player p : arena.getPlayers()) {
            spawnLocations.put(p.getUniqueId().toString(), p.getLocation());
        }
        equipmentTrackerTaskId = Bukkit.getScheduler().runTaskTimer(Replay.getInstance(), new EquipmentTrackerTask(this), 0, 1L).getTaskId();
    }

    @Override
    public void pause() {
        isRecording = false;
        Bukkit.getScheduler().cancelTask(frameGeneratorTaskId);
        Bukkit.getScheduler().cancelTask(equipmentTrackerTaskId);
        frameGeneratorTaskId = -1;
        equipmentTrackerTaskId = -1;
    }

    @Override
    public void stop() {
        LogUtil.info("FINISHED RECORDING WITH ID " + id.toString());
        isRecording = false;
        finished = true;
        Bukkit.getScheduler().cancelTask(frameGeneratorTaskId);
        Bukkit.getScheduler().cancelTask(equipmentTrackerTaskId);
        frameGeneratorTaskId = -1;
        Replay.getInstance().getReplayManager().getReplays().add(this);
        Replay.getInstance().getReplayManager().removeFromActiveReplays(arena);
    }

    @Override
    public Entity getSpawnedEntity(int id) {
        return spawnedEntities.stream().filter(e -> e.getEntityId() == id).collect(Collectors.toList()).get(0);
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public IReplaySession watch(Player player) {
        WorldCreator creator = new WorldCreator(worldCloneName);
        World worldClone = Bukkit.createWorld(creator);
        return new ReplaySession(worldClone, id, player);
    }

    @Override
    public IReplaySession watch(Player... players) {
        WorldCreator creator = new WorldCreator(worldCloneName);
        World worldClone = Bukkit.createWorld(creator);
        return new ReplaySession(worldClone, id, players);
    }
}
