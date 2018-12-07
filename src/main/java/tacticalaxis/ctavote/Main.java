package tacticalaxis.ctavote;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public final class Main extends JavaPlugin implements CommandExecutor {

    private static Main instance;
    private HashMap<Integer, Player> votes = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        createConfig();
        getCommand("ctavote").setExecutor(this);
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("ctavote")) {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                if(args.length > 0) {
                    if(args[0].equalsIgnoreCase("reload")) {
                        if(sender.hasPermission("ctavote.admin")) {
                            Main.getInstance().saveConfig();
                            Main.getInstance().reloadConfig();
                        } else {
                            vote(player);
                        }
                    } else if(args[0].equalsIgnoreCase("edit")) {
                        if(sender.hasPermission("ctavote.admin")) {
                            if(args.length > 2) {
                                if(args[1].equalsIgnoreCase("cmd")) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 2; i < args.length; i++){
                                        sb.append(args[i]).append(" ");
                                    }
                                    String allArgs = sb.toString().trim();
                                    Main.getInstance().getConfig().set("command-to-execute", allArgs);
                                    Main.getInstance().saveConfig();
                                    Main.getInstance().reloadConfig();
                                } else if (args[1].equalsIgnoreCase("percent")) {
                                    Main.getInstance().getConfig().set("percentage-required", args[2]);
                                    Main.getInstance().saveConfig();
                                    Main.getInstance().reloadConfig();
                                }
                            } else {
                                vote(player);
                            }
                        } else {
                            vote(player);
                        }
                    } else {
                        vote(player);
                    }
                } else {
                    vote(player);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can do this!");
            }
        }
        return true;
    }

    private void vote(Player player) {
        votes.put(0, player);
        float playerNum = votes.size();
        float totalNum = Bukkit.getOnlinePlayers().size();
        float percent = (playerNum * 100.0f) / totalNum;
        String configPercentVal = Main.getInstance().getConfig().get("percentage-required").toString();
        configPercentVal = configPercentVal.replaceAll("[^\\d.]", "");
        Main.getInstance().getConfig().set("percentage-required", configPercentVal);
        Main.getInstance().createConfig();
        Main.getInstance().reloadConfig();
        if(percent >= Integer.valueOf(configPercentVal)) {
            if(player.isOp()) {
               player.performCommand(Main.getInstance().getConfig().getString("command-to-execute"));
            } else {
                player.setOp(true);
                player.performCommand(Main.getInstance().getConfig().getString("command-to-execute"));
                player.setOp(false);
            }
        }
    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
