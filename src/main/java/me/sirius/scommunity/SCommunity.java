package me.sirius.scommunity;

import me.sirius.scommunity.commands.MissionCommand;
import me.sirius.scommunity.commands.MissionAdminCommand;
import me.sirius.scommunity.config.ConfigManager;
import me.sirius.scommunity.data.DataManager;
import me.sirius.scommunity.listeners.InventoryListener;
import me.sirius.scommunity.listeners.PlayerListener;
import me.sirius.scommunity.managers.MissionManager;
import me.sirius.scommunity.managers.RewardManager;
import me.sirius.scommunity.tasks.MissionResetTask;
import me.sirius.scommunity.tasks.AutoSaveTask;
import me.sirius.core.SiriusPlugin;

public final class SCommunity extends SiriusPlugin {

    private static SCommunity instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private MissionManager missionManager;
    private RewardManager rewardManager;

    @Override
    protected void onStart() {
        instance = this;
        
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        dataManager = new DataManager(this);
        dataManager.loadData();
        
        dataManager.createBackup();
        
        missionManager = new MissionManager(this);
        rewardManager = new RewardManager(this);
        
        registerCommands();
        registerListeners();
        startTasks();
        
        getLogger().info("SCommunity by Sirius enabled successfully!");
        getLogger().info("Backup system active - Data is protected!");
    }
    
    private void startTasks() {
        new MissionResetTask(this).runTaskTimer(this, 20L, 1200L);
        
        int autoSaveInterval = configManager.getConfig().getInt("auto-save-interval", 300) * 20;
        new AutoSaveTask(this).runTaskTimerAsynchronously(this, autoSaveInterval, autoSaveInterval);
    }

    @Override
    protected void onStop() {
        if (dataManager != null) {
            getLogger().info("Creating final backup before shutdown...");
            dataManager.createBackup();
            
            getLogger().info("Saving all data before shutdown...");
            dataManager.saveData();
            getLogger().info("Data saved successfully!");
        }
        getLogger().info("SCommunity by Sirius disabled successfully!");
    }

    private void registerCommands() {
        getCommand("mission").setExecutor(new MissionCommand(this));
        getCommand("missionadmin").setExecutor(new MissionAdminCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public static SCommunity getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public MissionManager getMissionManager() {
        return missionManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }
}