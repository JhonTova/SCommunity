package me.sirius.scommunity.tasks;

import me.sirius.core.text.Text;

import me.sirius.scommunity.SCommunity;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class MissionResetTask extends BukkitRunnable {

    private final SCommunity plugin;
    private boolean resetWarningGiven = false;

    public MissionResetTask(SCommunity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long lastReset = plugin.getDataManager().getLastResetTime();
        long currentTime = System.currentTimeMillis();
        
        LocalDateTime lastResetDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastReset),
            ZoneId.systemDefault()
        );
        
        LocalDateTime currentDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(currentTime),
            ZoneId.systemDefault()
        );
        
        String intervalType = plugin.getConfigManager().getConfig().getString("reset-time.interval-type", "days");
        boolean shouldReset = false;
        
        if (intervalType.equalsIgnoreCase("minutes")) {
            shouldReset = handleMinutesInterval(lastResetDate, currentDate);
        } else if (intervalType.equalsIgnoreCase("days")) {
            shouldReset = handleDaysInterval(lastResetDate, currentDate);
        } else {
            plugin.getLogger().warning("Invalid interval-type in config! Using 'days' as default.");
            shouldReset = handleDaysInterval(lastResetDate, currentDate);
        }
        
        if (shouldReset) {
            plugin.getDataManager().saveData();
            plugin.getDataManager().createBackup();
            plugin.getDataManager().resetDailyData();
            plugin.getMissionManager().loadMissions();
            
            resetWarningGiven = false;
            
            if (plugin.getConfigManager().getConfig().getBoolean("reset-broadcast", true)) {
                Text.broadcast(
                    plugin.getConfigManager().getMessage("missions-reset-broadcast")
                );
            }
        }
    }
    
    private boolean handleMinutesInterval(LocalDateTime lastResetDate, LocalDateTime currentDate) {
        int minutesInterval = Math.max(1, plugin.getConfigManager().getConfig().getInt("reset-time.minutes-interval", 1));
        long minutesBetween = ChronoUnit.MINUTES.between(lastResetDate, currentDate);
        
        if (minutesBetween >= minutesInterval) {
            return true;
        }
        
        return false;
    }
    
    private boolean handleDaysInterval(LocalDateTime lastResetDate, LocalDateTime currentDate) {
        int daysInterval = Math.max(1, plugin.getConfigManager().getConfig().getInt("reset-time.days-interval", 1));
        long daysBetween = ChronoUnit.DAYS.between(lastResetDate.toLocalDate(), currentDate.toLocalDate());
        
        if (daysBetween >= daysInterval) {
            int resetHour = plugin.getConfigManager().getConfig().getInt("reset-time.hour", 0);
            int resetMinute = plugin.getConfigManager().getConfig().getInt("reset-time.minute", 0);
            
            LocalDateTime nextReset = currentDate.withHour(resetHour).withMinute(resetMinute).withSecond(0);
            long minutesUntilReset = ChronoUnit.MINUTES.between(currentDate, nextReset);
            
            if (minutesUntilReset <= 5 && minutesUntilReset > 0 && !resetWarningGiven) {
                if (plugin.getConfigManager().getConfig().getBoolean("reset-warnings.enabled", true)) {
                    String warningMsg = plugin.getConfigManager().getMessage("missions-reset-warning")
                        .replace("{minutes}", String.valueOf(minutesUntilReset));
                    Text.broadcast(warningMsg);
                    resetWarningGiven = true;
                }
            }
            
            if (currentDate.isAfter(nextReset) || currentDate.isEqual(nextReset)) {
                return true;
            }
        }
        
        return false;
    }
}