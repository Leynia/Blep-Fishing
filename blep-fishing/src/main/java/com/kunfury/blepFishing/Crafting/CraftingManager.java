package com.kunfury.blepFishing.Crafting;

import com.kunfury.blepFishing.Crafting.Equipment.FishBag.BagInfo;
import com.kunfury.blepFishing.Crafting.Equipment.FishBag.FishBagRecipe;
import com.kunfury.blepFishing.Config.Variables;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingManager {

    public static NamespacedKey key;

    public void InitItems(){
        if(Variables.EnableFishBags) new FishBagRecipe().SmallBag();
    }

    public static void CheckBagCraft(CraftItemEvent e, ItemStack item){
        if(!BagInfo.IsBag(item) || BagInfo.IsBag(e.getInventory().getResult())) return;
        e.setCancelled(true);
        e.getWhoClicked().sendMessage(Variables.Prefix + ChatColor.RED + "You cannot use your bag for that.");
    }

}