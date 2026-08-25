package me.sirius.scommunity.commands;

import me.sirius.core.text.Text;

import me.sirius.scommunity.SCommunity;
import me.sirius.scommunity.gui.MissionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MissionCommand implements CommandExecutor {

    private final SCommunity plugin;

    public MissionCommand(SCommunity plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Text.send(sender, plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("scommunity.mission")) {
            Text.send(player, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        new MissionGUI(plugin, player).open();
        return true;
    }
}