package me.lagggpixel.replay.replay.content;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.content.IControls;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.menu.TrackerMenu;
import me.lagggpixel.replay.utils.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Controls implements IControls {

    private final Map<String, Runnable> controlActions;
    private final HashMap<Integer, ItemStack> oldInventory;
    private final IReplaySession replaySession;
    private final Player player;
    private boolean isInDelay = false;

    public Controls(IReplaySession replaySession, Player player) {
        this.controlActions = new HashMap<>();
        this.oldInventory = new HashMap<>();
        this.replaySession = replaySession;
        this.player = player;
        setupControls();
        giveItems();
    }

    @Override
    public void giveItems() {
        if (player == null) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            oldInventory.put(i, inv.getItem(i));
        }
        inv.clear();

        ItemStack tracker = new ItemStack(Material.COMPASS);
        ItemStack decreaseSpeed = PacketUtils.getSkull("http://textures.minecraft.net/texture/118a2dd5bef0b073b13271a7eeb9cfea7afe8593c57a93821e43175572461812");
        ItemStack rewind = PacketUtils.getSkull("http://textures.minecraft.net/texture/864f779a8e3ffa231143fa69b96b14ee35c16d669e19c75fd1a7da4bf306c");
        ItemStack pauseResume = PacketUtils.getSkull("http://textures.minecraft.net/texture/b46f95582cef626b5562ed656b8a1ce877108d066635378f3269fea34a770494");
        ItemStack forward = PacketUtils.getSkull("http://textures.minecraft.net/texture/d9eccc5c1c79aa7826a15a7f5f12fb40328157c5242164ba2aef47e5de9a5cfc");
        ItemStack increaseSpeed = PacketUtils.getSkull("http://textures.minecraft.net/texture/d99f28332bcc349f42023c29e6e641f4b10a6b1e48718cae557466d51eb922");
        ItemStack resetReplay = PacketUtils.getSkull("http://textures.minecraft.net/texture/3a4fab3fd97eb7ecf48ab4fd327e093e886f4e217aab69585313c27a5035831a");

        applyMeta(tracker,
                ChatColor.GOLD + "Player Tracker",
                ChatColor.GRAY + "Track players during",
                ChatColor.GRAY + "the replay."
        );

        applyMeta(decreaseSpeed,
                ChatColor.RED + "Decrease Speed",
                ChatColor.GRAY + "Click to decrease",
                ChatColor.GRAY + "the playback speed."
        );

        applyMeta(rewind,
                ChatColor.GOLD + "Rewind",
                ChatColor.GRAY + "Click to rewind",
                ChatColor.GRAY + "the playback."
        );

        applyMeta(pauseResume,
                ChatColor.YELLOW + "Pause/Resume",
                ChatColor.GRAY + "Click to pause or",
                ChatColor.GRAY + "resume the playback."
        );

        applyMeta(forward,
                ChatColor.GREEN + "Fast Forward",
                ChatColor.GRAY + "Click to fast forward",
                ChatColor.GRAY + "the playback."
        );

        applyMeta(increaseSpeed,
                ChatColor.BLUE + "Increase Speed",
                ChatColor.GRAY + "Click to increase",
                ChatColor.GRAY + "the playback speed."
        );

        applyMeta(resetReplay,
                ChatColor.GOLD + "Reset Replay",
                ChatColor.GRAY + "Click to reset the",
                ChatColor.GRAY + "replay to the beginning."
        );

        inv.setItem(0, safeTag(tracker, "tracker"));
        inv.setItem(2, safeTag(decreaseSpeed, "decreaseSpeed"));
        inv.setItem(3, safeTag(rewind, "rewind"));
        inv.setItem(4, safeTag(pauseResume, "pauseResume"));
        inv.setItem(5, safeTag(forward, "forward"));
        inv.setItem(6, safeTag(increaseSpeed, "increaseSpeed"));
        inv.setItem(7, safeTag(resetReplay, "resetReplay"));
    }

    private void applyMeta(ItemStack item, String name, String... lore) {
        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
    }

    private ItemStack safeTag(ItemStack item, String value) {
        if (item == null) {
            return null;
        }

        return PacketUtils.setItemTag(item, "Replay-Control", value);
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
            Runnable action = controlActions.get(control);
            if (action != null) action.run();
            isInDelay = true;
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> isInDelay = false, 20L);
        }
    }

    private void setupControls() {
        controlActions.put("tracker", () -> new TrackerMenu(replaySession, player));
        controlActions.put("decreaseSpeed", () -> {
            replaySession.setSpeed(replaySession.getSpeed() - 5);
            for (Player player : replaySession.getViewers()) {
                PacketUtils.sendActionBar(player, ChatColor.RED + "Speed decreased to " + ChatColor.YELLOW + "x" + replaySession.getSpeedAsDouble());
            }
        });
        controlActions.put("rewind", () -> {
            replaySession.rewind(10);
            for (Player player : replaySession.getViewers()) {
                PacketUtils.sendActionBar(player, ChatColor.AQUA + "Rewound 10 seconds");
            }
        });
        controlActions.put("pauseResume", () -> {
            if (replaySession.isPaused()) {
                replaySession.resume();
                for (Player player : replaySession.getViewers()) {
                    PacketUtils.sendActionBar(player, ChatColor.GREEN + "Playback resumed");
                }
            } else {
                replaySession.pause();
                for (Player player : replaySession.getViewers()) {
                    PacketUtils.sendActionBar(player, ChatColor.RED + "Playback paused");
                }
            }
        });
        controlActions.put("forward", () -> {
            replaySession.fastForward(10);
            for (Player player : replaySession.getViewers()) {
                PacketUtils.sendActionBar(player, ChatColor.AQUA + "Fast forwarded 10 seconds");
            }
        });
        controlActions.put("increaseSpeed", () -> {
            replaySession.setSpeed(replaySession.getSpeed() + 5);
            for (Player player : replaySession.getViewers()) {
                PacketUtils.sendActionBar(player, ChatColor.GREEN + "Speed increased to " + ChatColor.YELLOW + "x" + replaySession.getSpeedAsDouble());
            }
        });
        controlActions.put("resetReplay", replaySession::reset);
    }
}
