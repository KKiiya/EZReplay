package me.lagggpixel.replay.replay.data;

import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable.RecordableSignature;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.api.utils.data.RecordingUtils;
import me.lagggpixel.replay.replay.content.ReplaySession;
import me.lagggpixel.replay.replay.tasks.EntityTrackerTask;
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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Recording implements IRecording {

    @Writeable private short CODEC_VERSION = 1;

    @Getter
    public final World world;

    private final Map<Long, List<BlockCache>> blockUpdates = new HashMap<>();
    private final Map<Long, Set<Location>> recordedBlocksPerTick = new HashMap<>();

    @Writeable private UUID id;
    @Writeable private String worldName;
    @Writeable private EntityIndex entityIndex;
    @Writeable private final List<IFrame> frames;
    @Writeable private final List<UUID> playersThatPlayed;
    @Writeable private final Map<Short, Vector3d> spawnLocations;
    @Writeable private final Map<String, String> customData;

    private final List<Entity> spawnedEntities;
    private final Map<Integer, Integer> trackedEntities = new HashMap<>();
    private final Map<Integer, Integer> trackedEquipment = new HashMap<>();
    private final String worldCloneName;
    private int frameGeneratorTaskId = -1;
    private int equipmentTrackerTaskId = -1;
    private boolean isRecording = false;
    private boolean isRecordingChat = false;
    private boolean finished = false;

    /**
     * Use this constructor to create a new recording
     * @param world the world to record
     */
    public Recording(World world) {
        this.id = UUID.randomUUID();
        this.worldCloneName = world.getName()+"-"+ id;
        this.frames = new ArrayList<>();
        this.world = world;
        this.worldName = world.getName();
        this.entityIndex = new EntityIndex();
        this.spawnedEntities = new ArrayList<>();
        this.playersThatPlayed = world.getPlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList());
        this.spawnLocations = new HashMap<>();
        this.customData = new HashMap<>();
    }

    /**
     * Use this constructor to load a recording from file
     * @param codec the codec version of the recording
     * @param id the ID of the recording
     * @param worldName the name of the world recorded
     * @param index the entity index of the recording
     * @param frames the frames of the recording
     */
    public Recording(short codec, UUID id, String worldName, EntityIndex index, List<IFrame> frames) {
        this.CODEC_VERSION = codec;
        this.id = id;
        this.world = null;
        this.worldName = worldName;
        this.entityIndex = index;
        this.frames = frames;
        this.spawnedEntities = new ArrayList<>();
        this.playersThatPlayed = new ArrayList<>();
        this.spawnLocations = new HashMap<>();
        this.customData = new HashMap<>();
        this.worldCloneName = worldName + "-" + id;
        this.finished = true;
    }

    @Override
    public short getCodecVersion() {
        return CODEC_VERSION;
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
        File folder = new File(Replay.getInstance().getDataFolder(), "replays");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Failed to create replay folder: " + folder.getAbsolutePath());
        }

        File file = new File(folder, id + ".rpl");
        try (GZIPOutputStream gzip = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)), 8192);
             DataOutputStream out = new DataOutputStream(gzip)) {
            write(out);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save replay file for ID " + id, e);
        }

        return file;
    }

    @Override
    public List<Entity> getSpawnedEntities() {
        return spawnedEntities;
    }

    @Override
    public List<UUID> getPlayers() {
        return playersThatPlayed;
    }

    @Override
    public Vector3d getSpawnLocation(short entityId) {
        return spawnLocations.get(entityId);
    }

    @Override
    public EntityIndex getEntityIndex() {
        return entityIndex;
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

            // Player handling
            for (Player player : world.getPlayers()) {
                if (!EquipmentTrackerTask.isTracked(player)) {
                    equipmentTrackerTaskId = Bukkit.getScheduler().runTaskTimer(
                        Replay.getInstance(), 
                        new EquipmentTrackerTask(this, player), 
                        0L, 5L
                    ).getTaskId();
                    trackedEquipment.put(player.getEntityId(), equipmentTrackerTaskId);
                }
                entityIndex.getOrRegister(player.getUniqueId());
                lastFrame.addRecordable(vs.createSwordBlockRecordable(this, player));
                if (player.isSneaking()) {
                    lastFrame.addRecordable(vs.createSneakingRecordable(this, player.getUniqueId(), true));
                }
                if (player.isSprinting()) {
                    lastFrame.addRecordable(vs.createSprintRecordable(this, player.getUniqueId(), true));
                }
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    lastFrame.addRecordable(vs.createInvisibilityRecordable(this, player, true));
                }
            }

            // Entity handling
            List<Entity> deadEntities = new ArrayList<>();
            for (Entity entity : getSpawnedEntities()) {
                if (entity.isDead()) {
                    deadEntities.add(entity);
                    if (EntityTrackerTask.isTracked(entity)) {
                        int taskId = trackedEntities.get(entity.getEntityId());
                        Bukkit.getScheduler().cancelTask(taskId);
                        trackedEntities.remove(entity.getEntityId());
                        EntityTrackerTask.untrackEntity(entity);
                    }
                } else {
                    entityIndex.getOrRegister(entity.getUniqueId());
                }
                
                if (!(entity instanceof Item) && !(entity instanceof Projectile)) {
                    if (!EntityTrackerTask.isTracked(entity)) {
                        int taskId = Bukkit.getScheduler().runTaskTimer(
                            Replay.getInstance(), 
                            new EntityTrackerTask(this, entity), 
                            0L, 1L
                        ).getTaskId();
                        trackedEntities.put(entity.getEntityId(), taskId);
                    }
                }
                lastFrame.addRecordable(vs.createEntityStatusRecordable(this, entity));
            }

            for (Entity entity : deadEntities) {
                lastFrame.addRecordable(vs.createEntityDeathRecordable(this, entity));
            }
            getSpawnedEntities().removeAll(deadEntities);

            if (tick > 0) {
                long previousTick = tick - 1;
                List<BlockCache> caches = blockUpdates.get(previousTick);
                
                if (caches != null && !caches.isEmpty()) {
                    caches.sort(Comparator.comparing(cache -> cache.getMaterial() == Material.AIR));
                    
                    IFrame previousFrame = frames.get(frames.size() - 2);
                    Recordable blockRecordable = vs.createBlockUpdateRecordable(this, caches);
                    previousFrame.addRecordable(blockRecordable);
                    
                    blockUpdates.remove(previousTick);
                    recordedBlocksPerTick.remove(previousTick);
                }
            }

            // Clean up old tracking data
            if (tick % 100 == 0) {
                recordedBlocksPerTick.keySet().removeIf(t -> t < tick - 100);
            }
            
        }, 0, 1L).getTaskId();

        // Initial entity spawn
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
            IFrame lastFrame = getLastFrame();
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;
                if (entity instanceof Item) continue;
                spawnedEntities.add(entity);
                lastFrame.addRecordable(vs.createEntitySpawnRecordable(this, entity));
            }
        }, 5L);

        // Record spawn locations
        for (Player p : world.getPlayers()) {
            spawnLocations.put(
                entityIndex.getOrRegister(p.getUniqueId()), 
                Vector3d.fromBukkitLocation(p.getLocation())
            );
        }
    }

    @Override
    public void pause() {
        isRecording = false;
        Bukkit.getScheduler().cancelTask(frameGeneratorTaskId);
        Bukkit.getScheduler().cancelTask(equipmentTrackerTaskId);
        frameGeneratorTaskId = -1;
        equipmentTrackerTaskId = -1;
        for (Player player : world.getPlayers()) {
            int taskId = trackedEquipment.get(player.getEntityId());
            Bukkit.getScheduler().cancelTask(taskId);
            trackedEquipment.remove(player.getEntityId());
            EquipmentTrackerTask.untrack(player);

        }

        for (Entity entity : getSpawnedEntities()) {
            if (EntityTrackerTask.isTracked(entity)) {
                int taskId = trackedEntities.get(entity.getEntityId());
                Bukkit.getScheduler().cancelTask(taskId);
                trackedEntities.remove(entity.getEntityId());
                EntityTrackerTask.untrackEntity(entity);
            }
        }
    }

    @Override
    public void stop() {
        isRecording = false;
        finished = true;
        Bukkit.getScheduler().cancelTask(frameGeneratorTaskId);
        Bukkit.getScheduler().cancelTask(equipmentTrackerTaskId);
        frameGeneratorTaskId = -1;
        equipmentTrackerTaskId = -1;
        for (Player player : world.getPlayers()) {
            int taskId = trackedEquipment.get(player.getEntityId());
            Bukkit.getScheduler().cancelTask(taskId);
            trackedEquipment.remove(player.getEntityId());
            EquipmentTrackerTask.untrack(player);

        }

        for (Entity entity : getSpawnedEntities()) {
            if (EntityTrackerTask.isTracked(entity)) {
                int taskId = trackedEntities.get(entity.getEntityId());
                Bukkit.getScheduler().cancelTask(taskId);
                trackedEntities.remove(entity.getEntityId());
                EntityTrackerTask.untrackEntity(entity);
            }
        }
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
        // Deduplication checkç
        recordedBlocksPerTick.putIfAbsent(tick, new HashSet<>());
        Location loc = block.getLocation();
        
        if (!recordedBlocksPerTick.get(tick).add(loc)) return;
        
        blockUpdates.putIfAbsent(tick, new ArrayList<>());
        blockUpdates.get(tick).add(new BlockCache(block));
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

    @Override
    public Map<Short, Vector3d> getSpawnLocations() {
        return spawnLocations;
    }

    @Override
    public Map<String, String> getCustomData() {
        return customData;
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

    
    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeShort(CODEC_VERSION);

        // Core metadata
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
        out.writeUTF(worldName);

        // Entity index
        entityIndex.write(out);

        // OPTIMIZATION: Write frame count and use delta encoding
        out.writeInt(frames.size());
        
        // Track previous frame's recordables for delta encoding
        Set<Recordable.RecordableSignature> previousRecordables = new HashSet<>();
        
        for (int i = 0; i < frames.size(); i++) {
            IFrame frame = frames.get(i);
            
            // Get current frame's recordables
            Set<Recordable.RecordableSignature> currentRecordables = RecordingUtils.getRecordableSignatures(frame);
            
            // Calculate additions and removals
            Set<Recordable.RecordableSignature> additions = new HashSet<>(currentRecordables);
            additions.removeAll(previousRecordables);

            Set<Recordable.RecordableSignature> removals = new HashSet<>(previousRecordables);
            removals.removeAll(currentRecordables);
            
            // Write delta frame
            RecordingUtils.writeDeltaFrame(out, frame, additions, removals);

            // Update previous state
            previousRecordables = currentRecordables;
        }

        // Players (unchanged - already efficient)
        out.writeInt(playersThatPlayed.size());
        for (UUID playerId : playersThatPlayed) {
            out.writeLong(playerId.getMostSignificantBits());
            out.writeLong(playerId.getLeastSignificantBits());
        }

        // OPTIMIZATION: Use VarInt for spawn locations to save space
        RecordingUtils.writeVarInt(out, spawnLocations.size());
        for (Map.Entry<Short, Vector3d> entry : spawnLocations.entrySet()) {
            out.writeShort(entry.getKey());
            Vector3d loc = entry.getValue();
            
            // Use float instead of double (4 bytes vs 8 bytes each)
            out.writeFloat((float) loc.getX());
            out.writeFloat((float) loc.getY());
            out.writeFloat((float) loc.getZ());
            out.writeFloat(loc.getYaw());
            out.writeFloat(loc.getPitch());
        }

        // Custom data (use compression-friendly format)
        RecordingUtils.writeVarInt(out, customData.size());
        for (Map.Entry<String, String> entry : customData.entrySet()) {
            RecordingUtils.writeCompressedString(out, entry.getKey());
            if (entry.getValue() != null) {
                out.writeBoolean(true);
                RecordingUtils.writeCompressedString(out, entry.getValue());
            } else {
                out.writeBoolean(false);
            }
        }
    }


    @Override
    public void read(DataInputStream in, EntityIndex index) throws IOException {
        CODEC_VERSION = in.readShort();

        this.id = new UUID(in.readLong(), in.readLong());
        this.worldName = in.readUTF();

        // Entity index
        index.read(in);
        this.entityIndex = index;

        // Frames with delta decoding
        frames.clear();
        int frameCount = in.readInt();
        
        Set<RecordableSignature> currentState = new HashSet<>();
        
        for (int i = 0; i < frameCount; i++) {
            Frame frame = new Frame(this);

            int addCount = RecordingUtils.readVarInt(in);
            int removeCount = RecordingUtils.readVarInt(in);

            // Read additions
            for (int j = 0; j < addCount; j++) {
                short typeId = in.readShort();
                Recordable recordable = RecordableRegistry.create(typeId, in, index);
                recordable.read(in, index);
                frame.addRecordable(recordable);
                
                currentState.add(new RecordableSignature(recordable));
            }
            
            // Read removals
            for (int j = 0; j < removeCount; j++) {
                short typeId = in.readShort();
                RecordableSignature sig = RecordableSignature.readIdentifier(in, typeId);
                currentState.remove(sig);
            }
            
            frames.add(frame);
        }

        // Players
        playersThatPlayed.clear();
        int playerCount = in.readInt();
        for (int i = 0; i < playerCount; i++) {
            playersThatPlayed.add(new UUID(in.readLong(), in.readLong()));
        }

        // Spawn locations
        spawnLocations.clear();
        int spawnLocationCount = RecordingUtils.readVarInt(in);
        for (int i = 0; i < spawnLocationCount; i++) {
            short entityId = in.readShort();
            float x = in.readFloat();
            float y = in.readFloat();
            float z = in.readFloat();
            float yaw = in.readFloat();
            float pitch = in.readFloat();
            spawnLocations.put(entityId, new Vector3d(x, y, z, yaw, pitch));
        }

        // Custom data
        customData.clear();
        int customDataCount = RecordingUtils.readVarInt(in);
        for (int i = 0; i < customDataCount; i++) {
            String key = RecordingUtils.readCompressedString(in);
            if (in.readBoolean()) {
                customData.put(key, RecordingUtils.readCompressedString(in));
            } else {
                customData.put(key, null);
            }
        }
    }
}
