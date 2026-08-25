package me.sirius.scommunity.commands;

import me.sirius.core.text.Text;

import me.sirius.scommunity.SCommunity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MissionAdminCommand implements CommandExecutor {

    private final SCommunity plugin;

    public MissionAdminCommand(SCommunity plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("scommunity.admin")) {
            Text.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().reloadConfigs();
                plugin.getMissionManager().loadMissions();
                Text.send(sender, plugin.getConfigManager().getMessage("config-reloaded"));
                break;

            case "reset":
                if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                    plugin.getDataManager().resetDailyData();
                    plugin.getMissionManager().loadMissions();
                    Text.send(sender, plugin.getConfigManager().getMessage("missions-reset"));
                } else {
                    Text.send(sender, "§c§lᴀᴅᴠᴇʀᴛᴇɴᴄɪᴀ:");
                    Text.send(sender, "§7ᴇsᴛᴏ ʀᴇsᴇᴛᴇᴀʀá ʟᴀs ᴍɪsɪᴏɴᴇs ᴘᴇʀsᴏɴᴀʟᴇs.");
                    Text.send(sender, "§7ᴇʟ ᴘʀᴏɢʀᴇsᴏ ɢʟᴏʙᴀʟ ɴᴏ sᴇ ʀᴇsᴇᴛᴇᴀ.");
                    Text.send(sender, "§7ᴘᴀʀᴀ ᴄᴏɴғɪʀᴍᴀʀ ᴜsᴀ: §e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ ʀᴇsᴇᴛ ᴄᴏɴғɪʀᴍ");
                }
                break;

            case "resetglobal":
                if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                    plugin.getDataManager().resetGlobalMission();
                    plugin.getMissionManager().loadMissions();
                    Text.send(sender, "§a§lᴘʀᴏɢʀᴇsᴏ ɢʟᴏʙᴀʟ ʀᴇsᴇᴛᴇᴀᴅᴏ!");
                } else {
                    Text.send(sender, "§c§lᴀᴅᴠᴇʀᴛᴇɴᴄɪᴀ:");
                    Text.send(sender, "§7ᴇsᴛᴏ ʀᴇsᴇᴛᴇᴀʀá ᴇʟ ᴘʀᴏɢʀᴇsᴏ ɢʟᴏʙᴀʟ.");
                    Text.send(sender, "§7ᴘᴀʀᴀ ᴄᴏɴғɪʀᴍᴀʀ: §e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ ʀᴇsᴇᴛɢʟᴏʙᴀʟ ᴄᴏɴғɪʀᴍ");
                }
                break;

            case "save":
                plugin.getDataManager().saveData();
                Text.send(sender, plugin.getConfigManager().getMessage("data-saved"));
                break;

            case "backup":
                plugin.getDataManager().createBackup();
                Text.send(sender, "§a§lʙᴀᴄᴋᴜᴘ ᴄʀᴇᴀᴅᴏ ᴇxɪᴛᴏsᴀᴍᴇɴᴛᴇ!");
                break;

            case "setlevel":
                if (args.length < 2) {
                    Text.send(sender, "§cᴜsᴏ: /ᴍɪssɪᴏɴᴀᴅᴍɪɴ sᴇᴛʟᴇᴠᴇʟ <ɴɪᴠᴇʟ>");
                    return true;
                }
                try {
                    int level = Integer.parseInt(args[1]);
                    int maxLevel = plugin.getConfigManager().getConfig().getInt("community-levels.max-level", 10);
                    if (level < 1 || level > maxLevel) {
                        Text.send(sender, "§cᴇʟ ɴɪᴠᴇʟ ᴅᴇʙᴇ ᴇsᴛᴀʀ ᴇɴᴛʀᴇ 1 ʏ " + maxLevel);
                        return true;
                    }
                    plugin.getDataManager().setCommunityLevel(level);
                    plugin.getMissionManager().loadMissions();
                    plugin.getDataManager().saveData();
                    Text.send(sender, "§aɴɪᴠᴇʟ ᴄᴏᴍᴜɴɪᴛᴀʀɪᴏ ᴇsᴛᴀʙʟᴇᴄɪᴅᴏ ᴀ: §e" + level);
                } catch (NumberFormatException e) {
                    Text.send(sender, "§cᴇʟ ɴɪᴠᴇʟ ᴅᴇʙᴇ sᴇʀ ᴜɴ ɴúᴍᴇʀᴏ ᴠáʟɪᴅᴏ");
                }
                break;

            case "info":
                Text.send(sender, "§6§lɪɴғᴏʀᴍᴀᴄɪóɴ ᴅᴇʟ sɪsᴛᴇᴍᴀ");
                Text.send(sender, "§eɴɪᴠᴇʟ ᴄᴏᴍᴜɴɪᴛᴀʀɪᴏ: §b" + plugin.getDataManager().getCommunityLevel());
                Text.send(sender, "§eᴍɪsɪᴏɴ ɢʟᴏʙᴀʟ: §7" + (plugin.getDataManager().isGlobalCompleted() ? "§a✓ ᴄᴏᴍᴘʟᴇᴛᴀᴅᴀ" : "§c✗ ᴇɴ ᴘʀᴏɢʀᴇsᴏ"));
                Text.send(sender, "§eᴊᴜɢᴀᴅᴏʀᴇs ᴇʟᴇɢɪʙʟᴇs: §b" + plugin.getDataManager().getEligiblePlayers().size());
                Text.send(sender, "§eʀᴇᴄᴏᴍᴘᴇɴsᴀs ᴘᴇɴᴅɪᴇɴᴛᴇs: §b" + plugin.getDataManager().getPendingRewards().size());
                Text.send(sender, "§eᴊᴜɢᴀᴅᴏʀᴇs ᴄᴏɴ ᴘʀᴏɢʀᴇsᴏ: §b" + plugin.getDataManager().getAllPlayerData().size());
                Text.send(sender, "§eᴘʀᴏɢʀᴇsᴏ ɢʟᴏʙᴀʟ: §b" + plugin.getMissionManager().getTotalGlobalProgress() + " §7/ §b" + plugin.getMissionManager().getTotalGlobalRequired());
                Text.send(sender, "§6§l────────────────────────────");
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        Text.send(sender, "§6§lsᴄᴏᴍᴍᴜɴɪᴛʏ ᴀᴅᴍɪɴ ᴄᴏᴍᴍᴀɴᴅs");
        Text.send(sender, "§e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ ʀᴇʟᴏᴀᴅ §7- ʀᴇᴄᴀʀɢᴀʀ ᴄᴏɴғɪɢᴜʀᴀᴄɪóɴ");
        Text.send(sender, "§e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ ʀᴇsᴇᴛ ᴄᴏɴғɪʀᴍ §7- ʀᴇsᴇᴛᴇᴀʀ ᴍɪsɪᴏɴᴇs ᴅɪᴀʀɪᴀs");
        Text.send(sender, "§e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ ʀᴇsᴇᴛɢʟᴏʙᴀʟ ᴄᴏɴғɪʀᴍ §7- ʀᴇsᴇᴛᴇᴀʀ ᴘʀᴏɢʀᴇsᴏ ɢʟᴏʙᴀʟ");
        Text.send(sender, "§e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ sᴀᴠᴇ §7- ɢᴜᴀʀᴅᴀʀ ᴅᴀᴛᴏs ᴍᴀɴᴜᴀʟᴍᴇɴᴛᴇ");
        Text.send(sender, "§e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ ʙᴀᴄᴋᴜᴘ §7- ᴄʀᴇᴀʀ ʙᴀᴄᴋᴜᴘ ᴍᴀɴᴜᴀʟ");
        Text.send(sender, "§e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ sᴇᴛʟᴇᴠᴇʟ <#> §7- ᴄᴀᴍʙɪᴀʀ ɴɪᴠᴇʟ ᴄᴏᴍᴜɴɪᴛᴀʀɪᴏ");
        Text.send(sender, "§e/ᴍɪssɪᴏɴᴀᴅᴍɪɴ ɪɴғᴏ §7- ᴠᴇʀ ɪɴғᴏʀᴍᴀᴄɪóɴ ᴅᴇʟ sɪsᴛᴇᴍᴀ");
        Text.send(sender, "§6§l─────────────────────────────");
    }
}