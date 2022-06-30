package com.kunfury.blepFishing.Crafting.Equipment.FishBag;

import com.kunfury.blepFishing.Miscellaneous.Variables;
import com.kunfury.blepFishing.Objects.BaseFishObject;
import com.kunfury.blepFishing.Objects.FishObject;
import com.kunfury.blepFishing.Setup;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class UseFishBag {
    public void UseBag(ItemStack bag, Player player){
        String bagId = NBTEditor.getString(bag, "blep", "item", "fishBagId");
        //p.sendMessage("Bag UUID: " + bagId);

        final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        //Grabs the collection Asynchronously
        scheduler.runTaskAsynchronously(Setup.getPlugin(), () -> {
            final List<FishObject> tempFish = new ParseFish().RetrieveFish(bagId, "ALL");


            scheduler.runTask(Setup.getPlugin(), () -> {
                new UpdateBag().ShowBagInv(tempFish, player, bagId, bag);
            });
        });
    }


    public void FillBag(ItemStack bag, Player p){
        p.sendMessage("In Progress: Fill Bag. Pulls all fish from inventory into bag");
    }

    public void TogglePickup(ItemStack bag, Player p){
        boolean enabled = NBTEditor.getBoolean(bag, "blep", "item", "fishBagAutoPickup" );

        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        if(enabled){
            bag = NBTEditor.set(bag, false, "blep", "item", "fishBagAutoPickup" );
            bag.removeEnchantment(Enchantment.DURABILITY);
            p.sendMessage(Variables.Prefix + "Auto Pickup Disabled.");
        }else{
            bag = NBTEditor.set(bag, true, "blep", "item", "fishBagAutoPickup" );
            bag.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            p.sendMessage(Variables.Prefix + "Auto Pickup Enabled.");
        }

        ItemMeta m = bag.getItemMeta();
        bag.setItemMeta(m);

        p.getInventory().setItemInMainHand(bag);
    }

    /**
     * Moves fish from inventory to bag
     * @param bag
     * @param p
     * @param fish
     */
    public void AddFish(ItemStack bag, Player p, ItemStack fish){
        if(BagInfo.IsFull(bag)){
            p.sendMessage(Variables.Prefix + "There is no more space in that bag.");
            return;
        }
        String bagId = NBTEditor.getString(bag, "blep", "item", "fishBagId");
        if(bagId == null || bagId.isEmpty()) return;

        String fishId = NBTEditor.getString(fish, "blep", "item", "fishId");
        FishObject fishObj = new ParseFish().FishFromId(fishId);

        if(fishObj == null){
            p.sendMessage(Variables.Prefix + ChatColor.RED + "That fish is from a previous version and cannot be stored in the bag. Sorry.");
            return;
        }

        if(fishObj.BagID != null){
            p.sendMessage(Variables.Prefix + ChatColor.RED + "That fish is already stored in anothe bag somewhere. This shouldn't happen.");
            return;
        }
        fishObj.BagID = bagId;
        fish.setAmount(0);
        Variables.UpdateFishData();
        new UpdateBag().Update(bag, p);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .33f, 1f);
    }
}