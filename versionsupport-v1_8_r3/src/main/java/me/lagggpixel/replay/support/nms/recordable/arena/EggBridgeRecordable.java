package me.lagggpixel.replay.support.nms.recordable.arena;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IEggBridge;
import me.lagggpixel.replay.api.utils.block.IBlockData;
import me.lagggpixel.replay.support.nms.utils.BlockData;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

public class EggBridgeRecordable extends Recordable implements IEggBridge {
    private final IBlockData blockData;

    public EggBridgeRecordable(IRecording replay, Block block) {
        super(replay);
        this.blockData = new BlockData(block);
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        Location position = blockData.getPosition().toBukkitLocation();
        BlockPosition blockPosition = new BlockPosition(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        CraftBlockState state = new CraftBlockState(position.getBlock());
        MaterialData data = new MaterialData(blockData.getMaterial(), blockData.getData());
        state.setData(data);

        PacketPlayOutBlockChange blockChange = new PacketPlayOutBlockChange(world, blockPosition);

        v1_8_R3.sendPacket(player, blockChange);
    }

    @Override
    public IBlockData getBlockData() {
        return blockData;
    }
}
