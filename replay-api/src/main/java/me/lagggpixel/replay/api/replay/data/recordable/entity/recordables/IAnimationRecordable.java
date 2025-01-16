package me.lagggpixel.replay.api.replay.data.recordable.entity.recordables;

import me.lagggpixel.replay.api.utils.entity.AnimationType;

import java.util.UUID;

public interface IAnimationRecordable {
    UUID getUUID();
    AnimationType getAnimationType();
}
