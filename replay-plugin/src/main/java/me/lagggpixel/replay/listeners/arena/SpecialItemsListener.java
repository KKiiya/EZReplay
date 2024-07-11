package me.lagggpixel.replay.listeners.arena;

import com.tomkeuper.bedwars.api.events.gameplay.EggBridgeBuildEvent;
import com.tomkeuper.bedwars.api.events.gameplay.PopUpTowerBuildEvent;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpecialItemsListener implements Listener {

    @EventHandler
    public void onEggBridgeBuild(EggBridgeBuildEvent e) {
        IRecording replay = Replay.getInstance().getReplayManager().getActiveReplay(e.getArena());
        if (replay == null) return;

        IFrame lastFrame = replay.getLastFrame();
        Recordable eggBridgeBuild = Replay.getInstance().getVersionSupport().createEggBridgeRecordable(replay, e.getBlock());
        lastFrame.addRecordable(eggBridgeBuild);
    }

    @EventHandler
    public void onPopUpTowerBuild(PopUpTowerBuildEvent e) {
        IRecording replay = Replay.getInstance().getReplayManager().getActiveReplay(e.getArena());
        if (replay == null) return;

        IFrame lastFrame = replay.getLastFrame();
        Recordable popUpTowerBuild = Replay.getInstance().getVersionSupport().createPopUpTowerRecordable(replay, e.getBlock());
        lastFrame.addRecordable(popUpTowerBuild);
    }
}
