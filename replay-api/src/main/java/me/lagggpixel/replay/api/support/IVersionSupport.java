package me.lagggpixel.replay.api.support;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.AnimationType;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.recordables.IEquipment;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.BlockAction;
import me.lagggpixel.replay.api.utils.entity.player.ReplayPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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
     * Gets the player's current equipment
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return Equipment object containing data about the player's equipment
     * @see IEquipment
     */
    Recordable createEquipmentRecordable(IRecording replay, Entity entity);

    /**
     * Create a recordable for a block
     *
     * @param replay The replay where it is recorded
     * @param block The block to gather the data from
     * @param actionType The action type
     * @return The recordable generated containing the block data
     */
    Recordable createBlockRecordable(IRecording replay, Block block, BlockAction actionType, boolean playSound);

    /**
     * Create a recordable for player status
     *
     * @param replay The replay where it is recorded
     * @param player The player to gather the data from
     * @return The recordable generated for containing the block data
     */
    Recordable createPlayerStatusRecordable(IRecording replay, Player player);

    /**
     * Create a recordable for entity status
     *
     * @param replay The replay where it is recorded
     * @param entity The entity to gather the data from
     * @return The recordable generated for containing the entity status
     */
    Recordable createEntityStatusRecordable(IRecording replay, Entity entity);

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
     * @param spawnLocation The spawn location
     * @param entityType The entity type
     * @param entityId The entity'S ID
     * @param uniqueId The entity's uuid
     * @return The recordable generated for containing the entity spawn data
     */
    Recordable createEntitySpawnRecordable(IRecording replay, Location spawnLocation, EntityType entityType, int entityId, UUID uniqueId);

    /**
     * Create a recordable for a placed block via pop up tower
     *
     * @param replay The replay where it is recorded
     * @param block The block placed
     * @return The recordable generated for containing the placed block data
     */
    Recordable createPopUpTowerRecordable(IRecording replay, Block block);

    /**
     * Create a recordable for a placed block via pop up tower
     *
     * @param replay The replay where it is recorded
     * @param block The block placed
     * @return The recordable generated for containing the placed block data
     */
    Recordable createEggBridgeRecordable(IRecording replay, Block block);

    /**
     * Create a copy of a player, to then spawn it as a NPC
     * @param player The player to copy
     * @return the NPC player copy
     */
    Player createNPCCopy(IReplaySession replaySession, Player player);

    /**
     * Spawn the ReplayPlayer (fake player)
     * @param replayPlayer The fake player to spawn
     * @param player The player to show the fake player to
     */
    void spawnFakePlayer(ReplayPlayer replayPlayer, Player player, Location location);
}
