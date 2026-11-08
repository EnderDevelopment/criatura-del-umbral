package com.enderdevelopment.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements CommandExecutor {
    private HorrorManager horrorManager;

    @Override
    public void onEnable() {
        horrorManager = new HorrorManager(this);
        getServer().getPluginManager().registerEvents(horrorManager, this);
        getCommand("errorworld").setExecutor(this);
        horrorManager.start();
    }

    @Override
    public void onDisable() {
        if (horrorManager != null) {
            horrorManager.stop();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("errorworld.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(horrorManager.statusMessage());
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            horrorManager.resetWorlds();
            sender.sendMessage("ErrorWorld phases have been reset for loaded worlds.");
            return true;
        }
        sender.sendMessage("Usage: /errorworld <status|reset>");
        return true;
    }
}
