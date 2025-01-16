package me.lagggpixel.replay.support.nms.recordable.entity.item;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
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

  private final String entityUUID;
  private final String targetUUID;

  public ItemMerge(IRecording replay, Item entity, Item target) {
    super(replay);
    this.entityUUID = entity.getUniqueId().toString();
    this.targetUUID = target.getUniqueId().toString();
  }

  @Override
  public void play(IReplaySession replaySession, Player player) {
    try {
      CraftItem entity = (CraftItem) replaySession.getSpawnedEntities().get(entityUUID);
      int entityId = entity.getEntityId();
      CraftItem target = (CraftItem) replaySession.getSpawnedEntities().get(targetUUID);

      PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entityId);
      ItemStack originalItem = target.getItemStack();
      originalItem.setAmount(entity.getItemStack().getAmount() + target.getItemStack().getAmount());
      target.setItemStack(originalItem);
      EntityItem nmsTarget = (EntityItem) target.getHandle();

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

}
