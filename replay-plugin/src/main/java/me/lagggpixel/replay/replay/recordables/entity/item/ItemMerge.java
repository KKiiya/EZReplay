package me.lagggpixel.replay.replay.recordables.entity.item;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;

import java.util.Collections;
import java.util.UUID;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

/**
 * @author Lagggpixel
 * @since January 10, 2025
 */
public class ItemMerge extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final short targetId;
    @Writeable private final ItemStack newAmount;

    public ItemMerge(IRecording replay, Item entity, Item target) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.targetId = replay.getEntityIndex().getOrRegister(target.getUniqueId());
        this.newAmount = target.getItemStack().getAmount();
    }

    @Override
    public void play(IReplaySession replaySession) {
        try {
            int targetEntityId = targetId + 100000;
            int entityFakeId = entityId + 100000;
            
            EntityData<ItemStack> itemData = new EntityData<ItemStack>(8, EntityDataTypes.ITEMSTACK, newAmount);
            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(targetEntityId, Collections.singletonList(itemData));
            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityFakeId);
            for (Player viewer : replaySession.getViewers()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                user.sendPacket(destroyPacket);
                user.sendPacket(metadataPacket);
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
