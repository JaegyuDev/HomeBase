package dev.jaegyu.homeBase.commands;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.PDBackupManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HbTabCompleter implements TabCompleter {

    private final ConfigManager configManager;
    private final PDBackupManager backupManager;


    public HbTabCompleter(ConfigManager configManager, PDBackupManager backupManager) {
        this.configManager = configManager;
        this.backupManager = backupManager;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

    private List<String> offlinePlayerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}