package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.world.block.IBlockDigRecordable;
import me.lagggpixel.replay.api.utils.block.AbstractBlockBreaker;
import me.lagggpixel.replay.api.utils.block.DigType;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockDigRecordable extends Recordable implements IBlockDigRecordable {
    private final AbstractBlockBreaker blockBreaker;
    private final DigType digType;

    public BlockDigRecordable(IRecording replay, AbstractBlockBreaker blockBreaker, DigType digType) {
        super(replay);
        this.blockBreaker = blockBreaker;
        this.digType = digType;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        if (digType == DigType.DIG_START) {
            BukkitRunnable runnable = new DigTask(replaySession, blockBreaker, this, player);
            runnable.runTaskTimer(v1_8_R3.getInstance().getPlugin(), 0L, 1L);
            replaySession.startedTasks().add(runnable);
        } else if (digType == DigType.DIG_STOP) {

        }
    }

    @Override
    public String getUUID() {
        return blockBreaker.getEntity().getUniqueId().toString();
    }


    private static class DigTask extends BukkitRunnable {

        private final IReplaySession replaySession;
        private final AbstractBlockBreaker blockBreaker;
        private final IBlockDigRecordable blockDigRecordable;
        private final Player player;

        public DigTask(IReplaySession replaySession, AbstractBlockBreaker blockBreaker, IBlockDigRecordable blockDigRecordable, Player player) {
            this.replaySession = replaySession;
            this.blockBreaker = blockBreaker;
            this.blockDigRecordable = blockDigRecordable;
            this.player = player;
        }

        @Override
        public void run() {
            int currentTick = (int) (System.currentTimeMillis()/50);
            int damage = (int) blockBreaker.getDamage(currentTick);

            int entityId = replaySession.getSpawnedEntities().get(blockDigRecordable.getUUID()).getEntityId();
            blockBreaker.setBlockDamage(entityId, damage, player);
        }
    }
}
