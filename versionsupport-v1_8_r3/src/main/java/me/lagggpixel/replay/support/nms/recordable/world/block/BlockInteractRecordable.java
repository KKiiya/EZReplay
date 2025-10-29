package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.block.BlockAction;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

/**
 * Records block interactions like doors opening, levers flipping, buttons pressing
 * 
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class BlockInteractRecordable extends Recordable {

    @Writeable private final BlockCache blockCache;
    @Writeable private final BlockAction actionType;
    @Writeable private final boolean playSound;

    public BlockInteractRecordable(IRecording replay, BlockCache cache, BlockAction actionType, boolean playSound) {
        super(replay);
        this.blockCache = cache;
        this.actionType = actionType;
        this.playSound = playSound;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        if (actionType == BlockAction.INTERACT) {
            // For interactions, we need to send the actual block state change
            sendBlockChange(player);
            
            // Play sound if enabled
            if (playSound) {
                playInteractionSound(player);
            }
        } else if (actionType == BlockAction.PLACE || actionType == BlockAction.BREAK) {
            // For place/break, just play the sound
            if (playSound) {
                playPlaceBreakSound(player);
            }
        }
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        // For unplay, we should reverse the interaction
        // But this is typically handled by BlockUpdateRecordable
        // which restores the old block state
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BLOCK_INTERACT;
    }

    /**
     * Sends the actual block state change to the player
     * This is what makes doors actually open/close properly
     */
    private void sendBlockChange(Player player) {
        // Get NMS block data
        Block nmsBlock = CraftMagicNumbers.getBlock(blockCache.getMaterial());
        IBlockData blockData = nmsBlock.fromLegacyData(blockCache.getData());
        BlockPosition blockPosition = new BlockPosition(
            blockCache.getX(),
            blockCache.getY(),
            blockCache.getZ()
        );

        // Send block change packet
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(
            ((CraftWorld) player.getWorld()).getHandle(),
            blockPosition
        );
        
        // Use reflection to set the block data in the packet
        try {
            java.lang.reflect.Field blockField = packet.getClass().getDeclaredField("block");
            blockField.setAccessible(true);
            blockField.set(packet, blockData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        v1_8_R3.sendPacket(player, packet);
    }

    /**
     * Plays the interaction sound (door creak, lever click, etc.)
     */
    private void playInteractionSound(Player player) {
        Block nmsBlock = CraftMagicNumbers.getBlock(blockCache.getMaterial());
        Block.StepSound stepSound = nmsBlock.stepSound;
        BlockPosition blockPosition = new BlockPosition(
            blockCache.getX(),
            blockCache.getY(),
            blockCache.getZ()
        );

        String soundName = getSoundForInteraction(blockCache.getMaterial());
        if (soundName == null) {
            // Fallback to step sound
            soundName = stepSound.getStepSound();
        }
        
        PacketPlayOutNamedSoundEffect soundPacket = new PacketPlayOutNamedSoundEffect(
            soundName,
            blockPosition.getX() + 0.5,
            blockPosition.getY() + 0.5,
            blockPosition.getZ() + 0.5,
            stepSound.getVolume1() * 0.75f,
            stepSound.getVolume2()
        );
        
        v1_8_R3.sendPacket(player, soundPacket);
    }

    /**
     * Plays place/break sounds
     */
    private void playPlaceBreakSound(Player player) {
        Block nmsBlock = CraftMagicNumbers.getBlock(blockCache.getMaterial());
        Block.StepSound stepSound = nmsBlock.stepSound;
        BlockPosition blockPosition = new BlockPosition(
            blockCache.getX(),
            blockCache.getY(),
            blockCache.getZ()
        );
        
        String soundName;
        if (actionType == BlockAction.BREAK) {
            soundName = stepSound.getBreakSound();
        } else {
            soundName = stepSound.getPlaceSound();
        }
        
        PacketPlayOutNamedSoundEffect soundPacket = new PacketPlayOutNamedSoundEffect(
            soundName,
            blockPosition.getX() + 0.5,
            blockPosition.getY() + 0.5,
            blockPosition.getZ() + 0.5,
            stepSound.getVolume1(),
            stepSound.getVolume2() / 1.3f
        );
        
        v1_8_R3.sendPacket(player, soundPacket);
    }

    /**
     * Gets the appropriate sound for block interactions
     */
    private String getSoundForInteraction(Material material) {
        switch (material) {
            // Doors
            case WOODEN_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                return "random.door_open"; // or door_close depending on state
            
            case IRON_DOOR_BLOCK:
                return "random.door_open";
            
            // Trapdoors
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
                return "random.door_open";
            
            // Gates
            case FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
                return "random.door_open";
            
            // Redstone components
            case LEVER:
                return "random.click";
            
            case STONE_BUTTON:
            case WOOD_BUTTON:
                return "random.click";
            
            case STONE_PLATE:
            case WOOD_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
                return "random.click";
            
            // Chests
            case CHEST:
            case TRAPPED_CHEST:
                return "random.chestopen";
            
            case ENDER_CHEST:
                return "random.chestopen";
            
            default:
                return null;
        }
    }
}