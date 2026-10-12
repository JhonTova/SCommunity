package me.sirius.scommunity.managers;

import me.sirius.core.text.Text;

import me.sirius.scommunity.SCommunity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class RewardManager {

    private final SCommunity plugin;

    public RewardManager(SCommunity plugin) {
        this.plugin = plugin;
    }

    public void givePersonalReward(Player player) {
        List<String> commands = plugin.getConfigManager().getConfig().getStringList("rewards.personal");
        
        for (String command : commands) {
            String finalCommand = command.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        }
        
        String message = plugin.getConfigManager().getMessage("mission-personal-completed");
        Text.send(player, message);
        
        plugin.getDataManager().requestSave();
    }

    public void distributeGlobalRewards() {
        int level = plugin.getDataManager().getCommunityLevel();
        String rewardsPath = "rewards.global.level-" + level;
        
        List<String> commands = plugin.getConfigManager().getConfig().getStringList(rewardsPath);
        
        if (commands.isEmpty()) {
            commands = plugin.getConfigManager().getConfig().getStringList("rewards.global.level-1");
        }
        
        for (UUID uuid : plugin.getDataManager().getEligiblePlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                for (String command : commands) {
                    String finalCommand = command.replace("{player}", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                }
                
                String message = plugin.getConfigManager().getMessage("mission-global-completed")
                    .replace("{level}", String.valueOf(level));
                Text.send(player, message);
            } else {
                plugin.getDataManager().addPendingReward(uuid);
            }
        }
        
        String broadcastMessage = plugin.getConfigManager().getMessage("mission-global-completed")
            .replace("{level}", String.valueOf(level));
        Text.broadcast(broadcastMessage);
        
        plugin.getDataManager().requestSave();
    }

    public void deliverPendingReward(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!plugin.getDataManager().getPendingRewards().contains(uuid)) {
            return;
        }
        
        int level = plugin.getDataManager().getCommunityLevel();
        String rewardsPath = "rewards.global.level-" + level;
        
        List<String> commands = plugin.getConfigManager().getConfig().getStringList(rewardsPath);
        
        if (commands.isEmpty()) {
            commands = plugin.getConfigManager().getConfig().getStringList("rewards.global.level-1");
        }
        
        for (String command : commands) {
            String finalCommand = command.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        }
        
        String message = plugin.getConfigManager().getMessage("pending-reward-delivered")
            .replace("{level}", String.valueOf(level));
        Text.send(player, message);
        
        plugin.getDataManager().removePendingReward(uuid);
        plugin.getDataManager().requestSave();
    }

    public void addEligiblePlayer(UUID uuid) {
        plugin.getDataManager().addEligiblePlayer(uuid);
        plugin.getDataManager().requestSave();
    }
}