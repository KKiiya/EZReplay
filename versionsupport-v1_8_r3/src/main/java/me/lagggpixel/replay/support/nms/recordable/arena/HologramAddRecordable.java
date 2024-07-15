package me.lagggpixel.replay.support.nms.recordable.arena;

import com.tomkeuper.bedwars.api.hologram.containers.IHoloLine;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import com.tomkeuper.bedwars.api.server.VersionSupport;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IHologramAdd;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import org.bukkit.entity.Player;

public class HologramAddRecordable extends Recordable implements IHologramAdd {

    private final String[] lines;
    private final double gap;
    private final Vector3d location;

    public HologramAddRecordable(IRecording replay, IHologram hologram) {
        super(replay);
        this.lines = hologram.getLines().stream().map(IHoloLine::getText).toArray(String[]::new);
        this.gap = hologram.getGap();
        this.location = Vector3d.fromBukkitLocation(hologram.getLocation());
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        VersionSupport vs = v1_8_R3.getInstance().getPlugin().getBedWarsAPI().getVersionSupport();
        IHologram holo = vs.createHologram(player, location.toBukkitLocation(), lines);
        holo.setGap(gap);
    }
}
