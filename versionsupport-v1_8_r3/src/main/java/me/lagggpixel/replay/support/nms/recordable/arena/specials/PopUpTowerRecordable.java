package me.lagggpixel.replay.support.nms.recordable.arena.specials;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IPopUpTower;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class PopUpTowerRecordable extends Recordable implements IPopUpTower {
    private final org.bukkit.Material material;
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final byte data;
    private final BlockPosition blockPosition;

    public PopUpTowerRecordable(IRecording replay, Block block, Sound sound, float volume, float pitch) {
        super(replay);
        this.material = block.getType();
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.data = block.getData();
        this.blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Block nmsBlock = CraftMagicNumbers.getBlock(material);

        PacketPlayOutBlockChange blockChange = new PacketPlayOutBlockChange(world, blockPosition);
        PacketPlayOutNamedSoundEffect soundEffect = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(sound), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), volume, pitch);

        blockChange.block = nmsBlock.fromLegacyData(data);

        v1_8_R3.sendPackets(player, blockChange, soundEffect);
    }
}
