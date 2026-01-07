package me.lagggpixel.replay.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class PacketUtils {

    public static int spawnFakePlayer(Iterable<Player> players, String playerName, List<TextureProperty> textureProperties, Location location) {
        int entityId = 100000000 + (int) (Math.random() * 1900000000);
        UUID npcUUID = UUID.randomUUID();

        UserProfile profile = new UserProfile(npcUUID, playerName);
        if (textureProperties != null && !textureProperties.isEmpty()) profile.setTextureProperties(textureProperties);
        Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();

        WrapperPlayServerSpawnPlayer spawnPacket = new WrapperPlayServerSpawnPlayer(entityId, npcUUID, position, location.getYaw(), location.getPitch(), new ArrayList<>());
        PacketWrapper<?> playerInfoPacket = getPlayerInfoPacket(playerName, serverVersion, profile);

        for (Player viewer : players) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(playerInfoPacket);
            user.sendPacket(spawnPacket);
        }
        return entityId;
    }

    public static int spawnFakePlayer(Iterable<Player> players, Player playerToCopy, Location location) {
        return spawnFakePlayer(players, playerToCopy.getName(), SpigotReflectionUtil.getUserProfile(playerToCopy), location);
    }

    public static int spawnFakePlayer(Iterable<Player> players, UUID playerUUID, Location location) {
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) return spawnFakePlayer(players, onlinePlayer, location);
        else {
            String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
            if (playerName == null) playerName = "Unknown";
            return spawnFakePlayer(players, playerName, new ArrayList<>(), location);
        }
    }

    private static @NotNull PacketWrapper<?> getPlayerInfoPacket(String name, ServerVersion serverVersion, UserProfile profile) {
        PacketWrapper<?> playerInfoPacket;
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            // Use modern PlayerInfoUpdate packet for 1.19.3+
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(profile, false, 0, GameMode.SURVIVAL, Component.text(name), null);
            playerInfoPacket = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, playerInfo);
        } else {
            // Use legacy PlayerInfo packet for older versions
            WrapperPlayServerPlayerInfo.PlayerData playerData = new WrapperPlayServerPlayerInfo.PlayerData(Component.text(name), profile, GameMode.SURVIVAL, 0);
            playerInfoPacket = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, playerData);
        }
        return playerInfoPacket;
    }

    public static void sendActionBar(Player player, String message) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        WrapperPlayServerActionBar packet = new WrapperPlayServerActionBar(Component.text(message));
        user.sendPacket(packet);
    }

    public static ItemStack setItemTag(ItemStack itemStack, String key, String value) {
        com.github.retrooper.packetevents.protocol.item.ItemStack peStack = SpigotConversionUtil.fromBukkitItemStack(itemStack);
        NBTCompound tag = peStack.getOrCreateTag();
        NBT nbt = new NBTString(value);
        tag.setTag(key, nbt);
        return SpigotConversionUtil.toBukkitItemStack(peStack);
    }

    public static String getItemTag(ItemStack itemStack, String key) {
        com.github.retrooper.packetevents.protocol.item.ItemStack peStack = SpigotConversionUtil.fromBukkitItemStack(itemStack);
        NBTCompound tag = peStack.getOrCreateTag();
        NBT nbt = tag.getTagOrNull(key);
        if (nbt instanceof NBTString) return ((NBTString) nbt).getValue();
        return null;
    }

    public static ItemStack getSkull(String url) {
        com.github.retrooper.packetevents.protocol.item.ItemStack skull = new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder().type(ItemTypes.PLAYER_HEAD).amount(1).build();
        NBTCompound tag = skull.getOrCreateTag();
        NBTCompound skullOwner = new NBTCompound();
        NBTCompound properties = new NBTCompound();
        NBTList<NBTCompound> textures = new NBTList<>(NBTType.COMPOUND);
        NBTCompound texture = new NBTCompound();
        String base64 = Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes(StandardCharsets.UTF_8));
        texture.setTag("Value", new NBTString(base64));
        textures.addTag(texture);
        properties.setTag("textures", textures);
        skullOwner.setTag("Properties", properties);
        tag.setTag("SkullOwner", skullOwner);
        skull.setNBT(tag);
        return SpigotConversionUtil.toBukkitItemStack(skull);
    }

    public static ItemStack getSkull(Player player) {
        com.github.retrooper.packetevents.protocol.item.ItemStack skull = new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder().type(ItemTypes.PLAYER_HEAD).amount(1).build();
        NBTCompound tag = skull.getOrCreateTag();
        NBTCompound skullOwner = new NBTCompound();
        skullOwner.setTag("Name", new NBTString(player.getName()));
        tag.setTag("SkullOwner", skullOwner);
        skull.setNBT(tag);
        return SpigotConversionUtil.toBukkitItemStack(skull);
    }

    public static ItemStack getSkull(UUID uuid) {
        com.github.retrooper.packetevents.protocol.item.ItemStack skull = new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder().type(ItemTypes.PLAYER_HEAD).amount(1).build();
        NBTCompound tag = skull.getOrCreateTag();
        NBTCompound skullOwner = new NBTCompound();
        skullOwner.setTag("Id", new NBTString(uuid.toString()));
        tag.setTag("SkullOwner", skullOwner);
        skull.setNBT(tag);
        return SpigotConversionUtil.toBukkitItemStack(skull);
    }
}
