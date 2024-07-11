package me.lagggpixel.replay.utils;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.server.RestoreAdapter;
import me.lagggpixel.replay.Replay;
import org.bukkit.Bukkit;

import java.io.File;

public class FileUtils {

    public void saveArenaWorldToCache(IArena a) {
        String worldName = a.getWorldName();
        String displayName = a.getDisplayName();
        RestoreAdapter adapter = Replay.getInstance().getBedWarsAPI().getRestoreAdapter();
        File worldsPath;

        switch (adapter.getDisplayName()) {
            case "Internal Restore Adapter":
                worldsPath = Bukkit.getWorldContainer();
                break;
            case "Slime World Manager by Grinderwolf":
            case "Advanced Slime World Manager by Paul19988":
            case "Advanced Slime Paper by InfernalSuite":
                worldsPath = new File(Bukkit.getWorldContainer().getPath() + "/slime_worlds");
                break;
        }
    }
}
