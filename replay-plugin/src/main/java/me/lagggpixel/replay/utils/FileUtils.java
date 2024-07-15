package me.lagggpixel.replay.utils;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.server.RestoreAdapter;
import me.lagggpixel.replay.Replay;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.Objects;

public class FileUtils {

    public static boolean isWorldCached(IArena a) {
        RestoreAdapter adapter = Replay.getInstance().getBedWarsAPI().getRestoreAdapter();
        File worldsPath;

        switch (adapter.getDisplayName()) {
            case "Internal Restore Adapter":
                worldsPath = Bukkit.getWorldContainer();
                for (File file : Objects.requireNonNull(worldsPath.listFiles())) {
                    if (file.getName().equals(a.getDisplayName())) return true;
                }
                break;
            case "Slime World Manager by Grinderwolf":
            case "Advanced Slime World Manager by Paul19988":
            case "Advanced Slime Paper by InfernalSuite":
                worldsPath = new File(Bukkit.getWorldContainer().getPath() + "/slime_worlds");
                break;
        }
        return false;
    }

    public static void saveArenaWorldToCache(IArena a) {
        String worldName = a.getWorldName();
        String displayName = a.getDisplayName();
        RestoreAdapter adapter = Replay.getInstance().getBedWarsAPI().getRestoreAdapter();
        File worldsPath;

        switch (adapter.getDisplayName()) {
            case "Internal Restore Adapter":
                worldsPath = Bukkit.getWorldContainer();
                File worldPath = new File(Bukkit.getWorldContainer().getPath() + "/" + worldName);
                try {
                    copyDirectory(worldPath, new File(worldsPath.getPath() + "/" + displayName));
                } catch (Exception ignored) {

                }
                break;
            case "Slime World Manager by Grinderwolf":
            case "Advanced Slime World Manager by Paul19988":
            case "Advanced Slime Paper by InfernalSuite":
                worldsPath = new File(Bukkit.getWorldContainer().getPath() + "/slime_worlds");
                break;
        }
    }

    private static void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException {
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        for (String f : Objects.requireNonNull(sourceDirectory.list())) {
            copyDirectoryCompatibilityMode(new File(sourceDirectory, f), new File(destinationDirectory, f));
        }
    }

    public static void copyDirectoryCompatibilityMode(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    private static void copyFile(File sourceFile, File destinationFile)
            throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }
}
