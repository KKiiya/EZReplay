package me.lagggpixel.replay.listeners.world;

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

import java.util.ArrayList;
import java.util.List;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockEvent(BlockChangeEvent e) {
        Block block = e.getBlock();
        World world = block.getWorld();

        // Skip water and lava flow/physics
        if ((e.getEventType() == BlockEventType.FROM_TO || e.getEventType() == BlockEventType.PHYSICS)
                && (block.getType() == Material.WATER || block.getType() == Material.LAVA)) return;

        IRecording recording = ReplayManager.getInstance().getActiveRecording(world);
        if (recording == null) return;

        IFrame frame = recording.getLastFrame();
        BlockAction action = determineBlockAction(e, block, recording);
        if (action == null) return;

        // Schedule block capture AFTER the event completes so we get the NEW state
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            recording.addBlockUpdate(frame, block);

            // For interactions, only play sound on the first block of multi-block structures
            boolean playSound = true;
            if (e.getEventType() == BlockEventType.INTERACT) {
                // Check if this is a door's upper half
                if (isDoor(block.getType())) {
                    byte data = block.getData();
                    boolean isUpperHalf = (data & 0x8) != 0;
                    playSound = !isUpperHalf; // Only play sound on lower door half
                }
            }

            BlockCache cache = new BlockCache(block); // Captures NEW state
            Recordable recordable = Replay.getInstance().getVersionSupport()
                    .createBlockRecordable(recording, cache, action, playSound);
            frame.addRecordable(recordable);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (e.isCancelled()) return;
        if (shouldIgnorePhysics(e)) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.PHYSICS));
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent e) {
        callBlockChangeEvent(null, e.getBlock(), BlockEventType.REDSTONE);
    }

    @EventHandler
    public void onCanBuild(BlockCanBuildEvent e) {
        callBlockChangeEvent(null, e.getBlock(), BlockEventType.CAN_BUILD);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.SPREAD));
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FORM));
    }

    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.MULTI_PLACE));
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FROM_TO));
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getEntity(), e.getBlock(), BlockEventType.ENTITY_FORM));
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.DAMAGE));
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.GROW));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.BURN));
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.DISPENSE));
    }

    @EventHandler
    public void onBlockExp(BlockExpEvent e) {
        callBlockChangeEvent(null, e.getBlock(), BlockEventType.EXP);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.EXPLODE));
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.FADE));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.BREAK);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.PLACE);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.PISTON_EXTEND));
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(null, e.getBlock(), BlockEventType.PISTON_RETRACT));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.isCancelled()) return;
        e.setCancelled(callBlockChangeEvent(e.getPlayer(), e.getBlock(), BlockEventType.IGNITE));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        // Only handle right-click interactions with blocks
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        
        Block block = e.getClickedBlock();
        
        // Check if block is actually interactable (doors, levers, buttons, etc.)
        if (!Replay.getInstance().getVersionSupport().isInteractable(block)) return;
        
        // Don't process if event is cancelled (means interaction didn't happen)
        if (e.useInteractedBlock() == Event.Result.DENY) return;
        
        Player player = e.getPlayer();
        
        // Collect all blocks that will be affected by this interaction
        List<Block> affectedBlocks = getAffectedBlocks(block);
        
        // Call BlockChangeEvent for each affected block
        // This ensures they go through the same recording pipeline
        Bukkit.getScheduler().runTask(Replay.getInstance(), () -> {
            for (Block affectedBlock : affectedBlocks) {
                callBlockChangeEvent(player, affectedBlock, BlockEventType.INTERACT);
            }
        });
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
        } else if (e.getEventType() == BlockEventType.PISTON_EXTEND || e.getEventType() == BlockEventType.PISTON_RETRACT) {
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

    private boolean shouldIgnorePhysics(BlockPhysicsEvent e) {
        Material type = e.getChangedType();
        Material current = e.getBlock().getType();
        
        // Ignore if no actual change
        if (type == current) return true;
        
        // Ignore liquid flow
        if (type == Material.WATER || type == Material.LAVA) return true;
        
        // Add more filters as needed
        return false;
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
        
        // Handle double-height blocks (doors)
        if (isDoor(type)) {
            Block otherHalf = getOtherDoorHalf(block);
            if (otherHalf != null) {
                blocks.add(otherHalf);
            }
        }
        // Handle double chests
        else if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
            Block adjacent = getAdjacentChest(block);
            if (adjacent != null) {
                blocks.add(adjacent);
            }
        }
        
        return blocks;
    }

    private boolean isDoor(Material type) {
        if (type == null) return false;
        if (type.toString().contains("TRAP")) return false; // Exclude trapdoors
        return type.toString().contains("DOOR");
    }

    private Block getOtherDoorHalf(Block door) {
        byte data = door.getData();
        boolean isUpperHalf = (data & 0x8) != 0;
        
        if (isUpperHalf) {
            // Get lower half
            Block lower = door.getRelative(BlockFace.DOWN);
            if (isDoor(lower.getType())) {
                return lower;
            }
        } else {
            // Get upper half
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
}