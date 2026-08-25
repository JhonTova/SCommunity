package me.sirius.scommunity.gui;

import me.sirius.scommunity.SCommunity;
import me.sirius.scommunity.data.DataManager.PlayerMissionData;
import me.sirius.scommunity.managers.MissionManager.MissionItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MissionGUI implements InventoryHolder {

    private final SCommunity plugin;
    private final Player player;
    private final Inventory inventory;
    private static final DecimalFormat df = new DecimalFormat("#,###");

    public MissionGUI(SCommunity plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "§6§l✦ §e§lᴍɪsɪᴏɴᴇs ᴄᴏᴍᴜɴɪᴛᴀʀɪᴀsS §6§l✦");
        setupGUI();
    }

    private void setupGUI() {
        fillBorders();
        addDivider();
        addPersonalMissions();
        addGlobalMission();
        addDepositButton();
        addInfoItems();
        addDecoration();
    }

    private void fillBorders() {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§0");
        
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }

    private void addDivider() {
        ItemStack divider = createItem(Material.YELLOW_STAINED_GLASS_PANE, "§e§l|");
        
        for (int i = 10; i < 44; i += 9) {
            inventory.setItem(i + 3, divider);
        }
    }

    private void addPersonalMissions() {
        PlayerMissionData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        Map<String, MissionItem> requirements = plugin.getMissionManager().getPersonalRequirements();
        
        ItemStack header = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) header.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§e§lᴍɪsɪᴏɴ ᴘᴇʀsᴏɴᴀʟ");
        List<String> lore = new ArrayList<>();
        lore.add("§8§m────────────────────");
        lore.add("§7ᴊᴜɢᴀᴅᴏʀ: §f" + player.getName());
        lore.add("");
        lore.add("§7ᴄᴏᴍᴘʟᴇᴛᴀ ᴛᴜ ᴍɪsɪᴏɴ ᴘᴇʀsᴏɴᴀʟ");
        lore.add("§7ᴇɴᴛʀᴇɢᴀɴᴅᴏ ᴛᴏᴅᴏs ʟᴏs ɪᴛᴇᴍs");
        lore.add("§7ᴘᴀʀᴀ ʀᴇᴄɪʙɪʀ ʀᴇᴄᴏᴍᴘᴇɴsᴀs");
        lore.add("§8§m────────────────────");
        lore.add("");
        
        if (playerData.isCompleted()) {
            lore.add("§a§lᴄᴏᴍᴘʟᴇᴛᴀᴅᴀ");
            lore.add("§8» §a¡ғᴇʟɪᴄɪᴅᴀᴅᴇs!");
        } else {
            lore.add("§c§lᴇɴ ᴘʀᴏɢʀᴇsᴏ");
            lore.add("§8» §esɪɢᴜᴇ ᴇɴᴛʀᴇɢᴀɴᴅᴏ ɪᴛᴇᴍs");
        }
        
        meta.setLore(lore);
        header.setItemMeta(meta);
        inventory.setItem(11, header);
        
        for (Map.Entry<String, MissionItem> entry : requirements.entrySet()) {
            String materialName = entry.getKey();
            MissionItem missionItem = entry.getValue();
            int required = missionItem.getAmount();
            int progress = playerData.getProgress(materialName);
            int slot = missionItem.getSlot();
            
            Material material = Material.getMaterial(materialName);
            if (material == null) material = Material.STONE;
            
            ItemStack item = new ItemStack(material);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName("§6§l" + missionItem.getDisplayName());
            
            List<String> itemLore = new ArrayList<>();
            itemLore.add("§8§m────────────────────");
            itemLore.add("§7ᴛᴜ ᴘʀᴏɢʀᴇsᴏ:");
            itemLore.add("§e  ➥ " + df.format(progress) + " §7/ §e" + df.format(required));
            itemLore.add("");
            
            double percentage = (double) progress / required * 100;
            itemLore.add(getProgressBar(percentage, "§a", "§7"));
            itemLore.add("");
            
            if (progress >= required) {
                itemLore.add("§a§lᴄᴏᴍᴘʟᴇᴛᴀᴅᴀ");
                itemLore.add("§8» §a¡ᴇxᴄᴇʟᴇɴᴛᴇ ᴛʀᴀʙᴀᴊᴏ!");
            } else {
                int remaining = required - progress;
                itemLore.add("§c§lғᴀʟᴛᴀɴ: §e" + df.format(remaining));
                itemLore.add("§8» §7¡sɪɢᴜᴇ ᴀᴅᴇʟᴀɴᴛᴇ!");
            }
            itemLore.add("§8§m────────────────────");
            
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
            
            inventory.setItem(slot, item);
        }
    }

    private void addGlobalMission() {
        int totalProgress = plugin.getDataManager().getTotalGlobalProgress();
        int totalRequired = plugin.getMissionManager().getTotalGlobalRequired();
        int level = plugin.getDataManager().getCommunityLevel();
        
        String displayName = plugin.getConfigManager().getConfig().getString("global-item.display-name", "&b&lCONTRIBUCIONES TOTALES");
        String materialName = plugin.getConfigManager().getConfig().getString("global-item.material", "NETHER_STAR");
        int slot = plugin.getConfigManager().getConfig().getInt("global-item.slot", 24);
        
        Material material = Material.getMaterial(materialName);
        if (material == null) material = Material.NETHER_STAR;
        
        ItemStack globalItem = new ItemStack(material);
        ItemMeta meta = globalItem.getItemMeta();
        meta.setDisplayName(displayName.replace("&", "§"));
        
        List<String> lore = new ArrayList<>();
        lore.add("§8§m────────────────────");
        lore.add("§7ɴɪᴠᴇʟ ᴄᴏᴍᴜɴɪᴛᴀʀɪᴏ: §b§l" + level);
        lore.add("");
        lore.add("§7ᴛᴏᴅᴏ ᴇʟ sᴇʀᴠɪᴅᴏʀ ᴛʀᴀʙᴀᴊᴀ");
        lore.add("§7ᴇɴ ᴇǫᴜɪᴘᴏ ᴘᴀʀᴀ ᴄᴏᴍᴘʟᴇᴛᴀʀ");
        lore.add("§7ᴇsᴛᴀ ᴍɪsɪᴏɴ ɢʟᴏʙᴀʟ");
        lore.add("§8§m────────────────────");
        lore.add("");
        lore.add("§7ᴘʀᴏɢʀᴇsᴏ ɢʟᴏʙᴀʟ:");
        lore.add("§b  ➥ " + df.format(totalProgress) + " §7/ §b" + df.format(totalRequired));
        lore.add("");
        
        double percentage = (double) totalProgress / totalRequired * 100;
        lore.add(getProgressBar(percentage, "§b", "§7"));
        lore.add("");
        
        if (plugin.getDataManager().isGlobalCompleted()) {
            lore.add("§a§lᴄᴏᴍᴘʟᴇᴛᴀᴅᴀ");
            lore.add("§8» §a¡ɴɪᴠᴇʟ ᴄᴏᴍᴘʟᴇᴛᴀᴅᴏ!");
        } else {
            int remaining = totalRequired - totalProgress;
            lore.add("§c§lғᴀʟᴛᴀɴ: §b" + df.format(remaining));
            lore.add("§8» §7¡ᴜɴɪᴅᴏs ʟᴏ ʟᴏɢʀᴀʀᴇᴍᴏs!");
        }
        lore.add("§8§m────────────────────");
        
        meta.setLore(lore);
        globalItem.setItemMeta(meta);
        
        inventory.setItem(slot, globalItem);
    }

    private void addDepositButton() {
        ItemStack deposit = createItem(Material.ENDER_CHEST, "§a§lᴅᴇᴘᴏsɪᴛᴀʀ ɪᴛᴇᴍs");
        ItemMeta meta = deposit.getItemMeta();
        meta.setCustomModelData(1);
        List<String> lore = new ArrayList<>();
        lore.add("§8§m────────────────────");
        lore.add("§7ʜᴀᴢ ᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴀʙʀɪʀ");
        lore.add("§7ᴇʟ ᴄᴏғʀᴇ ᴅᴇ ᴅᴇᴘᴏsɪᴛᴏ");
        lore.add("");
        lore.add("§7ᴄᴏʟᴏᴄᴀ ʟᴏs ɪᴛᴇᴍs ǫᴜᴇ");
        lore.add("§7ɴᴇᴄᴇsɪᴛᴀs ᴇɴᴛʀᴇɢᴀʀ ʏ");
        lore.add("§7ᴘʀᴇsɪᴏɴᴀ ᴇʟ ʙᴏᴛᴏɴ ᴠᴇʀᴅᴇ");
        lore.add("§8§m────────────────────");
        lore.add("");
        lore.add("§e§lᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴀʙʀɪʀ");
        meta.setLore(lore);
        deposit.setItemMeta(meta);
        inventory.setItem(49, deposit);
    }

    private void addInfoItems() {
        ItemStack info = createItem(Material.ENCHANTED_BOOK, "§e§lɪɴғᴏʀᴍᴀᴄɪᴏɴ");
        ItemMeta meta = info.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("§8§m────────────────────");
        lore.add("§6§lʀᴇᴄᴏᴍᴘᴇɴsᴀs:");
        lore.add("");
        lore.add("§e➥ ᴘᴇʀsᴏɴᴀʟ:");
        lore.add("  §7ᴄᴏᴍᴘʟᴇᴛᴀ ᴛᴜ ᴍɪsɪᴏɴ");
        lore.add("  §7ʏ ᴏʙᴛᴇɴ ʀᴇᴄᴏᴍᴘᴇɴsᴀs");
        lore.add("");
        lore.add("§b➥ ɢʟᴏʙᴀʟ:");
        lore.add("  §7sɪ ʟᴀ ᴍɪsɪᴏɴ ɢʟᴏʙᴀʟ");
        lore.add("  §7sᴇ ᴄᴏᴍᴘʟᴇᴛᴀ, ᴛᴏᴅᴏs");
        lore.add("  §7ʟᴏs ǫᴜᴇ ᴄᴏᴍᴘʟᴇᴛᴀʀᴏɴ sᴜ");
        lore.add("  §7ᴍɪsɪᴏɴ ᴘᴇʀsᴏɴᴀʟ ᴏʙᴛɪᴇɴᴇɴ");
        lore.add("  §7ʀᴇᴄᴏᴍᴘᴇɴsᴀs ᴇxᴛʀᴀs");
        lore.add("§8§m────────────────────");
        meta.setLore(lore);
        info.setItemMeta(meta);
        inventory.setItem(4, info);
    }

    private void addDecoration() {
        ItemStack deco1 = createItem(Material.LIME_STAINED_GLASS_PANE, "§a§l✦");
        ItemStack deco2 = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§b§l☆");
        
        inventory.setItem(1, deco1);
        inventory.setItem(7, deco2);
        inventory.setItem(10, deco1);
        inventory.setItem(16, deco2);
        inventory.setItem(46, deco1);
        inventory.setItem(52, deco2);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private String getProgressBar(double percentage, String completedColor, String remainingColor) {
        int bars = 20;
        int filled = (int) (bars * (percentage / 100));
        
        StringBuilder bar = new StringBuilder("  §7[");
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                bar.append(completedColor).append("■");
            } else {
                bar.append(remainingColor).append("■");
            }
        }
        bar.append("§7] §e").append(String.format("%.1f", percentage)).append("%");
        
        return bar.toString();
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}