package me.lagggpixel.replay.utils;

import me.lagggpixel.replay.Replay;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static boolean isWorldCached(World world) {
        File cacheFolder = new File("plugins/" + Replay.getInstance().getName() + "/Cache");
        File[] files = cacheFolder.listFiles();
        if (files == null) return false;
        return Arrays.stream(files).map(File::getName).anyMatch(file -> file.equals(world.getName() + ".zip"));
    }

    public static void saveWorldToCache(World world) {
        String worldName = world.getName();
        File cacheFolder = new File("plugins/" + Replay.getInstance().getName() + "/Cache");

        if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
            LogUtil.error("Couldn't send to cache world with name '" + world.getName() + "'");
            return;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        File zipFile = new File(cacheFolder, worldName + ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(worldFolder, zos, worldFolder.getPath().length() + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void zipDirectory(File folder, ZipOutputStream zos, int basePathLength) throws IOException {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().equals("uid.dat") || file.getName().equals("playerdata") || file.getName().equals("session.lock")) continue;

            if (file.isDirectory()) {
                zipDirectory(file, zos, basePathLength);
            } else {
                String zipEntryName = file.getPath().substring(basePathLength);
                zos.putNextEntry(new ZipEntry(zipEntryName));
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
        }
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory()) deleteDirectory(file);
                else file.delete();
            }
            directory.delete();
        }
    }


    public static void decompressWorldFromCache(World world, String newName) {
        File cacheFolder = new File("plugins/" + Replay.getInstance().getName() + "/Cache");
        File zipFile = new File(cacheFolder, world.getName() + ".zip");
        File worldFolder = new File(Bukkit.getWorldContainer(), newName); // Use newName for target directory

        if (!zipFile.exists()) {
            LogUtil.error("Zip file for world '" + world.getName() + "' not found in cache.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                File entryDestination = new File(worldFolder, entry.getName()); // Use entry name to preserve structure

                if (entry.isDirectory()) {
                    if (!entryDestination.exists() && !entryDestination.mkdirs()) {
                        throw new IOException("Failed to create directory: " + entryDestination.getPath());
                    }
                } else {
                    File parent = entryDestination.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory: " + parent.getPath());
                    }
                    try (FileOutputStream fos = new FileOutputStream(entryDestination)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
