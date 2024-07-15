package me.lagggpixel.replay.listeners.arena;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigManager;
import com.tomkeuper.bedwars.api.events.gameplay.EggBridgeBuildEvent;
import com.tomkeuper.bedwars.api.events.gameplay.PopUpTowerBuildEvent;
import com.tomkeuper.bedwars.configuration.Sounds;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class SpecialItemsListener implements Listener {

    @EventHandler
    public void onEggBridgeBuild(EggBridgeBuildEvent e) {
        IRecording replay = Replay.getInstance().getReplayManager().getActiveReplay(e.getArena());
        if (replay == null) return;

        ConfigManager sounds = Sounds.getSounds();

        Sound sound = Sound.valueOf(sounds.getString("egg-bridge-block.sound"));
        float volume = (float) sounds.getDouble("egg-bridge-block.volume");
        float pitch = (float) sounds.getDouble("egg-bridge-block.pitch");

        IFrame lastFrame = replay.getLastFrame();
        Recordable eggBridgeBuild = Replay.getInstance().getVersionSupport().createEggBridgeRecordable(replay, e.getBlock(), sound, volume, pitch);
        lastFrame.addRecordable(eggBridgeBuild);
    }

    @EventHandler
    public void onPopUpTowerBuild(PopUpTowerBuildEvent e) {
        IRecording replay = Replay.getInstance().getReplayManager().getActiveReplay(e.getArena());
        if (replay == null) return;

        ConfigManager sounds = Sounds.getSounds();

        Sound sound = Sound.valueOf(sounds.getString("pop-up-tower-build.sound"));
        float volume = (float) sounds.getDouble("pop-up-tower-build.volume");
        float pitch = (float) sounds.getDouble("pop-up-tower-build.pitch");

        IFrame lastFrame = replay.getLastFrame();
        Recordable popUpTowerBuild = Replay.getInstance().getVersionSupport().createPopUpTowerRecordable(replay, e.getBlock(), sound, volume, pitch);
        lastFrame.addRecordable(popUpTowerBuild);
    }

    @EventHandler
    public void onExplosion(ExplosionPrimeEvent e) {
        String identifier = e.getEntity().getWorld().getName();
        Location loc = e.getEntity().getLocation();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(identifier);

        if (e.isCancelled()) return;

        IRecording replay = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (replay == null) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createExplosionRecordable(replay, loc, e.getRadius());
        replay.getLastFrame().addRecordable(recordable);
    }
}
