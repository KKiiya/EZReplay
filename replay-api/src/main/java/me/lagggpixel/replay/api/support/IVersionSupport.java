package me.lagggpixel.replay.api.support;

import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public interface IVersionSupport {
    /**
     * Get the version number
     *
     * @return The number of the version support currently being used
     */
    int getVersion();

    /**
     * Create a recordable for an entity status change
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return The recordable generated for containing the entity status change data
     */
    Recordable createEntityStatusRecordable(IRecording replay, Entity entity);


    /**
     * Create a movement recordable
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return The recordable generated containing the entity position
     */
    Recordable createEntityMovementRecordable(IRecording replay, Entity entity);

    /**
     * Gets the player's current equipment
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return Recordable object containing data about the player's equipment
     */
    Recordable createEquipmentRecordable(IRecording replay, LivingEntity entity);

    /**
     * Create a recordable for a block
     *
     * @param replay The replay where it is recorded
     * @param world The world where the block is located
     * @param material The material of the block
     * @param data The data value of the block
     * @param location The location of the block
     * @param actionType The action type associated with the block
     * @param playSound Whether to play a sound associated with the block action
     * @return The recordable generated containing the block data
     */
    Recordable createBlockRecordable(IRecording replay, World world, org.bukkit.Material material, byte data, Location location, BlockAction actionType, boolean playSound);


    /**
     * Create a recordable for entity animation
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @param animationType The animation type
     * @return The recordable generated for containing the animation data
     */
    Recordable createAnimationRecordable(IRecording replay, Entity entity, AnimationType animationType);

    /**
     * Create a recordable for a spawned entity
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return The recordable generated for containing the entity spawn data
     */
    Recordable createEntitySpawnRecordable(IRecording replay, Entity entity);

    /**
     * Create a recordable for a dying entity
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return The recordable generated for containing the entity death data
     */
    Recordable createEntityDeathRecordable(IRecording replay, Entity entity);

    /**
     * Create a recordable for an entity burning
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return The recordable generated for containing the entity burning data
     */
    Recordable createBurningRecordable(IRecording replay, Entity entity);

    /**
     * Create a recordable for a player becoming invisible
     *
     * @param replay The replay where it is recorded
     * @param player The player to gather the data from
     * @return The recordable generated for containing the player invisibility data
     */
    Recordable createInvisibilityRecordable(IRecording replay, Player player, boolean isInvisible);

    /**
     * Create a recordable for a player sneaking
     *
     * @param replay The replay where it is recorded
     * @param player The player to gather the data from
     * @return The recordable generated for containing the player sneaking data
     */
    Recordable createSneakingRecordable(IRecording replay, Player player);

    /**
     * Create a recordable for a player sprinting
     *
     * @param replay The replay where it is recorded
     * @param player The player to gather the data from
     * @return The recordable generated for containing the player sprinting data
     */
    Recordable createSprintRecordable(IRecording replay, Player player);

    /**
     * Create a recordable for a player blocking with a sword
     *
     * @param replay The replay where it is recorded
     * @param player The player to gather the data from
     * @return The recordable generated for containing the player sword blocking data
     */
    Recordable createSwordBlockRecordable(IRecording replay, Player player);

    /**
     * Create a recordable for a placed block via pop up tower
     *
     * @param replay The replay where it is recorded
     * @param block The block placed
     * @param sound The sound associated with the block placement
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     * @return The recordable generated for containing the placed block data
     */
    Recordable createPopUpTowerRecordable(IRecording replay, Block block, Sound sound, float volume, float pitch);


    /**
     * Create a recordable for a placed block via pop up tower
     *
     * @param replay The replay where it is recorded
     * @param block The block placed
     * @param sound The sound associated with the block placement
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     * @return The recordable generated for containing the placed block data
     */
    Recordable createEggBridgeRecordable(IRecording replay, Block block, Sound sound, float volume, float pitch);

    /**
     * The hologram to create the recordable for
     * @param replay The replay where it is recorded
     * @param hologram The hologram spawned
     * @return The recordable generated for containing the created hologram data
     */
    Recordable createHologramRecordable(IRecording replay, IHologram hologram);

    /**
     * The generator to create the recordable for
     * @param replay The replay where it is recorded
     * @param generator The generator created
     * @return The recordable generated for containing the created generator data
     */
    Recordable createGeneratorRecordable(IRecording replay, IGenerator generator);

    /**
     * Crate a recordable for chat messages
     * @param replay The replay where it is recorded
     * @param sender The sender of the message
     * @param content The content message
     * @return The recordable generated for containing the message sent
     */
    Recordable createChatRecordable(IRecording replay, UUID sender, String content);

    /**
     * Create a recordable for item drops
     * @param replay The replay where it is recorded
     * @param item The item dropped
     * @return The recordable generated for containing the item drop
     */
    Recordable createItemDropRecordable(IRecording replay, Item item);

    /**
     * Create a recordable for item pickups
     * @param replay The replay where it is recorded
     * @param item The item picked
     * @param collector The entity collecting the item
     * @return The recordable generated for containing the item pick
     */
    Recordable createItemPickRecordable(IRecording replay, Item item, Entity collector);

    /**
     * Create a recordable for a TNT spawn
     * @param recording The replay where it is recorded
     * @param location The location where the TNT spawns
     * @return The recordable generated for containing the tnt spawn
     */
    Recordable createTntSpawnRecordable(IRecording recording, Location location);

    /**
     * Create a recordable for a explosion
     * @param replay The replay where it is recorded
     * @param location The location where the explosion occurs
     * @return The recordable generated for containing the explosion information
     */
    Recordable createExplosionRecordable(IRecording replay, Location location, float radius);

    /**
     * Create a copy of a player, to then spawn it as a NPC
     * @param player The player to copy
     * @return the NPC player copy
     */
    Player createNPCCopy(IReplaySession replaySession, OfflinePlayer player);

    /**
     * Sets a custom tag on an ItemStack.
     *
     * @param item The ItemStack to set the tag on.
     * @param key The key of the tag.
     * @param value The value of the tag.
     * @return The ItemStack with the updated tag.
     */
    ItemStack setItemTag(ItemStack item, String key, String value);

    /**
     * Retrieves a custom tag from an ItemStack.
     *
     * @param item The ItemStack to get the tag from.
     * @param key The key of the tag.
     * @return The value of the tag, or null if the tag is not present.
     */
    String getItemTag(ItemStack item, String key);

    /**
     * Retrieves the Material type for a player's head.
     *
     * @return The Material type representing a player's head.
     */
    Material getPlayerHeadMaterial();

    /**
     * Get the skull of a player using the
     * minecraft texture URL
     * @param url The url of the skull
     * @return The item stack of the generated skull
     */
    ItemStack getSkull(String url);

    /**
     * Spawn the ReplayPlayer (fake player)
     * @param replayPlayer The fake player to spawn
     * @param player The player to show the fake player to
     */
    void spawnFakePlayer(Player replayPlayer, Player player, Location location);

    /**
     * Send an action bar to a player
     * @param player The player to send the action bar to
     * @param message The message being sent
     */
    void sendActionBar(Player player, String message);

    /**
     * Check if a material can be interacted with (e.g: Doors, buttons, ...)
     * @param material The material to check
     * @return Whether the material is interactable
     */
    boolean isInteractable(Material material);

    /**
     * Check if a block can be interacted with (e.g: Doors, buttons, ...)
     * @param block The block to check
     * @return Whether the block is interactable
     */
    boolean isInteractable(Block block);
}
