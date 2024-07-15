package me.lagggpixel.replay.replay.tasks;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.entity.*;

public class EquipmentTrackerTask implements Runnable {
    private final IRecording replay;

    public EquipmentTrackerTask(IRecording replay) {
        this.replay = replay;
    }

    @Override
    public void run() {
        IFrame lastFrame = replay.getLastFrame();

        for (Entity entity : replay.getSpawnedEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity livingEntity = (LivingEntity) entity;
            Recordable equipmentRecordable = Replay.getInstance().getVersionSupport().createEquipmentRecordable(replay, livingEntity);
            lastFrame.addRecordable(equipmentRecordable);
        }

        for (Player p : replay.getArena().getPlayers()) {
            Recordable equipmentRecordable = Replay.getInstance().getVersionSupport().createEquipmentRecordable(replay, p);
            lastFrame.addRecordable(equipmentRecordable);
        }
    }
}
