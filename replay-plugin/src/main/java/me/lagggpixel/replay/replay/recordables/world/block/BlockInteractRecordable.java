package me.lagggpixel.replay.replay.recordables.world.block;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.Vector3i;
import me.lagggpixel.replay.api.utils.block.BlockAction;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import org.bukkit.entity.Player;

/**
 * Records block interactions like doors opening, levers flipping, buttons pressing
 * 
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class BlockInteractRecordable extends Recordable {

    @Writeable private final int material;
    @Writeable private final byte data;
    @Writeable private final Vector3i blockPosition;
    @Writeable private final BlockAction actionType;
    @Writeable private final boolean playSound;

    public BlockInteractRecordable(IRecording replay, BlockCache cache, BlockAction actionType, boolean playSound) {
        super(replay);
        this.material = cache.getMaterial().ordinal();
        this.data = cache.getData();
        this.blockPosition = new Vector3i(cache.getX(), cache.getY(), cache.getZ());
        this.actionType = actionType;
        this.playSound = playSound;
    }

    @Override
    public void play(IReplaySession replaySession) {
        if (actionType == BlockAction.INTERACT) {
            // For interactions, we need to send the actual block state change
            sendBlockChange(replaySession.getViewers());
            
            // Play sound if enabled
            if (playSound) playInteractionSound(replaySession.getViewers());
        } else if (actionType == BlockAction.PLACE || actionType == BlockAction.BREAK) {
            // For place/break, just play the sound
            if (playSound) playPlaceBreakSound(replaySession.getViewers());
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {
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
    private void sendBlockChange(Iterable<Player> players) {
        // Send block change packet
        String materialName = MaterialType.values()[material].name();
        StateType stateType = StateTypes.getByName("minecraft:" + materialName.toLowerCase());
        if (stateType == null) return;
        WrappedBlockState blockState = WrappedBlockState.getDefaultState(stateType);
        com.github.retrooper.packetevents.util.Vector3i position = new com.github.retrooper.packetevents.util.Vector3i(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(position, blockState);
        for (Player player : players) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            user.sendPacket(packet);
        }
    }

    /**
     * Plays the interaction sound (door creak, lever click, etc.)
     */
    private void playInteractionSound(Iterable<Player> players) {
        String materialName = MaterialType.values()[material].name();
        StateType stateType = StateTypes.getByName("minecraft:" + materialName.toLowerCase());
        if (stateType == null) return;
        
        Sound sound = getSoundForInteraction(stateType, data);
        if (sound == null) return;
        
        com.github.retrooper.packetevents.util.Vector3i soundPosition = new com.github.retrooper.packetevents.util.Vector3i(blockPosition.getX() * 8, blockPosition.getY() * 8, blockPosition.getZ() * 8 );
        
        WrapperPlayServerSoundEffect soundPacket = new WrapperPlayServerSoundEffect(sound, SoundCategory.BLOCK, soundPosition, 1.0f, 1.0f);
        
        for (Player player : players) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            user.sendPacket(soundPacket);
        }
    }

    /**
     * Plays place/break sounds
     */
    private void playPlaceBreakSound(Iterable<Player> players) {
        String materialName = MaterialType.values()[material].name();
        StateType stateType = StateTypes.getByName("minecraft:" + materialName.toLowerCase());
        if (stateType == null) return;
        
        Sound sound = getSoundForPlaceBreak(stateType, actionType);
        if (sound == null) return;
        
        com.github.retrooper.packetevents.util.Vector3i soundPosition = new com.github.retrooper.packetevents.util.Vector3i(blockPosition.getX() * 8, blockPosition.getY() * 8, blockPosition.getZ() * 8);
        
        WrapperPlayServerSoundEffect soundPacket = new WrapperPlayServerSoundEffect(sound, SoundCategory.BLOCK, soundPosition, 1.0f, 0.8f);
        
        for (Player player : players) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            user.sendPacket(soundPacket);
        }
    }

    /**
     * Gets the appropriate sound for block interactions using PacketEvents StateType
     */
    private Sound getSoundForInteraction(StateType stateType, byte blockData) {
        String blockName = stateType.getName().toLowerCase();
        MaterialType materialType = stateType.getMaterialType();
        
        // Doors
        if (blockName.contains("door") && !blockName.contains("trapdoor")) {
            boolean doorOpen = (blockData & 0x4) != 0;
            if (materialType == MaterialType.METAL || blockName.contains("iron")) return doorOpen ? Sounds.BLOCK_IRON_DOOR_CLOSE : Sounds.BLOCK_IRON_DOOR_OPEN;
            else return doorOpen ? Sounds.BLOCK_WOODEN_DOOR_CLOSE : Sounds.BLOCK_WOODEN_DOOR_OPEN;
        }
        
        // Trapdoors
        if (blockName.contains("trapdoor")) {
            boolean trapdoorOpen = (blockData & 0x4) != 0;
            if (materialType == MaterialType.METAL || blockName.contains("iron")) return trapdoorOpen ? Sounds.BLOCK_IRON_TRAPDOOR_CLOSE : Sounds.BLOCK_IRON_TRAPDOOR_OPEN;
            else return trapdoorOpen ? Sounds.BLOCK_WOODEN_TRAPDOOR_CLOSE : Sounds.BLOCK_WOODEN_TRAPDOOR_OPEN;
        }
        
        // Fence Gates
        if (blockName.contains("fence_gate")) {
            boolean gateOpen = (blockData & 0x4) != 0;
            return gateOpen ? Sounds.BLOCK_FENCE_GATE_CLOSE : Sounds.BLOCK_FENCE_GATE_OPEN;
        }
        
        // Redstone components
        if (blockName.equals("lever")) return Sounds.BLOCK_LEVER_CLICK;
        
        
        if (blockName.contains("button")) {
            if (materialType == MaterialType.WOOD || materialType == MaterialType.NETHER_WOOD) return Sounds.BLOCK_WOODEN_BUTTON_CLICK_ON;
            else return Sounds.BLOCK_STONE_BUTTON_CLICK_ON;
        }
        
        // Pressure plates
        if (blockName.contains("pressure_plate")) {
            boolean pressed = (blockData & 0x1) != 0;
            if (materialType == MaterialType.WOOD || materialType == MaterialType.NETHER_WOOD) return pressed ? Sounds.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON : Sounds.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF;
            else if (materialType == MaterialType.METAL || materialType == MaterialType.HEAVY_METAL) return pressed ? Sounds.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON : Sounds.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF;
            else return pressed ? Sounds.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON : Sounds.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF;
        }
        
        // Chests
        if (blockName.equals("chest") || blockName.equals("trapped_chest")) return Sounds.BLOCK_CHEST_OPEN;
        if (blockName.equals("ender_chest")) return Sounds.BLOCK_ENDER_CHEST_OPEN;
        return null;
    }
    
    /**
     * Gets the appropriate sound for place/break actions using PacketEvents StateType and MaterialType
     */
    private Sound getSoundForPlaceBreak(StateType stateType, BlockAction action) {
        boolean isBreak = action == BlockAction.BREAK;
        MaterialType materialType = stateType.getMaterialType();
        
        // Use MaterialType enum from PacketEvents for proper sound selection
        switch (materialType) {
            case WOOD:
            case NETHER_WOOD:
            case BAMBOO:
            case BAMBOO_SAPLING:
                return isBreak ? Sounds.BLOCK_WOOD_BREAK : Sounds.BLOCK_WOOD_PLACE;
            case METAL:
            case HEAVY_METAL:
                return isBreak ? Sounds.BLOCK_METAL_BREAK : Sounds.BLOCK_METAL_PLACE;
            case GLASS:
            case BUILDABLE_GLASS:
                return isBreak ? Sounds.BLOCK_GLASS_BREAK : Sounds.BLOCK_GLASS_PLACE;
            case SAND:
                return isBreak ? Sounds.BLOCK_SAND_BREAK : Sounds.BLOCK_SAND_PLACE;
            case DIRT:
                return isBreak ? Sounds.BLOCK_ROOTED_DIRT_BREAK : Sounds.BLOCK_ROOTED_DIRT_PLACE;
            case GRASS:
            case LEAVES:
            case PLANT:
            case REPLACEABLE_PLANT:
            case WATER_PLANT:
            case REPLACEABLE_WATER_PLANT:
            case SPONGE:
                return isBreak ? Sounds.BLOCK_GRASS_BREAK : Sounds.BLOCK_GRASS_PLACE;
            case WOOL:
            case CLOTH_DECORATION:
                return isBreak ? Sounds.BLOCK_WOOL_BREAK : Sounds.BLOCK_WOOL_PLACE;
            case SNOW:
            case TOP_SNOW:
                return isBreak ? Sounds.BLOCK_SNOW_BREAK : Sounds.BLOCK_SNOW_PLACE;
            case CLAY:
                return isBreak ? Sounds.BLOCK_GRAVEL_BREAK : Sounds.BLOCK_GRAVEL_PLACE;
            case SCULK:
                return isBreak ? Sounds.BLOCK_SCULK_BREAK : Sounds.BLOCK_SCULK_PLACE;
            default:
                return isBreak ? Sounds.BLOCK_STONE_BREAK : Sounds.BLOCK_STONE_PLACE;
        }
    }
}
