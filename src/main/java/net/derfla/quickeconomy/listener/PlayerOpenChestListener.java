package net.derfla.quickeconomy.listener;

import net.derfla.quickeconomy.util.BlockOwner;
import net.derfla.quickeconomy.util.Styles;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerOpenChestListener implements Listener {

    @EventHandler
    public void onPlayerOpenChest(InventoryOpenEvent event){
        if (!event.getInventory().getType().equals(InventoryType.CHEST)) return;
        if (event.getInventory().getHolder() == null) return;
        if (!(event.getInventory().getHolder() instanceof Chest)) return;
        Chest chest = (Chest) event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();
        // Check if chest is locked
        if (BlockOwner.isLockedForPlayer(chest, player.getName())) {
            player.sendMessage(Component.translatable("shop.locked.chest", Styles.ERRORSTYLE));
            event.setCancelled(true);
            return;
        }
        if (BlockOwner.isShopOpen(chest)) {
            player.sendMessage(Component.translatable("shop.locked.owner", Styles.ERRORSTYLE));
            event.setCancelled(true);
            return;
        }
        if (BlockOwner.isShop(chest)) {
            BlockOwner.setShopOpen(chest, true);
        }
    }
}
