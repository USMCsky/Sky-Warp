package particles.usmcsky;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class skyWarp extends JavaPlugin {
    private static final String PLAYERS_PATH = "players";

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use warp commands.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /warp <name>, /warp set <name>, /warp list");
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length != 2) {
                player.sendMessage("Usage: /warp set <name>");
                return true;
            }

            String warpName = normalizeWarpName(args[1]);
            if (warpName == null) {
                player.sendMessage("Warp name must be 1-32 characters: letters, numbers, _ or -.");
                return true;
            }

            getConfig().set(getWarpPath(player, warpName), player.getLocation());
            saveConfig();
            player.sendMessage("Warp '" + warpName + "' has been set.");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (args.length != 1) {
                player.sendMessage("Usage: /warp list");
                return true;
            }

            List<String> warps = getPlayerWarpNames(player);
            if (warps.isEmpty()) {
                player.sendMessage("You do not have any warps yet.");
                return true;
            }

            player.sendMessage("Your warps: " + String.join(", ", warps));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /warp <name>");
            return true;
        }

        String warpName = normalizeWarpName(args[0]);
        if (warpName == null) {
            player.sendMessage("Invalid warp name.");
            return true;
        }

        Location target = getConfig().getLocation(getWarpPath(player, warpName));
        if (target == null) {
            player.sendMessage("Warp '" + warpName + "' does not exist.");
            return true;
        }

        player.teleport(target);
        player.sendMessage("Teleported to '" + warpName + "'.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>();
            if ("set".startsWith(prefix)) {
                suggestions.add("set");
            }
            if ("list".startsWith(prefix)) {
                suggestions.add("list");
            }
            for (String warpName : getPlayerWarpNames(player)) {
                if (warpName.startsWith(prefix)) {
                    suggestions.add(warpName);
                }
            }
            return suggestions;
        }

        return Collections.emptyList();
    }

    private String normalizeWarpName(String rawWarpName) {
        if (rawWarpName == null || rawWarpName.length() > 32 || rawWarpName.isEmpty()) {
            return null;
        }
        if (!rawWarpName.matches("[A-Za-z0-9_-]+")) {
            return null;
        }
        return rawWarpName.toLowerCase(Locale.ROOT);
    }

    private String getWarpPath(Player player, String warpName) {
        return PLAYERS_PATH + "." + player.getUniqueId() + "." + warpName;
    }

    private List<String> getPlayerWarpNames(Player player) {
        String playerPath = PLAYERS_PATH + "." + player.getUniqueId();
        if (!getConfig().isConfigurationSection(playerPath)) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>(getConfig().getConfigurationSection(playerPath).getKeys(false));
        Collections.sort(names);
        return names;
    }
}
