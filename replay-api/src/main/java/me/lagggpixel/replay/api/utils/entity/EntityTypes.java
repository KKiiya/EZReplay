package me.lagggpixel.replay.api.utils.entity;

import org.bukkit.entity.EntityType;

public class EntityTypes {
    public static final int BOAT = 1;
    public static final int ITEM_STACK = 2;
    public static final int AREA_EFFECT_CLOUD = 3;
    public static final int MINECART = 10;
    public static final int ACTIVATED_TNT = 50;
    public static final int ENDER_CRYSTAL = 51;
    public static final int TIPPED_ARROW_PROJECTILE = 60;
    public static final int SNOWBALL_PROJECTILE = 61;
    public static final int EGG_PROJECTILE = 62;
    public static final int GHAST_FIREBALL = 63;
    public static final int BLAZE_FIREBALL = 64;
    public static final int THROWN_ENDERPEARL = 65;
    public static final int WITHER_SKULL_PROJECTILE = 66;
    public static final int SHULKER_BULLET = 67;
    public static final int FALLING_BLOCK = 70;
    public static final int ITEM_FRAME = 71;
    public static final int EYE_OF_ENDER = 72;
    public static final int THROWN_POTION = 73;
    public static final int THROWN_EXP_BOTTLE = 75;
    public static final int FIREWORK_ROCKET = 76;
    public static final int LEASH_KNOT = 77;
    public static final int ARMORSTAND = 78;
    public static final int FISHING_FLOAT = 90;
    public static final int SPECTRAL_ARROW = 91;
    public static final int DRAGON_FIREBALL = 93;

    public static int getId(EntityType entityType) {
        switch (entityType) {
            case BOAT:
                return BOAT;
            case DROPPED_ITEM:
                return ITEM_STACK;
            case AREA_EFFECT_CLOUD:
                return AREA_EFFECT_CLOUD;
            case MINECART:
                return MINECART;
            case PRIMED_TNT:
                return ACTIVATED_TNT;
            case ENDER_CRYSTAL:
                return ENDER_CRYSTAL;
            case ARROW:
                return TIPPED_ARROW_PROJECTILE;
            case SNOWBALL:
                return SNOWBALL_PROJECTILE;
            case EGG:
                return EGG_PROJECTILE;
            case FIREBALL:
                return GHAST_FIREBALL;
            case SMALL_FIREBALL:
                return BLAZE_FIREBALL;
            case ENDER_PEARL:
                return THROWN_ENDERPEARL;
            case WITHER_SKULL:
                return WITHER_SKULL_PROJECTILE;
            case SHULKER_BULLET:
                return SHULKER_BULLET;
            case FALLING_BLOCK:
                return FALLING_BLOCK;
            case ITEM_FRAME:
                return ITEM_FRAME;
            case ENDER_SIGNAL:
                return EYE_OF_ENDER;
            case SPLASH_POTION:
                return THROWN_POTION;
            case THROWN_EXP_BOTTLE:
                return THROWN_EXP_BOTTLE;
            case FIREWORK:
                return FIREWORK_ROCKET;
            case LEASH_HITCH:
                return LEASH_KNOT;
            case ARMOR_STAND:
                return ARMORSTAND;
            case FISHING_HOOK:
                return FISHING_FLOAT;
            case SPECTRAL_ARROW:
                return SPECTRAL_ARROW;
            case DRAGON_FIREBALL:
                return DRAGON_FIREBALL;
        }
        return 0;
    }
}
