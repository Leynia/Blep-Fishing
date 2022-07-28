package com.kunfury.blepFishing.Miscellaneous;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.kunfury.blepFishing.Config.Variables;
import com.kunfury.blepFishing.Crafting.Equipment.FishBag.ParseFish;
import com.kunfury.blepFishing.Crafting.Equipment.FishBag.UpdateBag;
import com.kunfury.blepFishing.Objects.FishObject;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.kunfury.blepFishing.Setup;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.scheduler.BukkitScheduler;

public class FishEconomy {

	protected static List<String> PlayerWaitList = new ArrayList<>();
	//Handles the selling of fish
	public static void SellFish(Player p, double priceMod) {
		if(p.isSneaking()) {
			if(PlayerWaitList.contains(p.toString())) {
				SellFish(p, true, priceMod);
				PlayerWaitList.remove(p.toString());
			}else {
				PlayerWaitList.add(p.toString());
				p.sendMessage(Variables.Prefix + "Sell all fish? Sneak Right-Click again to confirm.");
			}
		}else
			SellFish(p, false, priceMod);
	}


	public static void SellFish(Player player, boolean sellAll, double priceMod) {
		Economy econ = Setup.getEconomy();
		if(econ != null){
			List<ItemStack> itemList = new ArrayList<>();
			if(sellAll) { //Runs if the player is wanting to sell all fish
				for (ItemStack item : player.getInventory().getContents()) {
					if(item != null && item.getType() == Material.SALMON) {
						itemList.add(item);
					}
				}
			}else
				itemList.add(player.getInventory().getItemInMainHand());

			double total = 0;

			//Adds the cost of all the fish together.
			for (ItemStack item : itemList)
			{

				double value = NBTEditor.getDouble( item, "blep", "item", "fishValue" );
				value *= priceMod;
				total += value;
			}

			if(total > 0) {
				EconomyResponse r = econ.depositPlayer(player, total);
				if(r.transactionSuccess()) {
					if(itemList.size() == 1){
						ItemStack item = itemList.get(0);
						player.sendMessage(Formatting.formatColor("Sold " + Objects.requireNonNull(item.getItemMeta()).getDisplayName()
										+ String.format("&f for &a%s", econ.format(r.amount))));
						item.setAmount(item.getAmount() - 1);
					}else{
						player.sendMessage(ChatColor.translateAlternateColorCodes('&',
								Variables.Prefix + "Sold " + itemList.size() + " fish"
										+ "&f for " + ChatColor.GREEN +Variables.CurrSym + Setup.getEconomy().format(total) + "." ));
						for(var i : itemList){
							i.setAmount(0);
						}
					}
				} else {
					player.sendMessage(String.format("An error occured: %s", r.errorMessage));
				}
			}else player.sendMessage(Variables.Prefix + "Those fish aren't worth anything.");
		}
	}

	public static void SellFish(List<FishObject> fishList, Player p, double priceMod, ItemStack bag){
		Economy econ = Setup.getEconomy();
		if(econ == null) return;
		double total = 0;

		//Adds the cost of all the fish together.
		for (var f : fishList)
		{
			total += f.RealCost * priceMod;
		}

		if(total > 0) {
			EconomyResponse r = econ.depositPlayer(p, total);
			if(r.transactionSuccess()) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&',
						Variables.Prefix + "Sold " + fishList.size() + " fish"
								+ "&f for " + ChatColor.GREEN +Variables.CurrSym + Setup.getEconomy().format(total) + "." ));
				for(var f : fishList){
					f.BagID = null;
				}
				new UpdateBag().Update(bag, p, false);
				Variables.UpdateFishData();
			} else {
				p.sendMessage(String.format("An error occured: %s", r.errorMessage));
			}
		}else p.sendMessage(Variables.Prefix + "Those fish aren't worth anything.");
	}

	public static void SellBag(Player p, ItemStack bag, double priceMod){
		if(!p.isSneaking()){
			p.sendMessage(Variables.Prefix + "Sneak Right-Click to sell all fish in bag.");
			return;
		}

		if(!PlayerWaitList.contains(p.toString())) {
			PlayerWaitList.add(p.toString());
			p.sendMessage(Variables.Prefix + "Sell all fish in bag? Sneak Right-Click again to confirm.");
			return;
		}


		final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		String bagId = NBTEditor.getString(bag, "blep", "item", "fishBagId");
		//Grabs the collection Asynchronously
		scheduler.runTaskAsynchronously(Setup.getPlugin(), () -> {
			final List<FishObject> tempFish = new ParseFish().RetrieveFish(bagId, "ALL");

			scheduler.runTask(Setup.getPlugin(), () -> {
				SellFish(tempFish, p, priceMod, bag);
				PlayerWaitList.remove(p.toString());
			});
		});


	}

}



