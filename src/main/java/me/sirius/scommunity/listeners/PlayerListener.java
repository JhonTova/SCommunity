package me.sirius.scommunity.listeners;

import me.sirius.scommunity.SCommunity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SCommunity plugin;

    public PlayerListener(SCommunity plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getDataManager().getPendingRewards().contains(player.getUniqueId())) {
                plugin.getRewardManager().deliverPendingReward(player);
            }
        }, 40L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataManager().saveData();
        });
    }
}