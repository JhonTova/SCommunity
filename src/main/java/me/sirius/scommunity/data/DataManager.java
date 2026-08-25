package me.sirius.scommunity.data;

import me.sirius.scommunity.SCommunity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final SCommunity plugin;
    private File dataFile;
    private FileConfiguration data;
    
    private final Map<UUID, PlayerMissionData> playerData = new ConcurrentHashMap<>();
    private int totalGlobalProgress;
    private final Set<UUID> eligiblePlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingRewards = ConcurrentHashMap.newKeySet();
    private long lastResetTime;
    private boolean globalCompleted;
    private int communityLevel;

    public DataManager(SCommunity plugin) {
        this.plugin = plugin;
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        
        lastResetTime = data.getLong("last-reset", System.currentTimeMillis());
        globalCompleted = data.getBoolean("global-completed", false);
        communityLevel = data.getInt("community-level", 1);
        totalGlobalProgress = data.getInt("total-global-progress", 0);
        
        eligiblePlayers.clear();
        if (data.contains("eligible-players")) {
            for (String uuidStr : data.getStringList("eligible-players")) {
                eligiblePlayers.add(UUID.fromString(uuidStr));
            }
        }
        
        pendingRewards.clear();
        if (data.contains("pending-rewards")) {
            for (String uuidStr : data.getStringList("pending-rewards")) {
                pendingRewards.add(UUID.fromString(uuidStr));
            }
        }
        
        if (data.contains("player-data")) {
            for (String uuidStr : data.getConfigurationSection("player-data").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Integer> progress = new HashMap<>();
                
                if (data.contains("player-data." + uuidStr + ".progress")) {
                    for (String material : data.getConfigurationSection("player-data." + uuidStr + ".progress").getKeys(false)) {
                        progress.put(material, data.getInt("player-data." + uuidStr + ".progress." + material));
                    }
                }
                
                boolean completed = data.getBoolean("player-data." + uuidStr + ".completed");
                playerData.put(uuid, new PlayerMissionData(progress, completed));
            }
        }
    }

    // synchronized: el guardado async en cada salida podía solaparse con otro y corromper
    // el archivo al escribir dos veces a la vez sobre el mismo YamlConfiguration.
    public synchronized void saveData() {
        try {
            data.set("last-reset", lastResetTime);
            data.set("global-completed", globalCompleted);
            data.set("community-level", communityLevel);
            data.set("total-global-progress", totalGlobalProgress);
            
            List<String> eligibleList = new ArrayList<>();
            for (UUID uuid : eligiblePlayers) {
                eligibleList.add(uuid.toString());
            }
            data.set("eligible-players", eligibleList);
            
            List<String> pendingList = new ArrayList<>();
            for (UUID uuid : pendingRewards) {
                pendingList.add(uuid.toString());
            }
            data.set("pending-rewards", pendingList);
            
            data.set("player-data", null);
            for (Map.Entry<UUID, PlayerMissionData> entry : playerData.entrySet()) {
                String path = "player-data." + entry.getKey().toString();
                for (Map.Entry<String, Integer> progress : entry.getValue().getProgress().entrySet()) {
                    data.set(path + ".progress." + progress.getKey(), progress.getValue());
                }
                data.set(path + ".completed", entry.getValue().isCompleted());
            }
            
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("CRITICAL: Could not save data.yml: " + e.getMessage());
            e.printStackTrace();
            createEmergencyBackup();
        }
    }
    
    public void createBackup() {
        try {
            File backupFolder = new File(plugin.getDataFolder(), "backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }
            
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new java.util.Date());
            File backupFile = new File(backupFolder, "data_backup_" + timestamp + ".yml");
            
            if (dataFile.exists()) {
                java.nio.file.Files.copy(dataFile.toPath(), backupFile.toPath());
                plugin.getLogger().info("Backup created: " + backupFile.getName());
                
                cleanOldBackups(backupFolder);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create backup: " + e.getMessage());
        }
    }
    
    private void createEmergencyBackup() {
        try {
            File backupFolder = new File(plugin.getDataFolder(), "backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }
            
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new java.util.Date());
            File emergencyFile = new File(backupFolder, "EMERGENCY_" + timestamp + ".yml");
            
            if (dataFile.exists()) {
                java.nio.file.Files.copy(dataFile.toPath(), emergencyFile.toPath());
                plugin.getLogger().warning("EMERGENCY backup created: " + emergencyFile.getName());
            }
        } catch (IOException ex) {
            plugin.getLogger().severe("CRITICAL: Could not create emergency backup!");
        }
    }
    
    private void cleanOldBackups(File backupFolder) {
        int maxBackups = plugin.getConfigManager().getConfig().getInt("backup-settings.max-backups", 10);
        
        File[] backups = backupFolder.listFiles((dir, name) -> name.startsWith("data_backup_") && name.endsWith(".yml"));
        
        if (backups != null && backups.length > maxBackups) {
            java.util.Arrays.sort(backups, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            
            for (int i = 0; i < backups.length - maxBackups; i++) {
                if (backups[i].delete()) {
                    plugin.getLogger().info("Old backup deleted: " + backups[i].getName());
                }
            }
        }
    }
    
    public void autoSave() {
        saveData();
    }

    public PlayerMissionData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerMissionData());
    }

    public int getTotalGlobalProgress() {
        return totalGlobalProgress;
    }

    public void addGlobalProgress(int amount) {
        totalGlobalProgress += amount;
    }

    public boolean isGlobalCompleted() {
        return globalCompleted;
    }

    public void setGlobalCompleted(boolean completed) {
        this.globalCompleted = completed;
    }

    public Set<UUID> getEligiblePlayers() {
        return eligiblePlayers;
    }

    public Set<UUID> getPendingRewards() {
        return pendingRewards;
    }

    public void addEligiblePlayer(UUID uuid) {
        eligiblePlayers.add(uuid);
    }

    public void addPendingReward(UUID uuid) {
        pendingRewards.add(uuid);
    }

    public void removePendingReward(UUID uuid) {
        pendingRewards.remove(uuid);
    }

    public long getLastResetTime() {
        return lastResetTime;
    }

    public void setLastResetTime(long time) {
        this.lastResetTime = time;
    }

    public int getCommunityLevel() {
        return communityLevel;
    }

    public void setCommunityLevel(int level) {
        this.communityLevel = level;
    }

    public void incrementCommunityLevel() {
        int maxLevel = plugin.getConfigManager().getConfig().getInt("community-levels.max-level", 10);
        if (communityLevel < maxLevel) {
            communityLevel++;
            saveData();
        }
    }

    public Map<UUID, PlayerMissionData> getAllPlayerData() {
        return new HashMap<>(playerData);
    }

    public void resetDailyData() {
        plugin.getLogger().info("Resetting personal missions...");
        createBackup();
        
        playerData.clear();
        eligiblePlayers.clear();
        lastResetTime = System.currentTimeMillis();
        
        saveData();
        plugin.getLogger().info("Personal reset complete | Global: " + totalGlobalProgress);
    }

    public void resetGlobalMission() {
        plugin.getLogger().info("Resetting global mission progress");
        totalGlobalProgress = 0;
        globalCompleted = false;
        eligiblePlayers.clear();
        saveData();
    }

    public static class PlayerMissionData {
        private final Map<String, Integer> progress;
        private boolean completed;

        public PlayerMissionData() {
            this.progress = new HashMap<>();
            this.completed = false;
        }

        public PlayerMissionData(Map<String, Integer> progress, boolean completed) {
            this.progress = progress;
            this.completed = completed;
        }

        public Map<String, Integer> getProgress() {
            return progress;
        }

        public void addProgress(String material, int amount) {
            progress.merge(material, amount, Integer::sum);
        }

        public int getProgress(String material) {
            return progress.getOrDefault(material, 0);
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}