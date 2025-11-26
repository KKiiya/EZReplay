package me.lagggpixel.replay.replay.content;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IControls;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.menu.TrackerMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;

public class Controls implements IControls {

    private final IVersionSupport vs;
    private final HashMap<Integer, ItemStack> oldInventory;
    private final IReplaySession replaySession;
    private final Player player;
    private boolean isInDelay = false;

    public Controls(IReplaySession replaySession, Player player) {
        this.vs = Replay.getInstance().getVersionSupport();
        this.oldInventory = new HashMap<>();
        this.replaySession = replaySession;
        this.player = player;
        giveItems();
    }

    @Override
    public void giveItems() {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            oldInventory.put(i, inv.getItem(i));
        }
        inv.clear();

        ItemStack tracker = new ItemStack(Material.COMPASS);
        ItemStack decreaseSpeed = vs.getSkull("http://textures.minecraft.net/texture/118a2dd5bef0b073b13271a7eeb9cfea7afe8593c57a93821e43175572461812");
        ItemStack rewind = vs.getSkull("http://textures.minecraft.net/texture/864f779a8e3ffa231143fa69b96b14ee35c16d669e19c75fd1a7da4bf306c");
        ItemStack pauseResume = vs.getSkull("http://textures.minecraft.net/texture/b46f95582cef626b5562ed656b8a1ce877108d066635378f3269fea34a770494");
        ItemStack forward = vs.getSkull("http://textures.minecraft.net/texture/d9eccc5c1c79aa7826a15a7f5f12fb40328157c5242164ba2aef47e5de9a5cfc");
        ItemStack increaseSpeed = vs.getSkull("http://textures.minecraft.net/texture/d99f28332bcc349f42023c29e6e641f4b10a6b1e48718cae557466d51eb922");
        ItemStack resetReplay = vs.getSkull("http://textures.minecraft.net/texture/3a4fab3fd97eb7ecf48ab4fd327e093e886f4e217aab69585313c27a5035831a");

        ItemMeta trackerMeta = tracker.getItemMeta();
        trackerMeta.setDisplayName(ChatColor.GOLD + "Player Tracker");
        trackerMeta.setLore(Arrays.asList(ChatColor.GRAY + "Track players during", ChatColor.GRAY + "the replay."));
        tracker.setItemMeta(trackerMeta);

        ItemMeta decreaseSpeedMeta = decreaseSpeed.getItemMeta();
        decreaseSpeedMeta.setDisplayName(ChatColor.RED + "Decrease Speed");
        decreaseSpeedMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to decrease", ChatColor.GRAY + "the playback speed."));
        decreaseSpeed.setItemMeta(decreaseSpeedMeta);

        ItemMeta rewindMeta = rewind.getItemMeta();
        rewindMeta.setDisplayName(ChatColor.GOLD + "Rewind");
        rewindMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to rewind", ChatColor.GRAY + "the playback."));
        rewind.setItemMeta(rewindMeta);

        ItemMeta pauseResumeMeta = pauseResume.getItemMeta();
        pauseResumeMeta.setDisplayName(ChatColor.YELLOW + "Pause/Resume");
        pauseResumeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to pause or", ChatColor.GRAY + "resume the playback."));
        pauseResume.setItemMeta(pauseResumeMeta);

        ItemMeta forwardMeta = forward.getItemMeta();
        forwardMeta.setDisplayName(ChatColor.GREEN + "Fast Forward");
        forwardMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to fast forward", ChatColor.GRAY + "the playback."));
        forward.setItemMeta(forwardMeta);

        ItemMeta increaseSpeedMeta = increaseSpeed.getItemMeta();
        increaseSpeedMeta.setDisplayName(ChatColor.BLUE + "Increase Speed");
        increaseSpeedMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to increase", ChatColor.GRAY + "the playback speed."));
        increaseSpeed.setItemMeta(increaseSpeedMeta);

        ItemMeta resetReplayMeta = resetReplay.getItemMeta();
        resetReplayMeta.setDisplayName(ChatColor.GOLD + "Reset Replay");
        resetReplayMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to reset the", ChatColor.GRAY + "replay to the beginning."));
        resetReplay.setItemMeta(resetReplayMeta);

        inv.setItem(0, vs.setItemTag(tracker, "Replay-Control", "tracker"));
        inv.setItem(2, vs.setItemTag(decreaseSpeed, "Replay-Control", "decreaseSpeed"));
        inv.setItem(3, vs.setItemTag(rewind, "Replay-Control", "rewind"));
        inv.setItem(4, vs.setItemTag(pauseResume, "Replay-Control", "pauseResume"));
        inv.setItem(5, vs.setItemTag(forward, "Replay-Control", "forward"));
        inv.setItem(6, vs.setItemTag(increaseSpeed, "Replay-Control", "increaseSpeed"));
        inv.setItem(7, vs.setItemTag(resetReplay, "Replay-Control", "resetReplay"));
    }

    @Override
    public void giveOriginalInventory() {
        player.getInventory().clear();
        for (Integer slot : oldInventory.keySet()) {
            ItemStack originalItem = oldInventory.get(slot);
            player.getInventory().setItem(slot, originalItem);
        }
        oldInventory.clear();
    }

    @Override
    public void onControl(String control) {
        if (!isInDelay) {
            switch (control) {
                case "tracker":
                    new TrackerMenu(replaySession, player);
                    break;
                case "decreaseSpeed":
                    replaySession.setSpeed(replaySession.getSpeed() - 5);
                    for (Player player : replaySession.getViewers()) {
                        vs.sendActionBar(player, ChatColor.RED + "Speed decreased to " + ChatColor.YELLOW + "x" + replaySession.getSpeedAsDouble());
                    }
                    break;
                case "rewind":
                    replaySession.rewind(10);
                    for (Player player : replaySession.getViewers()) {
                        vs.sendActionBar(player, ChatColor.AQUA + "Rewound 10 seconds");
                    }
                    break;
                case "pauseResume":
                    if (replaySession.isPaused()) {
                        replaySession.resume();
                        for (Player player : replaySession.getViewers()) {
                            vs.sendActionBar(player, ChatColor.GREEN + "Playback resumed");
                        }
                    } else {
                        replaySession.pause();
                        for (Player player : replaySession.getViewers()) {
                            vs.sendActionBar(player, ChatColor.RED + "Playback paused");
                        }
                    }
                    break;
                case "forward":
                    replaySession.fastForward(10);
                    for (Player player : replaySession.getViewers()) {
                        vs.sendActionBar(player, ChatColor.AQUA + "Fast forwarded 10 seconds");
                    }
                    break;
                case "increaseSpeed":
                    replaySession.setSpeed(replaySession.getSpeed() + 5);
                    for (Player player : replaySession.getViewers()) {
                        vs.sendActionBar(player, ChatColor.GREEN + "Speed increased to " + ChatColor.YELLOW + "x" + replaySession.getSpeedAsDouble());
                    }
                    break;
                case "resetReplay":
                    replaySession.reset();
                    break;
            }
            isInDelay = true;
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> isInDelay = false, 20L);
        }
    }
}
