package kheeto.hackcontrol.commands;

import kheeto.hackcontrol.util.CommandBase;
import kheeto.hackcontrol.HackControl;
import kheeto.hackcontrol.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.UUID;

public class Control implements CommandExecutor, TabCompleter {
    private static Control instance;
    private HackControl plugin;
    private Dictionary<UUID, UUID> controlList; // PlayerUUID, StafferUUID

    private Location playerPos = null;
    private Location stafferPos = null;
    private Location endPos = null;

    public Control(HackControl plugin) {
        instance = this;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();

        if (!sender.hasPermission("hackcontrol.control")) {
            Message.send(sender, config.getString("errors.noPermission"));
            return true;
        }

        if (args.length == 0) {
            for (String s : config.getStringList("help.control")) {
                Message.send(sender, s);
            }
            return true;
        }

        // Starts a new hack control
        if (args[0] == "start") {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) Message.send(sender, config.getString("error.noPlayer"));

            if (controlList.get(target) != null) {
                Message.send(sender, config.getString("errors.alreadyControlled"));
                return true;
            }

            // Executed from console
            if (!(sender instanceof Player)) {
                Message.send(sender, config.getString("errors.notPlayer"));
                return true;
            }
            // Executed by a player
            if (controlList.get(target.getUniqueId()) == Bukkit.getPlayer(sender.getName()).getUniqueId()) {
                if (sender.hasPermission("hackcontrol.control.start")) {
                    controlList.put(target.getUniqueId(), ((Player) sender).getUniqueId());
                    Message.send(sender, config.getString("control.stafferControlMessage"));
                    Message.send(target, config.getString("control.playerControlMessage"));
                    StartControl(target, (Player)sender);
                    return true;
                }
                else Message.send(sender, config.getString("errors.noPermission"));
            }
        }

        // Stops an hack control that is currently occurring
        else if (args[0] == "cancel") {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) Message.send(sender, config.getString("error.noPlayer"));

            // Executed from console
            if (!(sender instanceof Player)) {
                controlList.remove(target.getUniqueId());
                return true;
            }
            // Executed by the same staffer who is controlling the player
            if (controlList.get(target.getUniqueId()) == Bukkit.getPlayer(sender.getName()).getUniqueId()) {
                if (!sender.hasPermission("hackcontrol.control.cancel")) {
                    Message.send(sender, config.getString("errors.noPermission"));
                    return true;
                }
            }
            // Executed by another staffer
            else {
                if (!sender.hasPermission("hackcontrol.control.cancel.others")) {
                    Message.send(sender, config.getString("errors.noPermission"));
                    return true;
                }
            }

            // Removes the target from the list of players in hack control
            controlList.remove(target.getUniqueId());
            Message.send(sender, config.getString("control.stafferControlMessage"));
            Message.send(target, config.getString("control.playerControlMessage"));
            EndControl(target, (Player)sender);
            return true;
        }

        // Sets the spawn positions of the hack control
        else if (args[0] == "setup") {
            if (!sender.hasPermission("hackcontrol.control.setup")) {
                Message.send(sender, config.getString("errors.noPermission"));
                return true;
            }

            if (!(sender instanceof Player)) {
                Message.send(sender, config.getString("errors.notPlayer"));
                return true;
            }

            if (args.length == 1) {
                for (String s : config.getStringList("help.controlSetup")) {
                    Message.send(sender, s);
                }
                return true;
            }
            Player p = (Player)sender;
            Location loc = p.getLocation();

            if (args[1] == "stafferPos") {
                config.set("stafferPos.world", loc.getWorld().getName());
                config.set("stafferPos.x", loc.getBlockX());
                config.set("stafferPos.y", loc.getBlockY());
                config.set("stafferPos.z", loc.getBlockZ());
                config.set("stafferPos.yaw", loc.getYaw());
                config.set("stafferPos.pitch", loc.getPitch());
                plugin.saveConfig();
                stafferPos = loc;

                Message.send(sender, config.getString("setup.stafferPos"));
                return true;
            }
            else if (args[1] == "playerPos") {
                config.set("playerPos.world", loc.getWorld().getName());
                config.set("playerPos.x", loc.getBlockX());
                config.set("playerPos.y", loc.getBlockY());
                config.set("playerPos.z", loc.getBlockZ());
                config.set("playerPos.yaw", loc.getYaw());
                config.set("playerPos.pitch", loc.getPitch());
                plugin.saveConfig();
                playerPos = loc;

                Message.send(sender, config.getString("setup.playerPos"));
                return true;
            }
            else if (args[1] == "endPos") {
                config.set("endPos.world", loc.getWorld().getName());
                config.set("endPos.x", loc.getBlockX());
                config.set("endPos.y", loc.getBlockY());
                config.set("endPos.z", loc.getBlockZ());
                config.set("endPos.yaw", loc.getYaw());
                config.set("endPos.pitch", loc.getPitch());
                plugin.saveConfig();
                endPos = loc;

                Message.send(sender, config.getString("setup.endPos"));
                return true;
            }
            else {
                for (String s : config.getStringList("help.controlSetup")) {
                    Message.send(sender, s);
                }
                return true;
            }
        }

        else if (args[0] == "reload") {
            if (!sender.hasPermission("hackcontrol.control.reload")) {
                Message.send(sender, config.getString("errors.noPermission"));
                return true;
            }

            plugin.reloadConfig();
            return true;
        }

        else {
            for (String s : config.getStringList("help.control")) {
                Message.send(sender, s);
            }
            return true;
        }

        return false;
    }

    private void StartControl(Player target, Player staffer) {
        FileConfiguration config = plugin.getConfig();

        target.teleport(playerPos);
        staffer.teleport(stafferPos);

        if(config.getBoolean("freezeDuringControl")) {
            Freeze.getInstance().FreezePlayer(target);
        }
    }

    private void EndControl(Player target, Player staffer) {
        FileConfiguration config = plugin.getConfig();

        target.teleport(endPos);
        staffer.teleport(endPos);

        if(config.getBoolean("freezeDuringControl")) {
            Freeze.getInstance().UnfreezePlayer(target);
        }
    }

    public Control getInstance() {
        return instance;
    }

    public void LoadLocations() {
        LoadLocation(stafferPos, "stafferPos");
        LoadLocation(playerPos, "playerPos");
        LoadLocation(endPos, "endPos");
    }

    private void LoadLocation(Location location, String name) {
        FileConfiguration config = plugin.getConfig();

        String worldName = config.getString(name + ".world");
        if (worldName == null) {
            Bukkit.getLogger().severe(name + ".world doesn't exist within config.yml, could not load spawn location.");
            return;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().severe("Could not find world \"" + worldName + "\", could not load spawn location.");
            return;
        }
        int x = config.getInt(name + ".x");
        int y = config.getInt(name + ".y");
        int z = config.getInt(name + ".z");
        float yaw = (float)config.getDouble(name + ".yaw");
        float pitch = (float)config.getDouble(name + ".pitch");
        this.stafferPos = new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> playerNames = new ArrayList<>();
        Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().toArray().length];
        Bukkit.getServer().getOnlinePlayers().toArray(players);
        for (Player p : players) {
            playerNames.add(p.getName());
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("start");
            options.add("cancel");
            options.add("setup");
            options.add("reload");
            return options;
        }
        else if (args.length == 2) {
            switch (args[0]) {
                case "start":
                    return playerNames;
                case "cancel":
                    return playerNames;
                case "setup":
                    List<String> setupOptions = new ArrayList<>();
                    setupOptions.add("stafferPos");
                    setupOptions.add("playerPos");
                    setupOptions.add("endPos");
                    return setupOptions;
            }
        }

        return null;
    }
}
