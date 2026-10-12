package me.sirius.scommunity.listeners;

import me.sirius.core.text.Text;

import me.sirius.scommunity.SCommunity;
import me.sirius.scommunity.gui.DepositGUI;
import me.sirius.scommunity.gui.MissionGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import me.sirius.core.gui.Holders;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final SCommunity plugin;

    public InventoryListener(SCommunity plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = Holders.of(event.getInventory());

        if (holder instanceof MissionGUI) {
            handleMainGUIClick(event, player);
        } else if (holder instanceof DepositGUI) {
            handleDepositGUIClick(event, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        InventoryHolder holder = Holders.of(event.getInventory());
        if (holder instanceof MissionGUI) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
        } else if (holder instanceof DepositGUI) {
            for (int slot : event.getRawSlots()) {
                if (slot >= 45 && slot < 54) {
                    event.setCancelled(true);
                    event.setResult(org.bukkit.event.Event.Result.DENY);
                    return;
                }
            }
        }
    }

    private void handleMainGUIClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
        
        int slot = event.getRawSlot();
        
        if (slot < 0 || slot >= 54) return;
        
        if (slot == 49) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                new DepositGUI(plugin, player).open();
            }, 2L);
        }
    }

    private void handleDepositGUIClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        int clickedSlot = event.getSlot();
        
        if (slot >= 45 && slot < 54) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            
            if (slot >= 48 && slot <= 50) {
                handleDeposit(player, event.getInventory());
            } else if (slot >= 52 && slot <= 53) {
                returnItemsAndClose(player, event.getInventory());
            }
            return;
        }
        
        if (event.isShiftClick() && slot >= 54) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                for (int i = 0; i < 45; i++) {
                    ItemStack slotItem = event.getInventory().getItem(i);
                    if (slotItem == null || slotItem.getType() == Material.AIR) {
                        event.getInventory().setItem(i, clicked.clone());
                        event.getWhoClicked().getInventory().setItem(clickedSlot, null);
                        break;
                    } else if (slotItem.isSimilar(clicked) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                        int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                        int toAdd = Math.min(space, clicked.getAmount());
                        slotItem.setAmount(slotItem.getAmount() + toAdd);
                        clicked.setAmount(clicked.getAmount() - toAdd);
                        if (clicked.getAmount() <= 0) {
                            event.getWhoClicked().getInventory().setItem(clickedSlot, null);
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        
        if (Holders.of(event.getInventory()) instanceof DepositGUI) {
            returnItems(player, event.getInventory());
        }
    }

    private void handleDeposit(Player player, Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        ItemStack[] validItems = new ItemStack[45];
        
        int count = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                validItems[count++] = item.clone();
                inventory.setItem(i, null);
            }
        }
        
        if (count == 0) {
            Text.send(player, plugin.getConfigManager().getMessage("no-items-to-deposit"));
            player.closeInventory();
            return;
        }
        
        boolean success = plugin.getMissionManager().depositItems(player, validItems);
        
        if (success) {
            Text.send(player, plugin.getConfigManager().getMessage("items-deposited"));
            
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                new MissionGUI(plugin, player).open();
            }, 5L);
        } else {
            Text.send(player, plugin.getConfigManager().getMessage("invalid-items"));
            
            for (ItemStack item : validItems) {
                if (item != null && item.getType() != Material.AIR) {
                    give(player, item);
                }
            }
            
            player.closeInventory();
        }
    }

    private void returnItemsAndClose(Player player, Inventory inventory) {
        returnItems(player, inventory);
        player.closeInventory();
    }

    private void returnItems(Player player, Inventory inventory) {
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(i, null);
                give(player, item);
            }
        }
    }

    // Con el inventario lleno, addItem devuelve lo que no cabe y antes se perdia.
    private void give(Player player, ItemStack item) {
        for (ItemStack leftover : player.getInventory().addItem(item).values()) {
            player.getWorld().dropItem(player.getLocation(), leftover);
        }
    }
}