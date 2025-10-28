package me.lagggpixel.replay.support.nms;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import me.lagggpixel.replay.api.IReplay;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.api.utils.block.ChunkPos;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.utils.block.BlockAction;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.support.nms.recordable.entity.entity.*;
import me.lagggpixel.replay.support.nms.recordable.entity.player.Respawn;
import me.lagggpixel.replay.support.nms.recordable.entity.item.ItemDrop;
import me.lagggpixel.replay.support.nms.recordable.entity.item.ItemMerge;
import me.lagggpixel.replay.support.nms.recordable.entity.item.ItemPick;
import me.lagggpixel.replay.support.nms.recordable.player.ChatRecordable;
import me.lagggpixel.replay.support.nms.recordable.world.ExplosionRecordable;
import me.lagggpixel.replay.support.nms.recordable.entity.player.status.Invisible;
import me.lagggpixel.replay.support.nms.recordable.entity.player.status.Sneaking;
import me.lagggpixel.replay.support.nms.recordable.entity.player.status.Sprinting;
import me.lagggpixel.replay.support.nms.recordable.entity.player.status.SwordBlock;
import me.lagggpixel.replay.support.nms.recordable.entity.status.Burning;
import me.lagggpixel.replay.support.nms.recordable.world.block.BlockInteractRecordable;
import me.lagggpixel.replay.support.nms.recordable.world.block.BlockUpdateRecordable;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.World;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class v1_8_R3 implements IVersionSupport {

    @Getter
    private static v1_8_R3 instance;

    private static IReplay plugin;
    @Getter
    private final CraftServer server;

    private static final ConcurrentHashMap<String, String[]> skinCache = new ConcurrentHashMap<>();

    public v1_8_R3(IReplay plugin) {
        this.server = (CraftServer) Bukkit.getServer();
        v1_8_R3.plugin = plugin;
        instance = this;

        //InjectorHandler.init();
    }

    public IReplay getPlugin() {
        return plugin;
    }

    public IBlockData getBlockDataToNMS(BlockCache cache) {
        net.minecraft.server.v1_8_R3.Block block = CraftMagicNumbers.getBlock(cache.getMaterial());
        return block.fromLegacyData(cache.getData());
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public Recordable createEntityStatusRecordable(IRecording replay, Entity entity) {
        return new EntityStatus(replay, entity);
    }

    @Override
    public Recordable createEntityMovementRecordable(IRecording replay, Entity entity) {
        return new EntityRecordable(replay, entity);
    }

    @Override
    public Recordable createEquipmentRecordable(IRecording replay, LivingEntity entity) {
        return new Equipment(replay, entity);
    }

    @Override
    public Recordable createBlockUpdateRecordable(IRecording recording, HashMap<ChunkPos, List<BlockCache>> cache) {
        return new BlockUpdateRecordable(recording, cache);
    }

    @Override
    public Recordable createBlockRecordable(IRecording replay, BlockCache cache, BlockAction actionType, boolean playSound) {
        return new BlockInteractRecordable(replay, cache, actionType, playSound);
    }

    @Override
    public Recordable createAnimationRecordable(IRecording replay, Entity entity, AnimationType animationType) {
        return new Animation(replay, entity, animationType);
    }

    @Override
    public Recordable createEntitySpawnRecordable(IRecording replay, Entity entity) {
        return new EntitySpawn(replay, entity);
    }

    @Override
    public Recordable createPlayerRespawnRecordable(IRecording replay, Player player) {
        return new Respawn(replay, player);
    }

    @Override
    public Recordable createEntityDeathRecordable(IRecording replay, Entity entity) {
        return new EntityDeath(replay, entity);
    }

    @Override
    public Recordable createBurningRecordable(IRecording replay, Entity entity) {
        return new Burning(replay, entity);
    }

    @Override
    public Recordable createInvisibilityRecordable(IRecording replay, Player player, boolean isInvisible) {
        return new Invisible(replay, player, isInvisible);
    }

    @Override
    public Recordable createSneakingRecordable(IRecording replay, UUID player, boolean isSneaking) {
        return new Sneaking(replay, player, isSneaking);
    }

    @Override
    public Recordable createSprintRecordable(IRecording replay, UUID player, boolean isSprinting) {
        return new Sprinting(replay, player, isSprinting);
    }

    @Override
    public Recordable createSwordBlockRecordable(IRecording replay, Player player) {
        return new SwordBlock(replay, player);
    }

    @Override
    public Recordable createChatRecordable(IRecording recording, UUID sender, String format, String content) {
        return new ChatRecordable(recording, sender, format, content);
    }

    @Override
    public Recordable createItemDropRecordable(IRecording recording, Item item) {
        return new ItemDrop(recording, item);
    }

    @Override
    public Recordable createItemPickRecordable(IRecording recording, Item item, Entity collector) {
        return new ItemPick(recording, item, collector);
    }

    @Override
    public Recordable createItemMergeRecordable(IRecording recording, Item entity, Item target) {
        return new ItemMerge(recording, entity, target);
    }

    @Override
    public Recordable createExplosionRecordable(IRecording recording, Location location, Entity entity, float radius) {
        return new ExplosionRecordable(recording, location, entity, radius);
    }

    @Override
    public Player createNPCCopy(IReplaySession replaySession, OfflinePlayer player) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        WorldServer worldServer = ((CraftWorld) replaySession.getWorld()).getHandle();
        World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getName());
        PlayerInteractManager pim = new PlayerInteractManager(world);
        EntityPlayer entityPlayer = new EntityPlayer(craftServer.getServer(), worldServer, gameProfile, pim);

        String[] skinData = this.getSkin(player.getName());
        gameProfile.getProperties().put("texture", new Property("textures", skinData[0], skinData[1]));

        return new CraftPlayer(craftServer, entityPlayer);
    }

    @Override
    public ItemStack setItemTag(ItemStack item, String key, String value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = nmsItem.getTag();
        if (nbt == null) {
            nbt = new NBTTagCompound();
        }

        nbt.setString(key, value);
        nmsItem.setTag(nbt);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public String getItemTag(ItemStack item, String key) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = nmsItem.getTag();
        if (nbt == null) {
            nbt = new NBTTagCompound();
        }
        nmsItem.setTag(nbt);
        return nbt.getString(key);
    }

    @Override
    public Material getPlayerHeadMaterial() {
        return Material.SKULL_ITEM;
    }

    @Override
    public ItemStack getSkull(String url) {
        ItemStack skull = new ItemStack(getPlayerHeadMaterial(), 1, (short) 3);

        if (url == null || url.isEmpty())
            return skull;

        ItemMeta skullMeta = skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField;

        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }

        profileField.setAccessible(true);

        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }

    @Override
    public org.bukkit.World setStatic(WorldCreator creator) {
        creator.type(creator.type());
        org.bukkit.World world = creator.createWorld();
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRuleValue("doDaylightCycle", world.getGameRuleValue("doDaylightCycle"));
        World nmsWorld = ((CraftWorld) world).getHandle();
        nmsWorld.allowAnimals = false;
        nmsWorld.allowMonsters = false;
        nmsWorld.pvpMode = false;
        return world;
    }

    @Override
    public void spawnFakePlayer(Player replayPlayer, Player player, Location location) {
        EntityPlayer entityPlayer = ((CraftPlayer) replayPlayer).getHandle();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        float yaw = location.getYaw();
        float pitch = location.getPitch();

        entityPlayer.setPositionRotation(x, y, z, yaw, pitch);
        DataWatcher watcher = entityPlayer.getDataWatcher();
        watcher.watch(10, (byte) 127);

        PacketPlayOutPlayerInfo playerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);

        sendPackets(player, playerInfo, spawn, metadata);
    }

    @Override
    public void sendActionBar(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
        sendPacket(player, packet);
    }

    @Override
    public boolean isInteractable(Material material) {
        switch (material) {
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case WOOD_DOOR:
            case WOODEN_DOOR:
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case TRAP_DOOR:
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case REDSTONE_COMPARATOR:
            case REDSTONE_WIRE:
            case DIODE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isInteractable(Block block) {
        return isInteractable(block.getType());
    }

    @Nullable
    @Override
    public ItemStack getItemInMainHand(Player p) {
        return p.getItemInHand();
    }

    @Nullable
    @Override
    public ItemStack getItemInOffHand(Player p) {
        return null;
    }

    public static void sendPacket(Player player, Packet<PacketListenerPlayOut> packet) {
        Bukkit.getScheduler().runTaskAsynchronously(getInstance().getPlugin(), () -> {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(packet);
        });

    }

    @SafeVarargs
    public static void sendPackets(Player player, Packet<PacketListenerPlayOut>... packets) {
        Bukkit.getScheduler().runTaskAsynchronously(getInstance().getPlugin(), () -> {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            for (Packet<PacketListenerPlayOut> packet : packets) {
                connection.sendPacket(packet);
            }
        });
    }

    public static void sendPackets(Player player, Iterable<Packet<PacketListenerPlayOut>> packets) {
        Bukkit.getScheduler().runTaskAsynchronously(getInstance().getPlugin(), () -> {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            for (Packet<PacketListenerPlayOut> packet : packets) {
                connection.sendPacket(packet);
            }
        });
    }

    private String[] getSkin(String name) {
        if (skinCache.containsKey(name)) {
            return skinCache.get(name);
        }

        try {
            // gets UUID from name
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = (new JsonParser()).parse(reader).getAsJsonObject().get("id").getAsString();

            // gets textures for the account
            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = (new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject());
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            String[] skinData = new String[]{texture, signature};

            skinCache.put(name, skinData);
            return skinData;

        } catch (IOException | IllegalStateException exception) {
            Bukkit.getLogger().warning("The player " + ChatColor.RED + name + ChatColor.YELLOW + " does not exist.");
            String texture = "ewogICJ0aW1lc3RhbXAiIDogMTY0MDUxODU2Njk1NiwKICAicHJvZmlsZUlkIiA6ICJlYzU2MTUzOGYzZmQ0NjFkYWZmNTA4NmIyMjE1NGJjZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbGV4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFhNGFmNzE4NDU1ZDRhYWI1MjhlN2E2MWY4NmZhMjVlNmEzNjlkMTc2OGRjYjEzZjdkZjMxOWE3MTNlYjgxMGIiCiAgICB9CiAgfQp9";
            String signature = "BchUKARlsKuXPJA7qXd2QKgnj3jR+F2EYHG5gwl4QW/+nK8Mb7MLKJDcKbKdxGRgCFfi7perJrDXZ8TpNrGxLgI+ocmjonH+ebwqv5NuRbGD0+Pkc1HCp0mq1dXnRPVgxFrlB+1pTSOnsYRJSJbLdIDvxbwL3RgQIkpKOFT7+Tpdx0VXEoHp2HCWtteAtjh1kEReHTJmnKwAzWmOU5j3Ro8e7xcuOOEG5p9CTbZyk2xxBDNHOJMq7jhPCMModKz15JdGm02r7k1al8GzdO9g0yx6GD8RlpzH0j1Ol+BHCnQ80TcrBvEOc9xgNN9q68Z2kVU7elNbXPHZYFsxalbpvwaHelDgTmx71NYfDzIqqvOY0s37kJsndWuY2bRhqNhJBFZi/SOvXFZHHhQcARGxBsizc5LKfIG3UqYHhuAJ/beErRvZLUM8hCgd5w8ISZNzPdM5pMGfe7ckaEWRRjhb7CmFHVZ9RQ+cHXGnUdSsrsDCT/gwZLIt8gHSIncE3H5m9zauhRmY2KYUZVVMKkbPB1TRfUbZdVWbEjJA7w4SXdyCN0Byh37pQl0ONvXtc5/eNRyuGHlkQj5qh/26zm/x4sawA+/7F4xfWiCib55DMLHFyXP3ooQIPmbwz+u4zLPnXymwJZG894ObapMlc1hWPmb2SbN28ZOuU1R67JwUqaI=";
            return new String[]{texture, signature};
        }
    }
}
