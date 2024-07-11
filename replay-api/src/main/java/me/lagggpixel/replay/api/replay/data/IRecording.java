package me.lagggpixel.replay.api.replay.data;

import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.entity.Entity;

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
}
