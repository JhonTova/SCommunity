package me.sirius.scommunity.gui;

import me.sirius.scommunity.SCommunity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DepositGUI implements InventoryHolder {

    private final SCommunity plugin;
    private final Player player;
    private final Inventory inventory;

    public DepositGUI(SCommunity plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "§a§l⚡ DEPOSITAR ITEMS");
        setupGUI();
    }

    private void setupGUI() {
        fillBorders();
        addDepositButton();
        addCancelButton();
        addInfoItem();
    }

    private void fillBorders() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "§7");
        
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }
    }

    private void addDepositButton() {
        ItemStack confirm = createItem(Material.LIME_STAINED_GLASS_PANE, "§a§l✓ CONFIRMAR DEPÓSITO");
        ItemMeta meta = confirm.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("§8§m────────────────────");
        lore.add("§7haz click aqui para");
        lore.add("§7ᴅᴇᴘᴏsɪᴛᴀʀ ᴛᴏᴅᴏs ʟᴏs ɪᴛᴇᴍs");
        lore.add("§7Qᴜᴇ ᴄᴏʟᴏᴄᴀsᴛᴇ ᴀʀʀɪʙᴀ");
        lore.add("§8§m────────────────────");
        lore.add("");
        lore.add("§a§l⚡ ᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴅᴇᴘᴏsɪᴛᴀʀ");
        meta.setLore(lore);
        confirm.setItemMeta(meta);
        
        inventory.setItem(48, confirm);
        inventory.setItem(49, confirm);
        inventory.setItem(50, confirm);
    }

    private void addCancelButton() {
        ItemStack cancel = createItem(Material.RED_STAINED_GLASS_PANE, "§c§l✖ ᴄᴀɴᴄᴇʟᴀʀ");
        ItemMeta meta = cancel.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("§8§m────────────────────");
        lore.add("§7ᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴄᴇʀʀᴀʀ");
        lore.add("§7los items volveran a ti");
        lore.add("§8§m────────────────────");
        lore.add("");
        lore.add("§c§l✖ ᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴄᴀɴᴄᴇʟᴀʀ");
        meta.setLore(lore);
        cancel.setItemMeta(meta);
        
        inventory.setItem(52, cancel);
        inventory.setItem(53, cancel);
    }

    private void addInfoItem() {
        ItemStack info = createItem(Material.PAPER, "§e§l📋 INSTRUCCIONES");
        ItemMeta meta = info.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("§8§m────────────────────");
        lore.add("§71. §fᴄᴏʟᴏᴄᴀ ʟᴏs ɪᴛᴇᴍs ᴀʀʀɪʙᴀ");
        lore.add("§72. §fʜᴀᴢ ᴄʟɪᴄᴋ ᴇɴ §a✓ ᴄᴏɴғɪʀᴍᴀʀ");
        lore.add("§73. §f¡ʟᴏs ɪᴛᴇᴍs sᴇʀᴀɴ ᴅᴇᴘᴏsɪᴛᴀᴅᴏs!");
        lore.add("");
        lore.add("§c⚠ sᴏʟᴏ sᴇ ᴀᴄᴇᴘᴛᴀɴ ɪᴛᴇᴍs");
        lore.add("§c  ᴠᴀʟɪᴅᴏs ᴅᴇ ʟᴀ ᴍɪsɪᴏɴ");
        lore.add("§8§m────────────────────");
        meta.setLore(lore);
        info.setItemMeta(meta);
        inventory.setItem(46, info);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}