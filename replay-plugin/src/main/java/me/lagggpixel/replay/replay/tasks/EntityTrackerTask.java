package me.lagggpixel.replay.replay.tasks;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.Vector3d;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import java.util.ArrayList;
import java.util.List;

public class EntityTrackerTask implements Runnable {

    private final static List<Entity> trackedEntities = new ArrayList<>();
    private final IRecording replay;
    private final Entity entity;
    private final Vector3d previousPosition;

    public EntityTrackerTask(IRecording replay, Entity entity) {
        this.replay = replay;
        this.entity = entity;
        Location loc = entity.getLocation();
        this.previousPosition = new Vector3d(loc.getX(), loc.getY(), loc.getZ());
        trackEntity(entity);
    }

    @Override
    public void run() {
        Location loc = entity.getLocation();
        Vector3d currentPosition = new Vector3d(loc.getX(), loc.getY(), loc.getZ());

        if (!currentPosition.equals(previousPosition)) {
            Recordable recordable = Replay.getInstance().getVersionSupport().createEntityMovementRecordable(replay, entity);
            IFrame lastFrame = replay.getLastFrame();
            lastFrame.addRecordable(recordable);
        }
    }

    public static boolean isTracked(Entity entity) {
        return trackedEntities.contains(entity);
    }

    private static void trackEntity(Entity entity) {
        trackedEntities.add(entity);
    }

    public static void untrackEntity(Entity entity) {
        trackedEntities.remove(entity);
    }
}
