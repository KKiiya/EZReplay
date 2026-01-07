package me.lagggpixel.replay.replay.recordables.entity.item;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Lagggpixel
 * @since January 10, 2025
 */
public class ItemMerge extends Recordable {

    private final short entityId;
    private final short targetId;

    public ItemMerge(IRecording replay, Item entity, Item target) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.targetId = replay.getEntityIndex().getOrRegister(target.getUniqueId());
    }

    @Override
    public void play(IReplaySession replaySession) {
        try {
            Item entity = (Item) replaySession.getSpawnedEntities().get(entityId);
            Item target = (Item) replaySession.getSpawnedEntities().get(targetId);
            int entityFakeId = entityId + 100000;
            
            // Update target item stack amount
            ItemStack itemStack = entity.getItemStack();
            ItemStack targetItemStack = target.getItemStack();
            targetItemStack.setAmount(targetItemStack.getAmount() + itemStack.getAmount());
            target.setItemStack(targetItemStack);
            
            // Send destroy packet for merged entity
            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityFakeId);
            for (Player viewer : replaySession.getViewers()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                user.sendPacket(destroyPacket);
            }
        } catch (ClassCastException ex) {
            // Handle merge of non-item entities
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ITEM_MERGE;
    }
}
