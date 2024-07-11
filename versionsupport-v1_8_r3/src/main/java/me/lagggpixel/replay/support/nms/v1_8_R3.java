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
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.AnimationType;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.api.utils.entity.player.ReplayPlayer;
import me.lagggpixel.replay.support.nms.packets.InjectorHandler;
import me.lagggpixel.replay.support.nms.recordable.arena.EggBridgeRecordable;
import me.lagggpixel.replay.support.nms.recordable.arena.PopUpTowerRecordable;
import me.lagggpixel.replay.support.nms.recordable.entity.recordables.AnimationRecordable;
import me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.Equipment;
import me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.PlayerStatus;
import me.lagggpixel.replay.support.nms.recordable.entity.recordables.EntitySpawn;
import me.lagggpixel.replay.support.nms.recordable.entity.recordables.EntityStatus;
import me.lagggpixel.replay.support.nms.recordable.world.block.BlockRecordable;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
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

        InjectorHandler.init();
    }

    public IReplay getPlugin() {
        return plugin;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public Recordable createEquipmentRecordable(IRecording replay, Entity entity) {
        return new Equipment(replay, entity);
    }

    @Override
    public Recordable createBlockRecordable(IRecording replay, Block block, BlockAction actionType, boolean playSound) {
        return new BlockRecordable(replay, block, actionType, playSound);
    }

    @Override
    public Recordable createPlayerStatusRecordable(IRecording replay, Player player) {
        return new PlayerStatus(replay, player);
    }

    @Override
    public Recordable createEntityStatusRecordable(IRecording replay, Entity entity) {
        return new EntityStatus(replay, entity);
    }

    @Override
    public Recordable createAnimationRecordable(IRecording replay, Entity entity, AnimationType animationType) {
        return new AnimationRecordable(replay, entity, animationType);
    }

    @Override
    public Recordable createEntitySpawnRecordable(IRecording replay, Location spawnLocation, EntityType entityType, int entityId, UUID uniqueId) {
        return new EntitySpawn(replay, spawnLocation, entityType, entityId, uniqueId);
    }

    @Override
    public Recordable createPopUpTowerRecordable(IRecording replay, Block block) {
        return new PopUpTowerRecordable(replay, block);
    }

    @Override
    public Recordable createEggBridgeRecordable(IRecording replay, Block block) {
        return new EggBridgeRecordable(replay, block);
    }

    @Override
    public Player createNPCCopy(IReplaySession replaySession, Player player) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        WorldServer worldServer = ((CraftWorld) replaySession.getWorld()).getHandle();
        World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getDisplayName());
        PlayerInteractManager pim = new PlayerInteractManager(world);
        EntityPlayer entityPlayer = new EntityPlayer(craftServer.getServer(), worldServer, gameProfile, pim);

        String[] skinData = this.getSkin(player.getName());
        gameProfile.getProperties().put("texture", new Property("textures", skinData[0], skinData[1]));

        return new CraftPlayer(craftServer, entityPlayer);
    }

    @Override
    public void spawnFakePlayer(ReplayPlayer replayPlayer, Player player, Location location) {
        EntityPlayer entityPlayer = ((CraftPlayer) replayPlayer.getEntity()).getHandle();

        PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(entityPlayer, replayPlayer.getEntity().getType().getTypeId());
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);

        sendPackets(player, spawn, metadata);
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
            JsonObject property = (new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
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
