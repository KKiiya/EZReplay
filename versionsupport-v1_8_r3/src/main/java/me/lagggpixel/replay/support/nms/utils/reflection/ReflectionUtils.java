package me.lagggpixel.replay.support.nms.utils.reflection;

import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static void setMultiBlockChangeInfo(PacketPlayOutMultiBlockChange packet, PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] newMultiBlockChangeInfo) {
        try {
            Field field = PacketPlayOutMultiBlockChange.class.getDeclaredField("b");
            field.setAccessible(true);
            field.set(packet, newMultiBlockChangeInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setChunkCordIntPairs(PacketPlayOutMultiBlockChange packet, ChunkCoordIntPair newChunkCoordIntPairs) {
        try {
            Field field = PacketPlayOutMultiBlockChange.class.getDeclaredField("a");
            field.setAccessible(true);
            field.set(packet, newChunkCoordIntPairs);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] getMultiBlockChangeInfo(PacketPlayOutMultiBlockChange packet) {
        try {
            Field field = PacketPlayOutMultiBlockChange.class.getDeclaredField("b");
            field.setAccessible(true);
            return (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[]) field.get(packet);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
