package me.lagggpixel.replay.commands;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Stoprecording implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can execute this command");
            return false;
        }
        Player player = ((Player) commandSender).getPlayer();
        IRecording replay = Replay.getInstance().getReplayManager().stopRecording(player.getWorld());
        player.sendMessage(ChatColor.GREEN + "Recording stopped.");
        replay.toFile();
        return false;
    }
}
