package me.lagggpixel.replay.support.nms;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import lombok.Getter;
import me.lagggpixel.replay.api.IReplay;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.support.nms.recordable.arena.*;
import me.lagggpixel.replay.support.nms.recordable.arena.specials.EggBridgeRecordable;
import me.lagggpixel.replay.support.nms.recordable.world.ExplosionRecordable;
import me.lagggpixel.replay.support.nms.recordable.arena.specials.PopUpTowerRecordable;
import me.lagggpixel.replay.support.nms.recordable.arena.specials.TntRecordable;
import me.lagggpixel.replay.support.nms.recordable.entity.EntityRecordable;
import me.lagggpixel.replay.support.nms.recordable.entity.EntityStatus;
import me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.status.Invisible;
import me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.status.Sneaking;
import me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.status.Sprinting;
import me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.status.SwordBlock;
import me.lagggpixel.replay.support.nms.recordable.entity.recordables.*;
import me.lagggpixel.replay.support.nms.recordable.entity.recordables.Equipment;
import me.lagggpixel.replay.support.nms.recordable.entity.recordables.status.Burning;
import me.lagggpixel.replay.support.nms.recordable.world.block.BlockRecordable;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.UUID;

public class v1_8_R3 implements IVersionSupport {
    @Getter
    private static v1_8_R3 instance;
    private static IReplay plugin;
    @Getter
    private final CraftServer server;

    public v1_8_R3(IReplay plugin) {
        this.server = (CraftServer) Bukkit.getServer();
        v1_8_R3.plugin = plugin;
        instance = this;

        //InjectorHandler.init();
    }

    public IReplay getPlugin() {
        return plugin;
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
    public Recordable createBlockRecordable(IRecording replay, org.bukkit.World world, org.bukkit.Material material, byte data, Location location, BlockAction actionType, boolean playSound) {
        return new BlockRecordable(replay, world, material, data, location, actionType, playSound);
    }

    @Override
    public Recordable createAnimationRecordable(IRecording replay, Entity entity, AnimationType animationType) {
        return new AnimationRecordable(replay, entity, animationType);
    }

    @Override
    public Recordable createEntitySpawnRecordable(IRecording replay, Entity entity) {
        return new EntitySpawn(replay, entity);
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
    public Recordable createSneakingRecordable(IRecording replay, Player player) {
        return new Sneaking(replay, player);
    }

    @Override
    public Recordable createSprintRecordable(IRecording replay, Player player) {
        return new Sprinting(replay, player);
    }

    @Override
    public Recordable createSwordBlockRecordable(IRecording replay, Player player) {
        return new SwordBlock(replay, player);
    }

    @Override
    public Recordable createPopUpTowerRecordable(IRecording replay, Block block, Sound sound, float volume, float pitch) {
        return new PopUpTowerRecordable(replay, block, sound, volume, pitch);
    }

    @Override
    public Recordable createEggBridgeRecordable(IRecording replay, Block block, Sound sound, float volume, float pitch) {
        return new EggBridgeRecordable(replay, block, sound, volume, pitch);
    }

    @Override
    public Recordable createHologramRecordable(IRecording replay, IHologram hologram) {
        return new HologramAddRecordable(replay, hologram);
    }

    @Override
    public Recordable createGeneratorRecordable(IRecording replay, IGenerator generator) {
        return new GeneratorAddRecordable(replay, generator);
    }

    @Override
    public Recordable createChatRecordable(IRecording replay, UUID sender, String content) {
        return new ChatRecordable(replay, sender, content);
    }

    @Override
    public Recordable createItemDropRecordable(IRecording replay, Item item) {
        return new ItemDropRecordable(replay, item);
    }

    @Override
    public Recordable createItemPickRecordable(IRecording replay, Item item, Entity collector) {
        return new ItemPickRecordable(replay, item, collector);
    }

    @Override
    public Recordable createTntSpawnRecordable(IRecording recording, Location location) {
        return new TntRecordable(recording, location);
    }

    @Override
    public Recordable createExplosionRecordable(IRecording replay, Location location, float radius) {
        return new ExplosionRecordable(replay, location, radius);
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

    public static void sendPacket(Player player,Packet<PacketListenerPlayOut> packet) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(packet);
    }

    @SafeVarargs
    public static void sendPackets(Player player, Packet<PacketListenerPlayOut>... packets) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (Packet<PacketListenerPlayOut> packet : packets) {
            connection.sendPacket(packet);
        }
    }

    private String[] getSkin(String name) {
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
            return new String[] {texture, signature};

        } catch (IOException | IllegalStateException exception) {

            Bukkit.getLogger().warning("The player " + ChatColor.RED + name + ChatColor.YELLOW + " does not exist.");
            String texture = "ewogICJ0aW1lc3RhbXAiIDogMTY0MDUxODU2Njk1NiwKICAicHJvZmlsZUlkIiA6ICJlYzU2MTUzOGYzZmQ0NjFkYWZmNTA4NmIyMjE1NGJjZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbGV4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFhNGFmNzE4NDU1ZDRhYWI1MjhlN2E2MWY4NmZhMjVlNmEzNjlkMTc2OGRjYjEzZjdkZjMxOWE3MTNlYjgxMGIiCiAgICB9CiAgfQp9";
            String signature = "BchUKARlsKuXPJA7qXd2QKgnj3jR+F2EYHG5gwl4QW/+nK8Mb7MLKJDcKbKdxGRgCFfi7perJrDXZ8TpNrGxLgI+ocmjonH+ebwqv5NuRbGD0+Pkc1HCp0mq1dXnRPVgxFrlB+1pTSOnsYRJSJbLdIDvxbwL3RgQIkpKOFT7+Tpdx0VXEoHp2HCWtteAtjh1kEReHTJmnKwAzWmOU5j3Ro8e7xcuOOEG5p9CTbZyk2xxBDNHOJMq7jhPCMModKz15JdGm02r7k1al8GzdO9g0yx6GD8RlpzH0j1Ol+BHCnQ80TcrBvEOc9xgNN9q68Z2kVU7elNbXPHZYFsxalbpvwaHelDgTmx71NYfDzIqqvOY0s37kJsndWuY2bRhqNhJBFZi/SOvXFZHHhQcARGxBsizc5LKfIG3UqYHhuAJ/beErRvZLUM8hCgd5w8ISZNzPdM5pMGfe7ckaEWRRjhb7CmFHVZ9RQ+cHXGnUdSsrsDCT/gwZLIt8gHSIncE3H5m9zauhRmY2KYUZVVMKkbPB1TRfUbZdVWbEjJA7w4SXdyCN0Byh37pQl0ONvXtc5/eNRyuGHlkQj5qh/26zm/x4sawA+/7F4xfWiCib55DMLHFyXP3ooQIPmbwz+u4zLPnXymwJZG894ObapMlc1hWPmb2SbN28ZOuU1R67JwUqaI=";
            return new String[] {texture, signature};
        }

    }
}
