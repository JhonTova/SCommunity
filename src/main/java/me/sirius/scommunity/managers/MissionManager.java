package me.sirius.scommunity.managers;

import me.sirius.core.text.Text;

import me.sirius.scommunity.SCommunity;
import me.sirius.scommunity.data.DataManager.PlayerMissionData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MissionManager {

    private final SCommunity plugin;
    private final Map<String, MissionItem> personalRequirements = new LinkedHashMap<>();
    private int totalPersonalRequired;
    private long currentRotationSeed;

    public MissionManager(SCommunity plugin) {
        this.plugin = plugin;
        loadMissions();
    }

    public void loadMissions() {
        personalRequirements.clear();
        
        long resetTime = plugin.getDataManager().getLastResetTime();
        long periodsSinceEpoch = calculatePeriodsSinceEpoch(resetTime);
        currentRotationSeed = periodsSinceEpoch;
        
        Random random = new Random(currentRotationSeed);
        
        Map<String, CategoryConfig> categories = loadCategories();
        List<String> allItems = new ArrayList<>();
        Map<String, CategoryConfig> itemToCategory = new HashMap<>();
        
        for (Map.Entry<String, CategoryConfig> entry : categories.entrySet()) {
            for (String item : entry.getValue().items) {
                allItems.add(item);
                itemToCategory.put(item, entry.getValue());
            }
        }
        
        Collections.shuffle(allItems, random);
        
        int itemsPerDay = plugin.getConfigManager().getConfig().getInt("mission-rotation.items-per-day", 6);
        int[] slots = {19, 20, 21, 28, 29, 30};
        
        totalPersonalRequired = 0;
        Set<String> usedMaterials = new HashSet<>();
        int itemIndex = 0;
        int slotsUsed = 0;
        
        while (slotsUsed < itemsPerDay && itemIndex < allItems.size()) {
            String materialName = allItems.get(itemIndex);
            itemIndex++;
            
            if (usedMaterials.contains(materialName)) {
                continue;
            }
            
            usedMaterials.add(materialName);
            CategoryConfig category = itemToCategory.get(materialName);
            
            int amount = category.minAmount + random.nextInt(category.maxAmount - category.minAmount + 1);
            String displayName = category.displayColor + getMaterialDisplayName(materialName);
            int slot = slots[slotsUsed];
            
            personalRequirements.put(materialName, new MissionItem(materialName, amount, displayName, slot));
            totalPersonalRequired += amount;
            slotsUsed++;
        }
        
        plugin.getLogger().info("Missions loaded | Period: " + periodsSinceEpoch + " | Seed: " + currentRotationSeed);
        plugin.getLogger().info("Personal: " + totalPersonalRequired + " items | Global: " + getTotalGlobalRequired() + " items | Level: " + plugin.getDataManager().getCommunityLevel());
    }
    
    private long calculatePeriodsSinceEpoch(long resetTime) {
        String intervalType = plugin.getConfigManager().getConfig().getString("reset-time.interval-type", "days");
        
        if (intervalType.equalsIgnoreCase("minutes")) {
            int minutesInterval = plugin.getConfigManager().getConfig().getInt("reset-time.minutes-interval", 1);
            return resetTime / (minutesInterval * 60 * 1000);
        } else {
            return resetTime / (24 * 60 * 60 * 1000);
        }
    }

    private Map<String, CategoryConfig> loadCategories() {
        Map<String, CategoryConfig> categories = new HashMap<>();
        ConfigurationSection categoriesSection = plugin.getConfigManager().getConfig().getConfigurationSection("item-categories");
        
        if (categoriesSection != null) {
            for (String categoryName : categoriesSection.getKeys(false)) {
                int minAmount = categoriesSection.getInt(categoryName + ".min-amount");
                int maxAmount = categoriesSection.getInt(categoryName + ".max-amount");
                String displayColor = categoriesSection.getString(categoryName + ".display-color", "&7");
                List<String> items = categoriesSection.getStringList(categoryName + ".items");
                
                categories.put(categoryName, new CategoryConfig(minAmount, maxAmount, displayColor, items));
            }
        }
        
        return categories;
    }

    private String getMaterialDisplayName(String material) {
        return material.toLowerCase().replace("_", " ");
    }

    private int calculateGlobalRequirement() {
        if (!plugin.getConfigManager().getConfig().getBoolean("community-levels.enabled", true)) {
            int baseMultiplier = plugin.getConfigManager().getConfig().getInt("global-mission.base-multiplier", 30);
            return totalPersonalRequired * baseMultiplier;
        }
        
        int level = plugin.getDataManager().getCommunityLevel();
        int baseMultiplier = plugin.getConfigManager().getConfig().getInt("global-mission.base-multiplier", 30);
        int baseAmount = totalPersonalRequired * baseMultiplier;
        
        if (level == 1) {
            return baseAmount;
        }
        
        double increment = plugin.getConfigManager().getConfig().getDouble("community-levels.increment-per-level", 0.15);
        int levelBonus = (int) (baseAmount * increment * (level - 1));
        
        return baseAmount + levelBonus;
    }

    public Map<String, MissionItem> getPersonalRequirements() {
        return personalRequirements;
    }

    public int getTotalGlobalRequired() {
        return calculateGlobalRequirement();
    }

    public int getTotalGlobalProgress() {
        return plugin.getDataManager().getTotalGlobalProgress();
    }

    public boolean depositItems(Player player, ItemStack[] items) {
        Map<String, Integer> toDeposit = new HashMap<>();
        
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            String materialName = item.getType().name();
            if (!personalRequirements.containsKey(materialName)) {
                continue;
            }
            
            toDeposit.merge(materialName, item.getAmount(), Integer::sum);
        }
        
        if (toDeposit.isEmpty()) {
            return false;
        }
        
        PlayerMissionData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        boolean allowExtra = plugin.getConfigManager().getConfig().getBoolean("contribution-settings.allow-extra-contributions", true);
        boolean blockAfterCompletion = plugin.getConfigManager().getConfig().getBoolean("contribution-settings.block-after-completion", false);
        
        if (blockAfterCompletion && playerData.isCompleted()) {
            String intervalType = plugin.getConfigManager().getConfig().getString("reset-time.interval-type", "days");
            String timeText;
            
            if (intervalType.equalsIgnoreCase("minutes")) {
                int minutesInterval = plugin.getConfigManager().getConfig().getInt("reset-time.minutes-interval", 1);
                timeText = minutesInterval + " minuto(s)";
            } else {
                int daysInterval = plugin.getConfigManager().getConfig().getInt("reset-time.days-interval", 1);
                timeText = daysInterval + " día(s)";
            }
            
            String message = plugin.getConfigManager().getMessage("already-completed-daily")
                .replace("{time}", timeText);
            Text.send(player, message);
            return false;
        }
        
        boolean personalProgressMade = false;
        boolean communityProgressMade = false;
        
        for (Map.Entry<String, Integer> entry : toDeposit.entrySet()) {
            String material = entry.getKey();
            int amount = entry.getValue();
            
            int currentProgress = playerData.getProgress(material);
            int required = personalRequirements.get(material).getAmount();
            int remaining = required - currentProgress;
            
            if (remaining > 0) {
                int toAdd = Math.min(amount, remaining);
                playerData.addProgress(material, toAdd);
                plugin.getDataManager().addGlobalProgress(toAdd);
                personalProgressMade = true;
                communityProgressMade = true;
                
                int extraAmount = amount - toAdd;
                if (extraAmount > 0 && allowExtra) {
                    plugin.getDataManager().addGlobalProgress(extraAmount);
                    communityProgressMade = true;
                }
            } else if (allowExtra) {
                plugin.getDataManager().addGlobalProgress(amount);
                communityProgressMade = true;
            }
        }
        
        if (personalProgressMade) {
            checkPersonalCompletion(player);
        }
        
        if (communityProgressMade) {
            plugin.getDataManager().addEligiblePlayer(player.getUniqueId());
            checkGlobalCompletion();
        }
        
        plugin.getDataManager().requestSave();
        
        return personalProgressMade || communityProgressMade;
    }

    private void checkPersonalCompletion(Player player) {
        PlayerMissionData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        
        if (playerData.isCompleted()) return;
        
        boolean allCompleted = true;
        for (Map.Entry<String, MissionItem> entry : personalRequirements.entrySet()) {
            int progress = playerData.getProgress(entry.getKey());
            int required = entry.getValue().getAmount();
            
            if (progress < required) {
                allCompleted = false;
                break;
            }
        }
        
        if (allCompleted) {
            playerData.setCompleted(true);
            plugin.getRewardManager().givePersonalReward(player);
        }
    }

    private void checkGlobalCompletion() {
        if (plugin.getDataManager().isGlobalCompleted()) return;
        
        int totalProgress = plugin.getDataManager().getTotalGlobalProgress();
        int totalRequired = getTotalGlobalRequired();
        
        if (totalProgress >= totalRequired) {
            plugin.getDataManager().setGlobalCompleted(true);
            plugin.getRewardManager().distributeGlobalRewards();
            
            if (plugin.getConfigManager().getConfig().getBoolean("community-levels.enabled", true)) {
                int currentLevel = plugin.getDataManager().getCommunityLevel();
                int maxLevel = plugin.getConfigManager().getConfig().getInt("community-levels.max-level", 10);
                
                if (currentLevel < maxLevel) {
                    plugin.getDataManager().incrementCommunityLevel();
                    plugin.getDataManager().resetGlobalMission();
                    
                    String levelUpMsg = plugin.getConfigManager().getMessage("community-level-up")
                        .replace("{level}", String.valueOf(plugin.getDataManager().getCommunityLevel()));
                    Text.broadcast(levelUpMsg);
                    
                    plugin.getLogger().info("Level up! New requirement: " + getTotalGlobalRequired());
                } else {
                    String maxLevelMsg = plugin.getConfigManager().getMessage("max-level-reached");
                    Text.broadcast(maxLevelMsg);
                }
            }
        }
    }

    private static class CategoryConfig {
        int minAmount;
        int maxAmount;
        String displayColor;
        List<String> items;

        CategoryConfig(int minAmount, int maxAmount, String displayColor, List<String> items) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.displayColor = displayColor;
            this.items = items;
        }
    }

    public static class MissionItem {
        private final String material;
        private final int amount;
        private final String displayName;
        private final int slot;

        public MissionItem(String material, int amount, String displayName, int slot) {
            this.material = material;
            this.amount = amount;
            this.displayName = displayName;
            this.slot = slot;
        }

        public String getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }

        public String getDisplayName() {
            return displayName.replace("&", "§");
        }

        public int getSlot() {
            return slot;
        }
    }
}