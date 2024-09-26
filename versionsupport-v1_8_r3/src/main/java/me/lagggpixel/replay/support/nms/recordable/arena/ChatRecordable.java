package me.lagggpixel.replay.support.nms.recordable.arena;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.language.Language;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IChat;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCanBuildEvent;

import java.util.UUID;

public class ChatRecordable extends Recordable implements IChat {

    private final UUID sender;
    private final String content;

    public ChatRecordable(IRecording replay, UUID sender, String content) {
        super(replay);
        this.sender = sender;
        this.content = content;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        BedWars bedWars = v1_8_R3.getInstance().getPlugin().getBedWarsAPI();
        Player replayPlayer = (Player) replaySession.getSpawnedEntities().get(sender.toString());
        Language lang = bedWars.getPlayerLanguage(player);

        String format = content.startsWith("!") || content.startsWith("/shout") ? lang.getString("format-chat-global") : lang.getString("format-chat-team");
        format = format
                .replace("%bw_level%", replaySession.getLevelName(sender.toString()))
                .replace("%bw_v_prefix%", replaySession.getPrefix(sender.toString()))
                .replace("%bw_v_suffix%", replaySession.getSuffix(sender.toString()))
                .replace("%bw_player%", replayPlayer.getDisplayName())
                .replace("%bw_playername%", replayPlayer.getName())
                .replace("%bw_message%", content);
        player.sendMessage(format);
    }

    @Override
    public UUID getSender() {
        return sender;
    }

    @Override
    public String getContent() {
        return content;
    }
}
