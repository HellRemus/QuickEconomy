package net.derfla.quickeconomy.command;

import net.derfla.quickeconomy.Main;
import net.derfla.quickeconomy.util.DatabaseManager;
import net.derfla.quickeconomy.util.Styles;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuickeconomyCommand implements TabExecutor {

    static Plugin plugin = Main.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] strings) {
        if (strings.length >= 1) {
            switch (strings[0].toLowerCase()){
                case "migrate":
                    if(!sender.hasPermission("quickeconomy.migrate") && sender instanceof Player) {
                        break;
                    }
                    if(Main.SQLMode) {
                        DatabaseManager.migrateToBalanceFile();
                        plugin.getConfig().set("database.enabled", false);
                        plugin.saveConfig();
                        Main.SQLMode = false;
                        sender.sendMessage("Migrated to file mode. You should consider restarting the server.");
                        return true;
                    } else {
                        try{
                            DatabaseManager.connectToDatabase();
                            DatabaseManager.createTables();
                            Main.SQLMode = true;
                            plugin.getConfig().set("database.enabled", true);
                            plugin.saveConfig();
                            sender.sendMessage("Migrated to database. You should consider restarting the server.");
                        } catch (Exception e) {
                            sender.sendMessage("Failed to connect to database! \n See console for more info!");
                            plugin.getLogger().warning("Failed to connect to database! " + e.getMessage());
                        }
                        DatabaseManager.migrateToDatabase();
                        return true;
                    }
                case "rollback":
                    if(!Main.SQLMode) {
                        // Rollback is only available when connected to a database
                        break;
                    }
                    if(!sender.hasPermission("quickeconomy.rollback") && sender instanceof Player) {
                        break;
                    }
                    Timestamp rollbackTime;
                    String timestampString;
                    try {
                        timestampString = strings[1] + "-" + strings[2] + "-" + strings[3] + " " + strings[4];
                        rollbackTime = Timestamp.valueOf(timestampString);
                    }catch (Exception e){
                        sender.sendMessage("Incorrect date format!");
                        plugin.getLogger().info("Rollback failed: " + e.getMessage());
                        return true;
                    }
                    try {
                        DatabaseManager.rollback(String.valueOf(rollbackTime), false);
                        sender.sendMessage("Completed rollback!");
                        plugin.getLogger().info("Rollback complete to " + timestampString);
                    } catch (Exception e){
                        sender.sendMessage("Failed rollback!");
                        plugin.getLogger().info("Rollback failed: " + e.getMessage());
                        return true;
                    }
                    return true;
            }
        }

        // Send applicable help messages based on permissions
        if (sender.hasPermission("quickeconomy.balance")) {
            sender.sendMessage(Component.translatable("qecommand.balance", Styles.INFOSTYLE));
        }
        if (sender.hasPermission("quickeconomy.balance.seeall")) {
            sender.sendMessage(Component.translatable("qecommand.balance.seeall", Styles.INFOSTYLE));
        }
        if (sender.hasPermission("quickeconomy.balance.modifyall")) {
            sender.sendMessage(Component.translatable("qecommand.balance.modifyall", Styles.INFOSTYLE));
        }
        if (sender.hasPermission("quickeconomy.shop.create")) {
            sender.sendMessage(Component.translatable("qecommand.shop.create", Styles.INFOSTYLE));
        }
        if (sender.hasPermission("quickeconomy.bank")) {
            sender.sendMessage(Component.translatable("qecommand.bank", Styles.INFOSTYLE));
        }
        if (sender.hasPermission("quickeconomy.bank.create")) {
            sender.sendMessage(Component.translatable("qecommand.bank.create", Styles.INFOSTYLE));
        }
        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 1){
            List<String> returnList = new ArrayList<>();
            if(sender.hasPermission("quickeconomy.migrate")){
                returnList.add("migrate");
            }
            if(sender.hasPermission("quickeconomy.rollback") && Main.SQLMode){
                returnList.add("rollback");
            }
            return returnList.stream()
                    .filter(subCommand -> subCommand.toLowerCase().startsWith(strings[0]))
                    .collect(Collectors.toList());
        }
        if(strings[0].equalsIgnoreCase("rollback") && sender.hasPermission("quickeconomy.rollback")) {
            switch (strings.length) {
                case 2:
                    return Stream.of(String.valueOf(LocalDate.now().getYear()))
                            .filter(subCommand -> subCommand.toLowerCase().startsWith(strings[1]))
                            .collect(Collectors.toList());
                case 3:
                    return Stream.of(String.valueOf(LocalDate.now().getMonthValue()))
                            .filter(subCommand -> subCommand.toLowerCase().startsWith(strings[2]))
                            .collect(Collectors.toList());
                case 4:
                    return Stream.of(String.valueOf(LocalDate.now().getDayOfMonth()))
                            .filter(subCommand -> subCommand.toLowerCase().startsWith(strings[3]))
                            .collect(Collectors.toList());
                case 5:
                    return Collections.singletonList("hh:mm:ss");
            }
        }
        return Collections.emptyList();
    }
}
