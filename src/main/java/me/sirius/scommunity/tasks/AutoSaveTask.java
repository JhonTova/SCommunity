package me.sirius.scommunity.tasks;

import me.sirius.scommunity.SCommunity;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveTask extends BukkitRunnable {

    private final SCommunity plugin;

    public AutoSaveTask(SCommunity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getDataManager().autoSave();
    }
}