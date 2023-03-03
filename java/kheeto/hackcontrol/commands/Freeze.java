package kheeto.hackcontrol.commands;

import kheeto.hackcontrol.HackControl;
import kheeto.hackcontrol.util.CommandBase;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.reflect.Array;
import java.util.*;

public class Freeze implements CommandExecutor, Listener, TabCompleter {

    private static Freeze instance;
    private HackControl plugin;
    private static List<Player> frozenPlayers;

    public Freeze(HackControl plugin) {
        instance = this;
        this.plugin = plugin;
        List<Player> list = Arrays.asList();
        frozenPlayers = new ArrayList<>(list);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = HackControl.getInstance().getConfig();

        if (!sender.hasPermission("hackcontrol.freeze")) {
            Message.send(sender, config.getString("errors.noPermission")
                    .replace("{staffer}", sender.getName()));
            return true;
        }

        if (args.length == 0) {
            Message.send(sender, config.getString("errors.noPlayer")
                    .replace("{staffer}", sender.getName()));
            return true;
        }

        Player p = Bukkit.getPlayer(args[0]);
        if (p == null) {
            Message.send(sender, config.getString("errors.noPlayerFound")
                    .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
            return true;
        }

        if (p.hasPermission("hackcontrol.freeze.bypass")) {
            Message.send(sender, config.getString("errors.immunePlayer")
                    .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
            return true;
        }

        if (args.length == 1) {
            if (frozenPlayers.contains(p)) {
                frozenPlayers.remove(p);
                Message.send(sender, config.getString("freeze.stafferUnfreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                Message.send(p, config.getString("freeze.playerUnfreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                return true;
            } else if (!frozenPlayers.contains(p)) {
                frozenPlayers.add(p);
                Message.send(sender, config.getString("freeze.stafferFreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                Message.send(p, config.getString("freeze.playerFreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                return true;
            }
        } else if (args.length > 1) {
            if (Boolean.parseBoolean(args[1])) {
                if (frozenPlayers.contains(p)) {
                    Message.send(sender, config.getString("errors.alreadyFrozen")
                            .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                    return true;
                }

                frozenPlayers.add(p);
                Message.send(sender, config.getString("freeze.stafferFreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                Message.send(p, config.getString("freeze.playerFreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                return true;
            } else {
                if (!frozenPlayers.contains(p)) {
                    Message.send(sender, config.getString("errors.notFrozen")
                            .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                    return true;
                }

                frozenPlayers.remove(p);
                Message.send(sender, config.getString("freeze.stafferUnfreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                Message.send(p, config.getString("freeze.playerUnfreezeMessage")
                        .replace("{player}", p.getName()).replace("{staffer}", sender.getName()));
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e) {
        if (frozenPlayers.contains(e.getPlayer()))
            e.setCancelled(true);
    }

    public static Freeze getInstance() {
        return instance;
    }

    public void FreezePlayer(Player p) {
        if (!frozenPlayers.contains(p))
            frozenPlayers.add(p);
    }

    public void UnfreezePlayer(Player p) {
        if (frozenPlayers.contains(p))
            frozenPlayers.remove(p);
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
            return playerNames;
        }
        else if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("true");
            options.add("false");
            return options;
        }

        return null;
    }
}
