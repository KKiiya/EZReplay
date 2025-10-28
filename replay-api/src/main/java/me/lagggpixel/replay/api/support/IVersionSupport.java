package me.lagggpixel.replay.api.support;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.api.utils.block.ChunkPos;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.block.BlockAction;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
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
     *
     * @param recording - The replay where it is recorded
     * @param cache - The new block cache object
     * @return Recordable object generated containing multiblock info
     */
    Recordable createBlockUpdateRecordable(IRecording recording, HashMap<ChunkPos, List<BlockCache>> cache);

    /**
     * Create a recordable for a block
     *
     * @param replay The replay where it is recorded
     * @param cache - The new block cache object
     * @param actionType The action type associated with the block
     * @param playSound Whether to play a sound associated with the block action
     * @return The recordable generated containing the block data
     */
    Recordable createBlockRecordable(IRecording replay, BlockCache cache, BlockAction actionType, boolean playSound);


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
     * Create a recordable for a spawned entity
     *
     * @param replay The replay where it is recorded
     * @param player The entity to gather the data from
     * @return The recordable generated for containing the player respawn data
     */
    Recordable createPlayerRespawnRecordable(IRecording replay, Player player);

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
    Recordable createSneakingRecordable(IRecording replay, UUID player, boolean isSneaking);

    /**
     * Create a recordable for a player sprinting
     *
     * @param replay The replay where it is recorded
     * @param player The player to gather the data from
     * @param isSprinting Whether the player is sprinting
     * @return The recordable generated for containing the player sprinting data
     */
    Recordable createSprintRecordable(IRecording replay, UUID player, boolean isSprinting);

    /**
     * Create a recordable for a player blocking with a sword
     *
     * @param replay The replay where it is recorded
     * @param player The player to gather the data from
     * @return The recordable generated for containing the player sword blocking data
     */
    Recordable createSwordBlockRecordable(IRecording replay, Player player);

    /**
     * Crate a recordable for chat messages
     * @param recording The recording where it is recorded
     * @param sender The sender of the message
     * @param format The format of the message to send
     * @param content The content message
     * @return The recordable generated for containing the message sent
     */
    Recordable createChatRecordable(IRecording recording, UUID sender, String format, String content);

    /**
     * Create a recordable for item drops
     * @param recording The recording where it is recorded
     * @param item The item dropped
     * @return The recordable generated for containing the item drop
     */
    Recordable createItemDropRecordable(IRecording recording, Item item);

    /**
     * Create a recordable for item pickups
     * @param recording The recording where it is recorded
     * @param item The item picked
     * @param collector The entity collecting the item
     * @return The recordable generated for containing the item pick
     */
    Recordable createItemPickRecordable(IRecording recording, Item item, Entity collector);

    /**
     * Create a recordable for item merge
     * @param recording The recording where it is recorded
     * @param entity The item being merged
     * @param target The main stack of items
     * @return The recordable generated for containing the item merge
     */
    Recordable createItemMergeRecordable(IRecording recording, Item entity, Item target);

    /**
     * Create a recordable for a explosion
     * @param recording The recording where it is recorded
     * @param location The location where the explosion occurs
     * @return The recordable generated for containing the explosion information
     */
    Recordable createExplosionRecordable(IRecording recording, Location location, Entity entity, float radius);

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
     * Make the world not update (no light cycle and no generation)
     * and peaceful
     * @param creator - The WorldCreator to set to static
     * @return The world with the applied changes
     */
    World setStatic(WorldCreator creator);

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

    /**
     * Get the item in the main hand of a specified player
     * @param p - The player to get the item from
     * @return the item in the main hand
     */
    @Nullable
    ItemStack getItemInMainHand(Player p);

    /**
     * Get the item in the offhand of a specified player
     * @param p - The player to get the item from
     * @return the item in the main hand
     */
    @Nullable
    ItemStack getItemInOffHand(Player p);
}
