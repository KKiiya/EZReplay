package me.lagggpixel.replay.replay.tasks;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EquipmentTrackerTask implements Runnable {
    private final IRecording replay;

    public EquipmentTrackerTask(IRecording replay) {
        this.replay = replay;
    }

    @Override
    public void run() {
        int lastTick = Math.max(replay.getFrames().size() - 1, 0);
        IFrame lastFrame = replay.getFrame(lastTick);

        for (Entity entity : replay.getSpawnedEntities()) {
            Recordable equipmentRecordable = Replay.getInstance().getVersionSupport().createEquipmentRecordable(replay, entity);
            lastFrame.addRecordable(equipmentRecordable);
        }

        for (Player p : replay.getArena().getPlayers()) {
            Recordable equipmentRecordable = Replay.getInstance().getVersionSupport().createEquipmentRecordable(replay, p);
            lastFrame.addRecordable(equipmentRecordable);
        }
    }
}
