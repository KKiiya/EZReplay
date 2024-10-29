package me.lagggpixel.replay.utils;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.server.RestoreAdapter;
import me.lagggpixel.replay.Replay;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class FileUtils {

    public static boolean isWorldCached(IArena a) {
        RestoreAdapter adapter = Replay.getInstance().getBedWarsAPI().getRestoreAdapter();
        File worldsPath;

        switch (adapter.getDisplayName()) {
            case "Internal Restore Adapter":
                worldsPath = Bukkit.getWorldContainer();
                File[] files = worldsPath.listFiles();
                return files != null && Arrays.stream(files).anyMatch(file -> file.getName().equals(a.getWorldName()));
            case "Slime World Manager by Grinderwolf":
            case "Advanced Slime World Manager by Paul19988":
            case "Advanced Slime Paper by InfernalSuite":
                worldsPath = new File(Bukkit.getWorldContainer().getPath() + "/slime_worlds");
                File[] slimeFiles = worldsPath.listFiles();
                return slimeFiles != null && Arrays.stream(slimeFiles)
                        .anyMatch(f -> f.getName().replace(".slime", "").equals(a.getWorldName()));
        }
        return false;
    }

    public static void saveArenaWorldToCache(IArena a) {
        String worldName = a.getWorldName();
        String displayName = a.getDisplayName();
        RestoreAdapter adapter = Replay.getInstance().getBedWarsAPI().getRestoreAdapter();

        switch (adapter.getDisplayName()) {
            case "Internal Restore Adapter":
                
                break;
            case "Slime World Manager by Grinderwolf":
            case "Advanced Slime World Manager by Paul19988":
            case "Advanced Slime Paper by InfernalSuite":
                adapter.cloneArena(worldName, displayName+"-Replay");
                break;
        }
    }

    private static void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException {
        if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
            throw new IOException("Failed to create directory: " + destinationDirectory.getPath());
        }

        for (File file : Objects.requireNonNull(sourceDirectory.listFiles())) {
            copyDirectoryCompatibilityMode(file, new File(destinationDirectory, file.getName()));
        }
    }

    private static void copyDirectoryCompatibilityMode(File source, File destination) throws IOException {
        if (source.isDirectory()) copyDirectory(source, destination);
        else copyFile(source, destination);

    }

    private static void copyFile(File sourceFile, File destinationFile) throws IOException {
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
