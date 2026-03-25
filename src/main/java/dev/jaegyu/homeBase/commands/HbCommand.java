package dev.jaegyu.homeBase.commands;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.PDBackupManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class HbCommand implements BasicCommand {

    private final ConfigManager configManager;
    private final PDBackupManager backupManager;

    public HbCommand(ConfigManager configManager, PDBackupManager backupManager) {
        this.configManager = configManager;
        this.backupManager = backupManager;
    }

    @Override
    public String permission() {
        return "homebase.admin";
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        var sender = stack.getSender();

        if (args.length == 0) {
            sendHelp(stack);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "config" -> handleConfig(stack, args);
            case "backup" -> handleBackup(stack, args);
            default -> sendHelp(stack);
        }
    }

    private void handleConfig(CommandSourceStack stack, String[] args) {
        var sender = stack.getSender();
        if (args.length < 2) {
            sender.sendMessage("Usage: /hb config <set|list>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "set" -> handleSet(stack, args);
            case "list" -> handleList(stack);
            default -> sender.sendMessage("Unknown subcommand. Usage: /hb config <set|list>");
        }
    }

    private void handleSet(CommandSourceStack stack, String[] args) {
        var sender = stack.getSender();
        if (args.length != 4) {
            sender.sendMessage("Usage: /hb config set <key> <value>");
            return;
        }

        String key = args[2];
        String rawValue = args[3];

        if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
            sender.sendMessage("Value must be true or false.");
            return;
        }

        if (!configManager.contains(key)) {
            sender.sendMessage("Unknown config key: " + key);
            return;
        }

        configManager.set(key, Boolean.parseBoolean(rawValue));
        sender.sendMessage("Set " + key + " to " + rawValue);
    }

    private void handleList(CommandSourceStack stack) {
        var sender = stack.getSender();
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
            sender.sendMessage("   " + property + " = " + configManager.get(key));
        }
    }

    private void handleBackup(CommandSourceStack stack, String[] args) {
        var sender = stack.getSender();
        if (args.length < 3) {
            sender.sendMessage("Usage: /hb backup <list|create|restore> <player> [timestamp]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list" -> handleBackupList(stack, args[2]);
            case "create" -> handleBackupCreate(stack, args[2]);
            case "restore" -> {
                if (args.length < 4) {
                    sender.sendMessage("Usage: /hb backup restore <player> <timestamp>");
                    return;
                }
                handleBackupRestore(stack, args[2], args[3]);
            }
            default -> sender.sendMessage("Unknown subcommand. Usage: /hb backup <list|create|restore>");
        }
    }

    private void handleBackupList(CommandSourceStack stack, String playerName) {
        var sender = stack.getSender();
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        try {
            Path[] backups = backupManager.getBackupsForPlayer(target.getUniqueId());
            if (backups.length == 0) {
                sender.sendMessage("No backups found for " + playerName);
                return;
            }
            sender.sendMessage("--- Backups for " + playerName + " ---");
            for (Path backup : backups) {
                sender.sendMessage("  " + backup.getFileName());
            }
        } catch (IOException e) {
            sender.sendMessage("Failed to list backups: " + e.getMessage());
        }
    }

    private void handleBackupCreate(CommandSourceStack stack, String playerName) {
        var sender = stack.getSender();
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(playerName + " is not online.");
            return;
        }
        backupManager.backup(target, PDBackupManager.BackupReason.MANUAL);
        sender.sendMessage("Backup created for " + playerName);
    }

    private void handleBackupRestore(CommandSourceStack stack, String playerName, String timestamp) {
        var sender = stack.getSender();
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        try {
            Path[] backups = backupManager.getBackupsForPlayer(target.getUniqueId());
            Path match = Arrays.stream(backups)
                    .filter(p -> p.getFileName().toString().contains(timestamp))
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                sender.sendMessage("No backup found matching timestamp: " + timestamp);
                return;
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
    }

    private void sendHelp(CommandSourceStack stack) {
        var sender = stack.getSender();
        sender.sendMessage("--- HomeBase Help ---");
        sender.sendMessage("/hb config set <key> <value>");
        sender.sendMessage("/hb config list");
        sender.sendMessage("/hb backup <list|create|restore> <player> [timestamp]");
    }

    // -------------------------------------------------------------------------
    // Tab completion
    // -------------------------------------------------------------------------

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return switch (args.length) {
            case 1 -> filter(List.of("config", "backup"), args[0]);
            case 2 -> switch (args[0].toLowerCase()) {
                case "config" -> filter(List.of("set", "list"), args[1]);
                case "backup" -> filter(List.of("list", "create", "restore"), args[1]);
                default -> List.of();
            };
            case 3 -> switch (args[0].toLowerCase()) {
                case "config" -> args[1].equalsIgnoreCase("set")
                        ? filter(configManager.getAllKeys(), args[2])
                        : List.of();
                case "backup" -> switch (args[1].toLowerCase()) {
                    case "create" -> filter(onlinePlayerNames(), args[2]);
                    case "list", "restore" -> filter(offlinePlayerNames(), args[2]);
                    default -> List.of();
                };
                default -> List.of();
            };
            case 4 -> {
                if (args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("set")) {
                    yield filter(List.of("true", "false"), args[3]);
                }
                if (args[0].equalsIgnoreCase("backup") && args[1].equalsIgnoreCase("restore")) {
                    yield filter(backupTimestampsFor(args[2]), args[3]);
                }
                yield List.of();
            }
            default -> List.of();
        };
    }

    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }

    private List<String> offlinePlayerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> backupTimestampsFor(String playerName) {
        try {
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            Path[] backups = backupManager.getBackupsForPlayer(target.getUniqueId());
            return Arrays.stream(backups)
                    .map(p -> p.getFileName().toString())
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}