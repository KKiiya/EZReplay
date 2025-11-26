package me.lagggpixel.replay.replay.recordables.world.block;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.block.AbstractBlockBreaker;
import me.lagggpixel.replay.api.utils.block.DigType;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockDigRecordable extends Recordable {
    @Writeable private final DigType digType;
    @Writeable private final Vector3d position;
    @Writeable private final short playerId;

    public BlockDigRecordable(IRecording replay, Player player, Vector3d pos, DigType digType) {
        super(replay);
        this.digType = digType;
        this.position = pos;
        this.playerId = replay.getEntityIndex().getOrRegister(player.getUniqueId());
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {

    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BLOCK_DIG;
    }

    private static class DigTask extends BukkitRunnable {

        private final IReplaySession replaySession;
        private final AbstractBlockBreaker blockBreaker;
        private final Player player;

        public DigTask(IReplaySession replaySession, AbstractBlockBreaker blockBreaker, Player player) {
            this.replaySession = replaySession;
            this.blockBreaker = blockBreaker;
            this.player = player;
        }

        @Override
        public void run() {
            int currentTick = (int) (System.currentTimeMillis()/50);
            int damage = (int) blockBreaker.getDamage(currentTick);

            int entityId = replaySession.getSpawnedEntities().get("").getEntityId();
            blockBreaker.setBlockDamage(entityId, damage, player);
        }
    }
}
