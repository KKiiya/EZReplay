package me.lagggpixel.replay.api.utils.entity.player;

import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import lombok.Getter;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class ReplayPlayer {

    private final Player entity;
    private final IReplaySession replaySession;
    private final UUID uniqueId;
    private final int entityId;
    private final String name;
    private final String displayName;
    private final String displayLevel;
    private final String prefix;
    private final String suffix;
    private final TeamColor teamColor;

    public ReplayPlayer(IReplaySession replaySession, Player player, TeamColor teamColor, String displayLevel, String prefix, String suffix) {
        this.entity = player;
        this.replaySession = replaySession;
        this.displayName = player.getDisplayName();
        this.displayLevel = displayLevel;
        this.prefix = prefix;
        this.suffix = suffix;
        this.entityId = player.getEntityId();
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
        this.teamColor = teamColor;
    }
}
