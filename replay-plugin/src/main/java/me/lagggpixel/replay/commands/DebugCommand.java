package me.lagggpixel.replay.commands;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            final Player player = (Player) commandSender;
            final UUID uuid = UUID.fromString(strings[0]);
            final IRecording replay = Replay.getInstance().getReplayManager().loadReplay(uuid);
            player.sendMessage(ChatColor.GREEN + "Replay loaded");
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> replay.watch(player), 70L);
            return true;
        }
        return true;
    }
}
