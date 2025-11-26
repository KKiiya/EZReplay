package me.lagggpixel.replay.replay.recordables.entity.item;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
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
    public void play(IReplaySession replaySession, Player player) {
        try {
            CraftItem entity = (CraftItem) replaySession.getSpawnedEntities().get(entityId);
            CraftItem target = (CraftItem) replaySession.getSpawnedEntities().get(targetId);
            EntityItem nmsTarget = (EntityItem) target.getHandle();
            int entityId = entity.getEntityId();

            ItemStack itemStack = entity.getItemStack();
            ItemStack targetItemStack = target.getItemStack();
            targetItemStack.setAmount(targetItemStack.getAmount() + itemStack.getAmount());
            target.setItemStack(targetItemStack);

            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entityId);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(nmsTarget.getId(), nmsTarget.getDataWatcher(), true);
            v1_8_R3.sendPackets(player, destroy, metadata);
        } catch (ClassCastException ex) {
            v1_8_R3.getInstance().getPlugin().getLogger().warning("Attempting to merge items of not type item stack, the item will not merge.");
            v1_8_R3.getInstance().getPlugin().getLogger().warning("It seems like the recording has a problem, the recording will continue to play, there may be inaccuracies in the recording.");
        }
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ITEM_MERGE;
    }
}
