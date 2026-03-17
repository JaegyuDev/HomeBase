package dev.jaegyu.homeBase.commands;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.PDBackupManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class HbCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final PDBackupManager backupManager;

    public HbCommand(ConfigManager configManager, PDBackupManager backupManager) {
        this.configManager = configManager;
        this.backupManager = backupManager;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "config" -> handleConfig(sender, args);
            case "backup" -> handleBackup(sender, args);
            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    private boolean handleConfig(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /hb config <set|list>");
            return true;
        }

        return switch (args[1].toLowerCase()) {
            case "set" -> handleSet(sender, args);
            case "list" -> handleList(sender);
            default -> {
                sender.sendMessage("Unknown subcommand. Usage: /hb config <set|list>");
                yield true;
            }
        };
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage("Usage: /hb config set <key> <value>");
            return true;
        }

        String key = args[2];
        String rawValue = args[3];

        if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
            sender.sendMessage("Value must be true or false.");
            return true;
        }

        if (!configManager.contains(key)) {
            sender.sendMessage("Unknown config key: " + key);
            return true;
        }

        boolean value = Boolean.parseBoolean(rawValue);
        configManager.set(key, value);
        sender.sendMessage("Set " + key + " to " + value);
        return true;
    }

    private boolean handleList(CommandSender sender) {
        sender.sendMessage("--- HomeBase Config ---");

        String currentSection = null;
        for (String key : configManager.getAllKeys()) {
            String[] parts = key.split("\\.");
            String section = parts.length > 1 ? parts[parts.length - 2] : "";

            if (!section.equals(currentSection)) {
                currentSection = section;
                sender.sendMessage(" [" + section + "]");
            }

            String property = parts[parts.length - 1];
            Object value = configManager.get(key);
            sender.sendMessage("   " + property + " = " + value);
        }

        return true;
    }


    private boolean handleBackup(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /hb backup <list|create|restore> <player> [timestamp]");
            return true;
        }

        return switch (args[1].toLowerCase()) {
            case "list" -> handleBackupList(sender, args[2]);
            case "create" -> handleBackupCreate(sender, args[2]);
            case "restore" -> {
                if (args.length < 4) {
                    sender.sendMessage("Usage: /hb backup restore <player> <timestamp>");
                    yield true;
                }
                yield handleBackupRestore(sender, args[2], args[3]);
            }
            default -> {
                sender.sendMessage("Unknown subcommand. Usage: /hb backup <list|create|restore>");
                yield true;
            }
        };
    }

    private boolean handleBackupList(CommandSender sender, String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        try {
            Path[] backups = backupManager.getBackupsForPlayer(target.getUniqueId());
            if (backups.length == 0) {
                sender.sendMessage("No backups found for " + playerName);
                return true;
            }

            sender.sendMessage("--- Backups for " + playerName + " ---");
            for (Path backup : backups) {
                sender.sendMessage("  " + backup.getFileName());
            }
        } catch (IOException e) {
            sender.sendMessage("Failed to list backups: " + e.getMessage());
        }

        return true;
    }

    private boolean handleBackupCreate(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(playerName + " is not online.");
            return true;
        }

        backupManager.backup(target, PDBackupManager.BackupReason.MANUAL);
        sender.sendMessage("Backup created for " + playerName);
        return true;
    }

    private boolean handleBackupRestore(CommandSender sender, String playerName, String timestamp) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        try {
            Path[] backups = backupManager.getBackupsForPlayer(target.getUniqueId());
            Path match = Arrays.stream(backups)
                    .filter(p -> p.getFileName().toString().contains(timestamp))
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                sender.sendMessage("No backup found matching timestamp: " + timestamp);
                return true;
            }

            Player online = Bukkit.getPlayer(target.getUniqueId());
            if (online != null) {
                backupManager.restore(online, match);
            } else {
                backupManager.restore(target.getUniqueId(), match);
            }

            sender.sendMessage("Restored " + playerName + " from " + match.getFileName());
        } catch (IOException e) {
            sender.sendMessage("Failed to restore: " + e.getMessage());
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("--- HomeBase Help ---");
        sender.sendMessage("/hb config set <key> <value>");
        sender.sendMessage("/hb config list");
    }
}
