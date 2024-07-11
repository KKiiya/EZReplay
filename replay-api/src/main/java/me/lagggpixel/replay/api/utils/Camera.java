package me.lagggpixel.replay.api.utils;

import lombok.Getter;

@Getter
public class Camera {
    private final float yaw;
    private final float pitch;

    public Camera(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public boolean equals(Object var1) {
        if (!(var1 instanceof Camera)) {
            return false;
        } else {
            Camera var2 = (Camera)var1;
            return this.yaw == var2.yaw && this.pitch == var2.pitch;
        }
    }
}
