package me.lagggpixel.replay.replay.recordables.world.block;

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

    @Writeable private final Material material;
    @Writeable private final byte data;
    @Writeable private final BlockPosition blockPosition;
    @Writeable private final BlockAction actionType;
    @Writeable private final boolean playSound;

    public BlockInteractRecordable(IRecording replay, BlockCache cache, BlockAction actionType, boolean playSound) {
        super(replay);
        this.material = cache.getMaterial();
        this.data = cache.getData();
        this.blockPosition = new BlockPosition(cache.getX(), cache.getY(), cache.getZ());
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
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        IBlockData blockData = nmsBlock.fromLegacyData(data);
        
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
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        Block.StepSound stepSound = nmsBlock.stepSound;
        
        String soundName = getSoundForInteraction(material);
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
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        Block.StepSound stepSound = nmsBlock.stepSound;
        
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
                // Check door state to determine open/close sound
                boolean isOpen = (data & 0x4) != 0;
                return isOpen ? "random.door_close" : "random.door_open";
            
            case IRON_DOOR_BLOCK:
                return "random.door_open";
            
            // Trapdoors
            case TRAP_DOOR:
                boolean trapdoorOpen = (data & 0x4) != 0;
                return trapdoorOpen ? "random.door_close" : "random.door_open";
                
            case IRON_TRAPDOOR:
                return "random.door_open";
            
            // Gates
            case FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
                boolean gateOpen = (data & 0x4) != 0;
                return gateOpen ? "random.door_close" : "random.door_open";
            
            // Redstone components - FIXED
            case LEVER:
                // Lever has distinct on/off sounds
                boolean leverOn = (data & 0x8) != 0;
                return leverOn ? "random.click" : "random.click";
            
            case STONE_BUTTON:
                return "random.click";
                
            case WOOD_BUTTON:
                return "random.wood_click";
            
            // Pressure plates - FIXED
            case STONE_PLATE:
                // Stone pressure plate
                return "random.click";
                
            case WOOD_PLATE:
                // Wood pressure plate
                return "random.wood_click";
                
            case GOLD_PLATE:
            case IRON_PLATE:
                // Metal pressure plates
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