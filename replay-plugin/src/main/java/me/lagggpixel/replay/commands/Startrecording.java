package me.lagggpixel.replay.commands;

import me.lagggpixel.replay.Replay;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Startrecording implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can execute this command");
            return false;
        }
        Player player = ((Player) commandSender).getPlayer();
        Replay.getInstance().getReplayManager().startRecording(player.getWorld());
        player.sendMessage(ChatColor.GREEN + "Recording started.");
        return false;
    }
}
