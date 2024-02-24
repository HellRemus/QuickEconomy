package net.derfla.quickeconomy.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.derfla.quickeconomy.util.Balances;
import net.derfla.quickeconomy.util.TypeChecker;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BalanceCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] strings) {
        if (strings.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You can only see your balance as a player!");
                return true;
            }
            Player player = ((Player) sender).getPlayer();
            player.sendMessage("§eYour balance is: " + Balances.getPlayerBalance(player.getName()));
            return true;
        }
        if (strings.length == 1) {
            if (sender instanceof  Player && !(sender.hasPermission("quickeconomy.balance.seeall"))) {
                sender.sendMessage("§cIncorrect arguments! Use /bal send.");
                return true;
            }
            float balance = Balances.getPlayerBalance(strings[0]);
            if (balance == 0.0f) {
                sender.sendMessage("§e" + strings[0] + " does not seem to have an account!");
                return true;
            }
            sender.sendMessage( "§e" + strings[0] + "'s balance: " + balance);
            return true;
        }
        if (strings[1] == null) {
            return true;
        }

        if (!(TypeChecker.isFloat(strings[1]))){
            sender.sendMessage("§cPlease provide a number!");
            return true;
        }
        float money = Float.parseFloat(strings[1]);



        switch (strings[0].toLowerCase()) {
            case "set":
                if (sender instanceof  Player && !(sender.hasPermission("quickeconomy.balance.modifyall"))) {
                    sender.sendMessage("§cYou are not allowed to use this command!");
                    break;
                }


                if (strings.length == 2) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cPlease provide a player!");
                        break;
                    }
                    Player player = ((Player) sender).getPlayer();

                    Balances.setPlayerBalance(player.getName(), money);
                    player.sendMessage("§eMoney set!");
                    break;
                }
                Balances.setPlayerBalance(strings[2], money);
                sender.sendMessage("§eSet balance of " + strings[2] + " to " + money);
                break;


            case "add":
                if (sender instanceof  Player && !(sender.hasPermission("quickeconomy.balance.modifyall"))) {
                    sender.sendMessage("§cYou are not allowed to use this command!");
                    break;
                }
                if (strings.length == 2) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Please provide a player!");
                        break;
                    }
                    Player player = ((Player) sender).getPlayer();

                    Balances.addPlayerBalance(player.getName(), money);
                    player.sendMessage("§eAdded " + money + " to your balance!");
                    break;
                }
                Balances.addPlayerBalance(strings[2], money);
                sender.sendMessage("§eAdded " + money + " to " + strings[2] + "'s balance!");
                break;

            case "subtract":
                if (sender instanceof  Player && !(sender.hasPermission("quickeconomy.balance.modifyall"))) {
                    sender.sendMessage("§cYou are not allowed to use this command!");
                    break;
                }
                if (strings.length == 2) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cPlease provide a player!");
                        break;
                    }
                    Player player = ((Player) sender).getPlayer();
                    Balances.subPlayerBalance(player.getName(), money);
                    player.sendMessage("§eSubtracted " + money + " from your balance!");
                    break;
                }
                Balances.subPlayerBalance(strings[2], money);
                sender.sendMessage("§eSubtracted " + money + " from " + strings[2] + "'s balance!");
                break;

            case "send":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cYou can only send coins as a player!");
                    break;
                }
                if (strings.length == 2) {
                    sender.sendMessage("§cPlease provide a player!");
                    break;
                }
                if (money < 0) {
                    sender.sendMessage("§cPlease use a positive number!");
                    break;
                }
                if (strings[2].equals(sender.getName())) {
                    sender.sendMessage("§cYou can't send coins to yourself!");
                    break;
                }
                if (Bukkit.getServer().getPlayer(strings[2]) == null || Balances.getPlayerBalance(strings[2]) == 0.0f) {
                    sender.sendMessage( "§cPlayer: "+ strings[2] + " does not seem to exist on this server!");
                    break;
                }
                Player player = ((Player) sender).getPlayer();
                if (Balances.getPlayerBalance(player.getName()) < money) {
                    player.sendMessage("§cYou do not have enough balance!");
                    break;
                }
                Balances.subPlayerBalance(player.getName(), money);
                Balances.addPlayerBalance(strings[2], money);
                player.sendMessage("§eSent " + money + " coins to " + strings[2] + "!");
                if (Bukkit.getPlayer(strings[2]) != null) {
                    // Alerts the receiving player if it's online
                    Player targetPlayer = Bukkit.getPlayer(strings[2]);
                    targetPlayer.sendMessage("§eYou just received " + money + " coins from " + player.getName() + "!");
                    break;
                }

                break;

            default:
                sender.sendMessage("§cInvalid arguments!");
                break;
        }
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            List<String> returnList = new ArrayList<>(Collections.singletonList("send"));
            if (sender.hasPermission("quickeconomy.balance.seeall") || !(sender instanceof Player)) {
                List<String> players =  Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                returnList.addAll(players);
            }
            if (sender.hasPermission("quickeconomy.balance.modifyall") || ! (sender instanceof Player)) {
                List<String> subCommands = Arrays.asList("set", "add", "subtract");
                returnList.addAll(subCommands);
            }
            return returnList.stream()
                    .filter(subCommand -> subCommand.toLowerCase().startsWith(strings[0]))
                    .collect(Collectors.toList());
        }
        if (strings.length == 2) {
            String balance;
            if (sender instanceof Player) {
                balance = String.valueOf(Balances.getPlayerBalance(sender.getName()));
            } else balance = "1001";
            return Stream.of("10", "100", "1000", balance)
                    .filter(amount -> amount.startsWith(strings[1]))
                    .collect(Collectors.toList());
        }
        if (strings.length == 3) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(player -> player.toLowerCase().startsWith(strings[2]))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
