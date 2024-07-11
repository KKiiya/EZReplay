package me.lagggpixel.replay.support.nms.recordable.arena;

import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.generator.IGeneratorAnimation;
import com.tomkeuper.bedwars.api.entity.GeneratorHolder;
import com.tomkeuper.bedwars.api.server.VersionSupport;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IGeneratorAdd;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class GeneratorAddRecordable extends Recordable implements IGeneratorAdd {

    private final Vector3d location;
    private final ItemStack helmet;

    public GeneratorAddRecordable(IRecording replay, IGenerator gen) {
        super(replay);
        this.location = Vector3d.fromBukkitLocation(gen.getLocation());
        this.helmet = gen.getHologramHolder().getHelmet();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        VersionSupport vs = v1_8_R3.getInstance().getPlugin().getBedWarsAPI().getVersionSupport();
        GeneratorHolder gh = new GeneratorHolder(location.toBukkitLocation(), helmet);
        IGeneratorAnimation genAnim = vs.createDefaultGeneratorAnimation(gh.getArmorStand());
        AnimationTask animTask = new AnimationTask(genAnim);
        animTask.runTaskTimer(v1_8_R3.getInstance().getPlugin(), 0, 1L);
        replaySession.startedTasks().add(animTask);
    }

    private static class AnimationTask extends BukkitRunnable {

        private final IGeneratorAnimation animation;

        public AnimationTask(IGeneratorAnimation animation) {
            this.animation = animation;
        }

        @Override
        public void run() {
            animation.run();
        }
    }
}
