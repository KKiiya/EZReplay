package me.lagggpixel.replay.replay.tasks;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.item.ItemData;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.List;

public class EquipmentTrackerTask implements Runnable {
    private final static List<Player> trackedPlayers = new ArrayList<>();
    private final IRecording replay;
    private final Player player;
    private ItemData[] previousEquipment;

    public EquipmentTrackerTask(IRecording replay, Player player) {
        this.replay = replay;
        this.player = player;
        this.previousEquipment = getCurrentEquipment(player);
        trackedPlayers.add(player);
    }

    @Override
    public void run() {
        IFrame lastFrame = replay.getLastFrame();
        ItemData[] currentEquipment = getCurrentEquipment(player);

        for (int i = 0; i < currentEquipment.length; i++) {
            if (!currentEquipment[i].equals(previousEquipment[i])) {
                Recordable recordable = Replay.getInstance().getVersionSupport().createEquipmentRecordable(replay, player);
                lastFrame.addRecordable(recordable);
            }
        }
        this.previousEquipment = currentEquipment;
    }

    private ItemData[] getCurrentEquipment(Player player) {
        ItemData[] currentEquipment = new ItemData[5];
        currentEquipment[0] = new ItemData(player.getInventory().getHelmet());
        currentEquipment[1] = new ItemData(player.getInventory().getChestplate());
        currentEquipment[2] = new ItemData(player.getInventory().getLeggings());
        currentEquipment[3] = new ItemData(player.getInventory().getBoots());
        currentEquipment[4] = new ItemData(player.getEquipment().getItemInHand());
        return currentEquipment;
    }

    public static boolean isTracked(Player player) {
        return trackedPlayers.contains(player);
    }
}
