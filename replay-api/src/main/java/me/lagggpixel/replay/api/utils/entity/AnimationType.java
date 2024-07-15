package me.lagggpixel.replay.api.utils.entity;

public enum AnimationType {
    SWING_MAIN_HAND(0),
    HURT(1),
    LEAVE_BED(2),
    SWING_OFF_HAND(3),
    CRITICAL_HIT(4),
    MAGIC_CRITICAL_HIT(5);

    private final int animationId;

    AnimationType(int animationId) {
        this.animationId = animationId;
    }

    public int getID() {
        return animationId;
    }

    public static AnimationType getById(int id) {
        return AnimationType.values()[id];
    }
}
