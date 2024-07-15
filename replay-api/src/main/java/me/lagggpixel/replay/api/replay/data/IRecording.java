package me.lagggpixel.replay.api.replay.data;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 10, 2024
 */
public interface IRecording {
    /**
     * Get the ID of the replay
     * @return the ID object of the replay
     */
    UUID getID();

    /**
     * Get the arena being recorded
     * @return the arena object
     */
    IArena getArena();

    /**
     * Add multiple frames to the replay
     * @param frames the frames to add
     */
    void add(IFrame... frames);

    /**
     * Add multiple frames to the replay
     * @param frames the frames to add
     */
    void add(List<IFrame> frames);

    /**
     * Get the desired frame
     * @param tick the tick where the frame was added
     * @return the requested frame
     */
    IFrame getFrame(int tick);

    /**
     * Get the last frame
     * @return the last frame recorded of the recording
     */
    IFrame getLastFrame();

    /**
     * Get the frame before the last frame
     * @return the previous frame recorded before the last frame
     */
    IFrame getPreviousFrame();

    /**
     * Get the frames of the replay
     * @return A list of the frames
     */
    List<IFrame> getFrames();

    /**
     * Convert the replay to a file
     * @return the file object of the replay
     */
    File toFile();

    /**
     * Get the spawned entities during the replay
     */
    List<Entity> getSpawnedEntities();

    /**
     * Get the dropped items during the replay
     */
    List<Item> getDroppedItems();

    /**
     * Get the players that started playing
     */
    List<String> getPlayers();

    /**
     * Get the player's team color
     */
    TeamColor getTeamColor(String player);

    /**
     * Get the prefix of a player
     * @param player The player to get the prefix from
     * @return String object containing the prefix
     */
    String getPrefix(String player);

    /**
     * Get the suffix of a player
     * @param player The player to get the suffix from
     * @return String object containing the suffix
     */
    String getSuffix(String player);

    /**
     * Get the level name of a player
     * @param player The player to get the level name from
     * @return String object containing the level name
     */
    String getLevelName(String player);

    /**
     * Get the first location where the player spawned
     * @param offlinePlayer The offline player to get the data from
     * @return the location Object
     */
    Location getSpawnLocation(String offlinePlayer);

    /**
     * Start or continue recording
     */
    void start();

    /**
     * Pause the recording
     */
    void pause();

    /**
     * Stop recording
     */
    @SuppressWarnings("Will not be able to continue recording")
    void stop();

    /**
     * Get the desired entity spawned during the replay
     *
     * @param id The id of the entity
     * @return the entity object
     */
    Entity getSpawnedEntity(int id);

    /**
     * Check if the game is being recorded currently
     */
    boolean isRecording();

    /**
     * Check if the replay has finished recording
     */
    boolean isFinished();

    /**
     * Create a replay session to replay the recording
     * @param player The player that will be watching the replay
     * @return The IReplaySession object
     */
    IReplaySession watch(Player player);

    /**
     * Create a replay session to replay the recording
     * @param players The players that will be watching the replay
     * @return The IReplaySession object
     */
    IReplaySession watch(Player... players);
}
