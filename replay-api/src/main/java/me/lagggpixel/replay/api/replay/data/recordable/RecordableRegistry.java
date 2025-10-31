package me.lagggpixel.replay.api.replay.data.recordable;

import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.utils.NMSVersion;

import java.io.DataInputStream;

public class RecordableRegistry {

    private static final String BASE_PACKAGE = "me.lagggpixel.replay.support.nms." + NMSVersion.getVersion() + ".recordable.";

    private RecordableRegistry() {
    }

    // ENTITY
    public static final short ANIMATION = 0;
    public static final short ENTITY_DEATH = 1;
    public static final short ENTITY_RECORDABLE = 2;
    public static final short ENTITY_SPAWN = 3;
    public static final short ENTITY_STATUS = 4;
    public static final short EQUIPMENT = 5;
    public static final short ITEM_DROP = 6;
    public static final short ITEM_MERGE = 7;
    public static final short ITEM_PICK = 8;

    // PLAYER STATUS
    public static final short INVISIBLE = 9;
    public static final short SNEAKING = 10;
    public static final short SPRINTING = 11;
    public static final short SWORD_BLOCK = 12;
    public static final short RESPAWN = 13;
    public static final short BURNING = 14;
    public static final short CHAT = 15;

    // WORLD
    public static final short BLOCK_DIG = 16;
    public static final short BLOCK_INTERACT = 17;
    public static final short BLOCK_UPDATE = 18;
    public static final short EXPLOSION = 19;
    public static final short PROJECTILE_LAUNCH = 20;
    public static final short VEHICLE_RIDE = 21;


    public static Recordable create(short typeId, DataInputStream in, EntityIndex index) {
        try {
            String className = getClassName(typeId);
            if (className == null)
                throw new IllegalArgumentException("Unknown Recordable type ID: " + typeId);

            Class<?> clazz = Class.forName(className);
            return (Recordable) clazz
                    .getConstructor(DataInputStream.class, EntityIndex.class)
                    .newInstance(in, index);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create recordable for ID " + typeId, e);
        }
    }

    private static String getClassName(short typeId) {
        switch (typeId) {
            // ENTITY
            case ANIMATION:
                return BASE_PACKAGE + "entity.entity.Animation";
            case ENTITY_DEATH:
                return BASE_PACKAGE + "entity.entity.EntityDeath";
            case ENTITY_RECORDABLE:
                return BASE_PACKAGE + "entity.entity.EntityRecordable";
            case ENTITY_SPAWN:
                return BASE_PACKAGE + "entity.entity.EntitySpawn";
            case ENTITY_STATUS:
                return BASE_PACKAGE + "entity.entity.EntityStatus";
            case EQUIPMENT:
                return BASE_PACKAGE + "entity.entity.Equipment";

            // ITEM
            case ITEM_DROP:
                return BASE_PACKAGE + "entity.item.ItemDrop";
            case ITEM_MERGE:
                return BASE_PACKAGE + "entity.item.ItemMerge";
            case ITEM_PICK:
                return BASE_PACKAGE + "entity.item.ItemPick";

            // PLAYER STATUS
            case INVISIBLE:
                return BASE_PACKAGE + "entity.player.status.Invisible";
            case SNEAKING:
                return BASE_PACKAGE + "entity.player.status.Sneaking";
            case SPRINTING:
                return BASE_PACKAGE + "entity.player.status.Sprinting";
            case SWORD_BLOCK:
                return BASE_PACKAGE + "entity.player.status.SwordBlock";
            case RESPAWN:
                return BASE_PACKAGE + "entity.player.status.Respawn";
            case BURNING:
                return BASE_PACKAGE + "entity.status.Burning";

            // PLAYER
            case CHAT:
                return BASE_PACKAGE + "player.ChatRecordable";

            // WORLD / BLOCK
            case BLOCK_DIG:
                return BASE_PACKAGE + "world.block.BlockDigRecordable";
            case BLOCK_INTERACT:
                return BASE_PACKAGE + "world.block.BlockInteractRecordable";
            case BLOCK_UPDATE:
                return BASE_PACKAGE + "world.block.BlockUpdateRecordable";
            case EXPLOSION:
                return BASE_PACKAGE + "world.ExplosionRecordable";

            default:
                return null;
        }
    }
}
