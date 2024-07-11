package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.IBlockDigRecordable;
import me.lagggpixel.replay.api.utils.block.AbstractBlockBreaker;
import org.bukkit.entity.Player;

public class BlockDigRecordable extends Recordable implements IBlockDigRecordable {
    private final AbstractBlockBreaker blockBreaker;
    private final int damage;

    public BlockDigRecordable(IRecording replay, AbstractBlockBreaker blockBreaker) {
        super(replay);
        int currentTick = (int) (System.currentTimeMillis()/50);
        float damage = blockBreaker.getDamage(currentTick);

        this.blockBreaker = blockBreaker;
        this.damage = (int) damage;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        int entityId = replaySession.getFakeEntity(getUUID()).getEntityId();
        blockBreaker.setBlockDamage(entityId, damage, player);
    }

    @Override
    public String getUUID() {
        return blockBreaker.getEntity().getUniqueId().toString();
    }

    @Override
    public int getDamage() {
        return damage;
    }
}
