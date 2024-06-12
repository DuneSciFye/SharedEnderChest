package me.dunescifye.sharedenderchest;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageMasterPlugin;
import net.quazar.offlinemanager.api.OfflineManagerAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static me.dunescifye.sharedenderchest.SharedEnderChest.getPlugin;

public class EnderChestOpen implements Listener {

    private OfflineManagerAPI offlineManagerAPI;
    private Map<Player, Inventory> openedInventories = new HashMap<>();
    private MarriageMasterPlugin marriageMaster;

    public void enderChestOpenHandler(SharedEnderChest plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        offlineManagerAPI = (OfflineManagerAPI) Bukkit.getPluginManager().getPlugin("OfflineManager");
        marriageMaster = (MarriageMasterPlugin) Bukkit.getPluginManager().getPlugin("MarriageMaster");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
            if (!marriageMaster.getPlayerData(p).isMarried()) return; //Open regular ender chest if not married

            e.setCancelled(true); //Cancel opening the regular ender chest
            p.openInventory(openInventory(p)); //Open shared ender chest
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory() != openedInventories.get(p)) return; //Ensure inventory is assigned inv

        Bukkit.getScheduler().runTask(getPlugin(), () -> { //Run task a tick later to get updated ender chest contents
            updateInventory(p, e.getInventory().getContents());
        });
    }
    
    @EventHandler
    public void onInventoryClick(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory() != openedInventories.get(p)) return; //Ensure inventory is assigned inv

        Bukkit.getScheduler().runTask(getPlugin(), () -> { //Run task a tick later to get updated ender chest contents
            updateInventory(p, e.getInventory().getContents());
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryCloseEvent e) { //Remove player from opened inventories map when closing inv
        Player p = (Player) e.getPlayer();
        if (e.getInventory() == openedInventories.get(p)) {
            openedInventories.remove(p);
        }
    }

    private Inventory openInventory(Player p) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Ender Chest"); //Create inv

        ItemStack[] enderChest = p.getEnderChest().getContents(); //Get player's ender chest contents

        for (int i = 0; i < enderChest.length; i++) { //Put items from player's ender chest to custom inv
            inventory.setItem(i, enderChest[i]);
        }

        String playerName = marriageMaster.getPlayerData(p).getPartner().getName(); //Get married partner

        ItemStack[] partnerEnderChestContents = Bukkit.getPlayerExact(playerName) != null ?
            Bukkit.getPlayerExact(playerName).getEnderChest().getContents() :  //If partner is online
            offlineManagerAPI.getPlayerData(playerName).getEnderChest().getEnderChest().getContents(); //If partner is offline

        for (int i = 0; i < partnerEnderChestContents.length; i++) { //Add partner's ender chest contents to custom inv
            inventory.setItem(i + 27, partnerEnderChestContents[i]);
        }

        openedInventories.put(p, inventory); //Assign this inventory to player's opened inv

        return inventory;
    }

    private void updateInventory(Player p, ItemStack[] inv) {
        Player partner = null;
        Inventory partnerEnderChest;
        String partnerName = marriageMaster.getPlayerData(p).getPartner().getName();

        if (Bukkit.getPlayerExact(partnerName) != null) { //If partner is online
            partner = Bukkit.getPlayerExact(partnerName);
            partnerEnderChest = partner.getEnderChest();

        } else { //If partner is offline
            partnerEnderChest = offlineManagerAPI.getPlayerData(marriageMaster.getPlayerData(p).getPartner().getName()).getEnderChest().getEnderChest();
        }

        for (int i = 0; i < 27; i++ ) { //Update players' actual ender chests
            p.getEnderChest().setItem(i, inv[i]);
            partnerEnderChest.setItem(i, inv[i + 27]);
        }

        if (openedInventories.containsKey(partner)) {
            partner.openInventory(openInventory(partner));
        }
    }

}
