package me.lagggpixel.replay.replay.data;

import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.replay.content.ReplaySession;
import me.lagggpixel.replay.replay.tasks.EquipmentTrackerTask;
import me.lagggpixel.replay.utils.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Recording implements IRecording {
    @Getter
    @Writeable
    public final UUID id;

    @Getter
    public final World world;

    private final HashMap<Long, HashMap<Chunk, List<BlockCache>>> blockUpdates = new HashMap<>();

    @Writeable
    private final List<IFrame> frames;
    @Writeable
    private final List<Entity> spawnedEntities;
    @Writeable
    private final List<String> playersThatPlayed;
    @Writeable
    private final HashMap<String, Location> spawnLocations;
    @Writeable
    private final HashMap<String, Object> customData;
    private final String worldCloneName;
    private int frameGeneratorTaskId = -1;
    private int equipmentTrackerTaskId = -1;
    private boolean isRecording = false;
    private boolean isRecordingChat = false;
    private boolean finished = false;

    public Recording(World world) {
        this.id = UUID.randomUUID();
        this.worldCloneName = world.getName()+"-"+ id;
        this.frames = new ArrayList<>();
        this.world = world;
        this.spawnedEntities = new ArrayList<>();
        this.playersThatPlayed = world.getPlayers().stream().map(p -> p.getUniqueId().toString()).collect(Collectors.toList());
        this.spawnLocations = new HashMap<>();
        this.customData = new HashMap<>();
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
    public IFrame getFrame(long tick) {
        if (tick < 0 || tick >= frames.size()) {
            throw new IllegalArgumentException("Tick index out of bounds");
        }
        return frames.get((int) tick);
    }

    @Override
    public long getFrameTick(IFrame frame) {
        return frames.indexOf(frame);
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
    public List<String> getPlayers() {
        return playersThatPlayed;
    }

    @Override
    public Location getSpawnLocation(String offlinePlayer) {
        return spawnLocations.get(offlinePlayer);
    }

    @Override
    public void start() {
        IVersionSupport vs = Replay.getInstance().getVersionSupport();
        isRecording = true;
        if (finished) throw new UnsupportedOperationException("Tried resuming replay with ID '" + id + "' while finished");

        frameGeneratorTaskId = Bukkit.getScheduler().runTaskTimer(Replay.getInstance(), () -> {
            frames.add(new Frame(this));
            IFrame lastFrame = getLastFrame();
            long tick = getFrameTick(lastFrame);

            for (Player player : world.getPlayers()) {
                lastFrame.addRecordable(vs.createEntityMovementRecordable(this, player));
                lastFrame.addRecordable(vs.createSwordBlockRecordable(this, player));
                if (player.isSneaking()) lastFrame.addRecordable(vs.createSneakingRecordable(this, player.getUniqueId(), true));
                if (player.isSprinting()) lastFrame.addRecordable(vs.createSprintRecordable(this, player.getUniqueId(), true));
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) lastFrame.addRecordable(vs.createInvisibilityRecordable(this, player, true));
            }

            List<Entity> deadEntities = new ArrayList<>();
            for (Entity entity : getSpawnedEntities()) {
                if (entity.isDead()) deadEntities.add(entity);
                if (!(entity instanceof Item) && !(entity instanceof Projectile)) {
                    lastFrame.addRecordable(vs.createEntityMovementRecordable(this, entity));
                }
                lastFrame.addRecordable(vs.createEntityStatusRecordable(this, entity));
            }
            for (Entity entity : deadEntities) {
                lastFrame.addRecordable(vs.createEntityDeathRecordable(this, entity));
            }
            getSpawnedEntities().removeAll(deadEntities);


            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
                HashMap<Chunk, List<BlockCache>> chunkUpdates = blockUpdates.get(tick);
                if (chunkUpdates != null) {
                    lastFrame.addRecordable(vs.createBlockUpdateRecordable(this, chunkUpdates));
                }
            }, 1L);
        }, 0, 1L).getTaskId();

        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
            IFrame lastFrame = getLastFrame();
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;
                if (entity instanceof Item) continue;
                spawnedEntities.add(entity);
                lastFrame.addRecordable(vs.createEntitySpawnRecordable(this, entity));
            }
        }, 5L);

        for (Player p : world.getPlayers()) {
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
        isRecording = false;
        finished = true;
        Bukkit.getScheduler().cancelTask(frameGeneratorTaskId);
        Bukkit.getScheduler().cancelTask(equipmentTrackerTaskId);
        frameGeneratorTaskId = -1;
        equipmentTrackerTaskId = -1;
        Replay.getInstance().getReplayManager().getReplays().add(this);
        Replay.getInstance().getReplayManager().removeFromActiveRecordings(world);
    }

    @Override
    public Entity getSpawnedEntity(int id) {
        return spawnedEntities.stream()
                .filter(e -> e.getEntityId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Entity with ID " + id + " not found"));
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
    public boolean isRecordingChat() {
        return isRecordingChat;
    }

    @Override
    public void setRecordingChat(boolean value) {
        this.isRecordingChat = value;
    }

    @Override
    public void addBlockUpdate(IFrame frame, Block block) {
        long tick = getFrameTick(frame);
        addBlockUpdate(tick, block);
    }

    @Override
    public void addBlockUpdate(long tick, Block block) {
        blockUpdates.putIfAbsent(tick, new HashMap<>());
        HashMap<Chunk, List<BlockCache>> chunkUpdates = blockUpdates.get(tick);
        Chunk chunk = block.getChunk();
        chunkUpdates.putIfAbsent(chunk, new ArrayList<>());
        chunkUpdates.get(chunk).add(new BlockCache(block));
    }

    @Override
    public IReplaySession watch(Player player) {
        if (!FileUtils.isWorldCached(world)) {
            FileUtils.saveWorldToCache(world);  // Ensure the world is saved to cache
        }
        loadWorldAsyncAndTeleport(player);
        return null;  // Returning null initially, as the actual session will start post-load.
    }

    @Override
    public IReplaySession watch(Player... players) {
        if (!FileUtils.isWorldCached(world)) {
            FileUtils.saveWorldToCache(world);  // Ensure the world is saved to cache
        }
        loadWorldAsyncAndTeleport(players);
        return null;
    }

    private void loadWorldAsyncAndTeleport(Player... players) {
        // Asynchronous task for unzipping
        new BukkitRunnable() {
            @Override
            public void run() {
                FileUtils.decompressWorldFromCache(world, world.getName() + "-" + id);  // Decompress asynchronously

                // Back to main thread to load the world and teleport players
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        WorldCreator creator = new WorldCreator(worldCloneName);
                        World worldClone = Replay.getInstance().getVersionSupport().setStatic(creator);
                        worldClone.setAutoSave(false);
                        worldClone.getEntities().forEach(Entity::remove);

                        for (Player player : players) {
                            new ReplaySession(worldClone, id, worldClone.getSpawnLocation(), player);
                        }
                    }

                }.runTask(Replay.getInstance()); // Run on the main server thread
            }
        }.runTaskAsynchronously(Replay.getInstance());
    }
}
