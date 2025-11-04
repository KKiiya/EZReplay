package me.lagggpixel.replay.listeners.world;

import com.cryptomorin.xseries.XMaterial;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.events.block.BlockChangeEvent;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.api.utils.block.BlockEventType;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.utils.block.BlockAction;
import me.lagggpixel.replay.replay.ReplayManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class BlockListener implements Listener {

    // Track blocks pending update to batch multi-block structures
    private final Map<World, Map<Location, PendingBlockUpdate>> pendingUpdates = new HashMap<>();
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockEvent(BlockChangeEvent e) {
        Block block = e.getBlock();
        World world = block.getWorld();
        XMaterial blockType = XMaterial.matchXMaterial(block.getType());

        // CRITICAL: Skip server-managed blocks
        if (isServerManagedBlock(blockType, e.getEventType())) {
            return;
        }

        // Skip liquid flow events (but not source placement/removal)
        if (e.getEventType() == BlockEventType.FROM_TO) {
            return; // Let physics handle during replay
        }

        IRecording recording = ReplayManager.getInstance().getActiveRecording(world);
        if (recording == null) return;

        IFrame frame = recording.getLastFrame();
        BlockAction action = determineBlockAction(e, block, recording);
        if (action == null) return;

        // For certain events, delay to capture complete multi-block structures
        if (shouldBatchUpdate(e.getEventType(), blockType)) {
            scheduleBatchedUpdate(recording, frame, block, action, e);
        } else {
            // Immediate recording for player actions
            scheduleImmediateUpdate(recording, frame, block, action, e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (e.isCancelled()) return;
        
        Block block = e.getBlock();
        XMaterial current = XMaterial.matchXMaterial(block.getType());
        XMaterial changed = XMaterial.matchXMaterial(e.getChangedType());
        
        // CRITICAL: Comprehensive physics filtering
        if (shouldIgnorePhysics(block, current, changed)) {
            return;
        }
        
        // Don't record physics for server-managed blocks
        if (isServerManagedBlock(current, BlockEventType.PHYSICS)) {
            return;
        }
        
        e.setCancelled(callBlockChangeEvent(null, block, BlockEventType.PHYSICS));
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent e) {
        // Never record redstone state changes - server handles this
        // Only record if it's a player-placed redstone component
        return;
    }

    @EventHandler
    public void onCanBuild(BlockCanBuildEvent e) {
        // Purely informational event, don't record
        return;
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        if (e.isCancelled()) return;
        // Fire, mushrooms, etc. - record these
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.SPREAD));
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent e) {
        if (e.isCancelled()) return;
        // Ice forming, snow accumulation, etc.
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FORM));
    }

    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        if (e.isCancelled()) return;
        // Beds, doors placed by player - record main block
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.MULTI_PLACE));
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        if (e.isCancelled()) return;
        
        Block from = e.getBlock();
        Material type = from.getType();
        
        // Only record SOURCE block placement, not flow
        if (isLiquidSource(from)) {
            // This is a source block spreading - record the TO block becoming a source
            Block to = e.getToBlock();
            callBlockChangeEvent(null, to, BlockEventType.FROM_TO);
        }
        // Don't record flowing liquid updates
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.isCancelled()) return;
        // Snowman creating snow, etc.
        e.setCancelled(callBlockChangeEvent(e.getEntity(), e.getBlock(), BlockEventType.ENTITY_FORM));
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        // Don't record damage, only final break
        return;
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent e) {
        if (e.isCancelled()) return;
        // Crops, saplings growing - batch these
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.GROW));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.BURN));
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        // Don't record dispenser operation, only item spawn
        return;
    }

    @EventHandler
    public void onBlockExp(BlockExpEvent e) {
        // Don't record exp drops, handled by entity system
        return;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) return;
        
        // Record explosion origin
        callBlockChangeEvent(null, e.getBlock(), BlockEventType.EXPLODE);
        
        // Batch record all destroyed blocks
        World world = e.getBlock().getWorld();
        IRecording recording = ReplayManager.getInstance().getActiveRecording(world);
        if (recording == null) return;
        
        IFrame frame = recording.getLastFrame();
        
        // Schedule all exploded blocks to be recorded together
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            for (Block block : e.blockList()) {
                recording.addBlockUpdate(frame, block);
            }
        });
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        if (e.isCancelled()) return;
        // Ice melting, coral dying, etc.
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FADE));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        
        // If breaking a liquid source, record it explicitly
        if (isLiquidSource(block)) {
            // This will clear the source and let physics handle flow removal
            callBlockChangeEvent(e.getPlayer(), block, BlockEventType.BREAK);
        } else {
            callBlockChangeEvent(e.getPlayer(), block, BlockEventType.BREAK);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        
        // Explicitly track source block placement
        if (isLiquidSource(block)) {
            callBlockChangeEvent(e.getPlayer(), block, BlockEventType.PLACE);
        } else {
            callBlockChangeEvent(e.getPlayer(), block, BlockEventType.PLACE);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled()) return;
        
        Block piston = e.getBlock();
        World world = piston.getWorld();
        IRecording recording = ReplayManager.getInstance().getActiveRecording(world);
        if (recording == null) return;
        
        // Record piston base state change
        callBlockChangeEvent(null, piston, BlockEventType.PISTON_EXTEND);
        
        // Schedule recording ALL affected blocks (pushed blocks + new positions + extension)
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            IFrame frame = recording.getLastFrame();
            BlockFace direction = e.getDirection();
            
            // Record piston extension block
            Block extension = piston.getRelative(direction);
            if (extension.getType() == Material.PISTON_HEAD ||
                extension.getType() == Material.MOVING_PISTON) {
                recording.addBlockUpdate(frame, extension);
            }
            
            // Record all pushed blocks (old and new positions)
            for (Block pushedBlock : e.getBlocks()) {
                recording.addBlockUpdate(frame, pushedBlock); // Old position
                Block newPos = pushedBlock.getRelative(direction);
                recording.addBlockUpdate(frame, newPos); // New position
            }
        });
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) return;
        
        Block piston = e.getBlock();
        World world = piston.getWorld();
        IRecording recording = ReplayManager.getInstance().getActiveRecording(world);
        if (recording == null) return;
        
        // Record piston base state change
        callBlockChangeEvent(null, piston, BlockEventType.PISTON_RETRACT);
        
        // Schedule recording retraction
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            IFrame frame = recording.getLastFrame();
            BlockFace direction = e.getDirection();
            
            // Record removed extension
            Block extension = piston.getRelative(direction);
            recording.addBlockUpdate(frame, extension);
            
            // Record pulled blocks (for sticky pistons)
            if (e.isSticky()) {
                for (Block pulledBlock : e.getBlocks()) {
                    recording.addBlockUpdate(frame, pulledBlock); // Old position
                    Block newPos = pulledBlock.getRelative(direction.getOppositeFace());
                    recording.addBlockUpdate(frame, newPos); // New position
                }
            }
        });
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.IGNITE));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        
        Block block = e.getClickedBlock();
        
        if (!Replay.getInstance().getVersionSupport().isInteractable(block)) return;
        if (e.useInteractedBlock() == Event.Result.DENY) return;
        
        Player player = e.getPlayer();
        List<Block> affectedBlocks = getAffectedBlocks(block);
        
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            for (Block affectedBlock : affectedBlocks) {
                callBlockChangeEvent(player, affectedBlock, BlockEventType.INTERACT);
            }
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Determines if a block is managed by server physics and shouldn't be recorded
     */
    private boolean isServerManagedBlock(XMaterial type, BlockEventType eventType) {
        // Piston extensions are purely visual
        if (type == XMaterial.PISTON_HEAD || type == XMaterial.MOVING_PISTON) {
            return true;
        }
        
        // Flowing liquids (not sources)
        if (type == XMaterial.WATER || type == XMaterial.LAVA) {
            return false; // Sources are recorded
        }
        
        // Redstone components in powered states
        if (isRedstoneComponent(type)) {
            // Only skip if it's a state change, not player placement
            return eventType == BlockEventType.PHYSICS || 
                   eventType == BlockEventType.REDSTONE;
        }
        
        // Fire spread is server-managed
        return type == XMaterial.FIRE && eventType == BlockEventType.PHYSICS;
    }

    /**
     * Check if block is a redstone component
     */
    private boolean isRedstoneComponent(XMaterial type) {
        return type == XMaterial.REDSTONE_WIRE ||
               type == XMaterial.REDSTONE_LAMP ||
               type == XMaterial.COMPARATOR ||
               type == XMaterial.REPEATER ||
               type == XMaterial.REDSTONE_TORCH ||
               type == XMaterial.REDSTONE_WALL_TORCH ||
               type == XMaterial.REDSTONE ||
               type == XMaterial.POWERED_RAIL ||
               type == XMaterial.ACTIVATOR_RAIL ||
               type == XMaterial.DETECTOR_RAIL;
    }

    /**
     * Check if block is a liquid source
     */
    private boolean isLiquidSource(Block block) {
        XMaterial type = XMaterial.matchXMaterial(block.getType());
        byte data = block.getData();
        
        // Data value 0 = source block for liquids
        return (type == XMaterial.WATER || type == XMaterial.LAVA ||
                type.toString().contains("STATIONARY")) &&
               data == 0;
    }

    /**
     * Comprehensive physics event filtering
     */
    private boolean shouldIgnorePhysics(Block block, XMaterial current, XMaterial changed) {
        // No actual change
        if (current == changed) {
            return true;
        }
        
        // Liquid physics
        if (current == XMaterial.WATER || current == XMaterial.LAVA ||
            block.toString().contains("STATIONARY")) {
            return true;
        }
        
        // Redstone component updates
        if (isRedstoneComponent(current)) {
            return true;
        }
        
        // Piston extensions
        if (current == XMaterial.PISTON_HEAD || current == XMaterial.MOVING_PISTON) {
            return true;
        }
        
        // Grass/Mycelium color changes
        if (current == XMaterial.GRASS_BLOCK && changed == XMaterial.GRASS_BLOCK) {
            return true;
        }

        // Fire physics
        return current == XMaterial.FIRE;
    }

    /**
     * Determine if update should be batched (for multi-block structures)
     */
    private boolean shouldBatchUpdate(BlockEventType eventType, XMaterial type) {
        // Tree growth - needs batching
        if (eventType == BlockEventType.GROW && 
            (type == XMaterial.RED_MUSHROOM ||
             type == XMaterial.BROWN_MUSHROOM || isSappling(type))) {
            return true;
        }
        
        // Structure generation
        return eventType == BlockEventType.FORM || eventType == BlockEventType.ENTITY_FORM;
    }

    private boolean isSappling(XMaterial type) {
        return type == XMaterial.OAK_SAPLING || type == XMaterial.SPRUCE_SAPLING ||
               type == XMaterial.BIRCH_SAPLING || type == XMaterial.JUNGLE_SAPLING ||
               type == XMaterial.ACACIA_SAPLING || type == XMaterial.DARK_OAK_SAPLING;
    }

    /**
     * Schedule batched update with delay to capture complete structure
     */
    private void scheduleBatchedUpdate(IRecording recording, IFrame frame, Block block, 
                                       BlockAction action, BlockChangeEvent e) {
        // Delay by 2 ticks to capture complete multi-block structure
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
            recording.addBlockUpdate(frame, block);
            
            boolean playSound = shouldPlaySound(e, block);
            BlockCache cache = new BlockCache(block);
            Recordable recordable = Replay.getInstance().getVersionSupport()
                    .createBlockRecordable(recording, cache, action, playSound);
            frame.addRecordable(recordable);
        }, 2L);
    }

    /**
     * Schedule immediate update for player actions
     */
    private void scheduleImmediateUpdate(IRecording recording, IFrame frame, Block block,
                                        BlockAction action, BlockChangeEvent e) {
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            recording.addBlockUpdate(frame, block);
            
            boolean playSound = shouldPlaySound(e, block);
            BlockCache cache = new BlockCache(block);
            Recordable recordable = Replay.getInstance().getVersionSupport()
                    .createBlockRecordable(recording, cache, action, playSound);
            frame.addRecordable(recordable);
        });
    }

    /**
     * Determine if sound should play
     */
    private boolean shouldPlaySound(BlockChangeEvent e, Block block) {
        if (e.getEventType() == BlockEventType.INTERACT) {
            if (isDoor(block.getType())) {
                byte data = block.getData();
                boolean isUpperHalf = (data & 0x8) != 0;
                return !isUpperHalf;
            }
        }
        return true;
    }

    private BlockAction determineBlockAction(BlockChangeEvent e, Block block, IRecording recording) {
        BlockAction action;
        if (e.getEventType() == BlockEventType.BREAK) {
            action = BlockAction.BREAK;
            if (e.getEntity() instanceof Player) {
                addSwingAnimation((Player) e.getEntity(), recording);
            }
        } else if (e.getEventType() == BlockEventType.PLACE) {
            action = BlockAction.PLACE;
            if (e.getEntity() instanceof Player) {
                addSwingAnimation((Player) e.getEntity(), recording);
            }
        } else if (e.getEventType() == BlockEventType.INTERACT) {
            action = BlockAction.INTERACT;
            if (e.getEntity() instanceof Player) {
                addSwingAnimation((Player) e.getEntity(), recording);
            }
        } else if (e.getEventType() == BlockEventType.PISTON_EXTEND || 
                   e.getEventType() == BlockEventType.PISTON_RETRACT) {
            action = BlockAction.INTERACT;
        } else {
            action = BlockAction.UPDATE;
        }
        return action;
    }

    private void addSwingAnimation(Player player, IRecording recording) {
        Recordable animation = Replay.getInstance().getVersionSupport()
                .createAnimationRecordable(recording, player, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(animation);
    }

    private boolean callBlockChangeEvent(Entity entity, Block block, BlockEventType eventType) {
        BlockChangeEvent event = new BlockChangeEvent(entity, block, eventType);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    private List<Block> getAffectedBlocks(Block block) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(block);
        
        Material type = block.getType();
        
        if (isDoor(type)) {
            Block otherHalf = getOtherDoorHalf(block);
            if (otherHalf != null) {
                blocks.add(otherHalf);
            }
        } else if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
            Block adjacent = getAdjacentChest(block);
            if (adjacent != null) {
                blocks.add(adjacent);
            }
        }
        
        return blocks;
    }

    private boolean isDoor(Material type) {
        if (type == null) return false;
        if (type.toString().contains("TRAP")) return false;
        return type.toString().contains("DOOR");
    }

    private Block getOtherDoorHalf(Block door) {
        byte data = door.getData();
        boolean isUpperHalf = (data & 0x8) != 0;
        
        if (isUpperHalf) {
            Block lower = door.getRelative(BlockFace.DOWN);
            if (isDoor(lower.getType())) {
                return lower;
            }
        } else {
            Block upper = door.getRelative(BlockFace.UP);
            if (isDoor(upper.getType())) {
                return upper;
            }
        }
        
        return null;
    }

    private Block getAdjacentChest(Block chest) {
        Material chestType = chest.getType();
        
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block adjacent = chest.getRelative(face);
            if (adjacent.getType() == chestType) {
                return adjacent;
            }
        }
        
        return null;
    }
    
    /**
     * Inner class to track pending updates
     */
    private static class PendingBlockUpdate {
        final Block block;
        final BlockAction action;
        final BlockEventType eventType;
        final long scheduledTick;
        
        PendingBlockUpdate(Block block, BlockAction action, BlockEventType eventType, long tick) {
            this.block = block;
            this.action = action;
            this.eventType = eventType;
            this.scheduledTick = tick;
        }
    }
}