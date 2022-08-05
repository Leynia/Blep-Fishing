package com.kunfury.blepFishing.Tournament;

import com.kunfury.blepFishing.Config.TournamentType;
import com.kunfury.blepFishing.Config.Variables;
import com.kunfury.blepFishing.Miscellaneous.ItemHandler;
import com.kunfury.blepFishing.Miscellaneous.Utilities;
import com.kunfury.blepFishing.Objects.FishObject;
import com.kunfury.blepFishing.Setup;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.kunfury.blepFishing.Config.Variables.Prefix;

public class Rewards {

    public static HashMap<UUID, List<ItemStack>> UnsavedRewards = new HashMap<>();
    List<OfflinePlayer> rewardedPlayers = new ArrayList<>();
    public void Generate(TournamentObject t){


        HashMap<OfflinePlayer, List<String>> winners;

        if(t.Type == TournamentType.AMOUNT)
            winners = getAmountRewards(t);
        else
            winners = getRewards(t);

        if(winners != null && !winners.isEmpty()){
            winners.forEach((player, reward) -> {
                giveRewards(reward, player);
            });
        }

        //TODO: Ensure the default winners are rewarded
        List<String> defaultRewards = t.Rewards.get("DEFAULT");
        if(defaultRewards != null && defaultRewards.size() > 0){
            for(var p : t.getParticipants()){
                UUID uuid = p.getUniqueId();
                for(var r : defaultRewards){
                    ItemHandler.parseReward(r, uuid);
                }
            }
        }

        SaveRewards();
    }

    private HashMap<OfflinePlayer, List<String>> getAmountRewards(TournamentObject t){
        HashMap<OfflinePlayer, List<String>> rewards = new HashMap<>();

        HashMap<OfflinePlayer, Integer> winners = t.getWinnersAmount();

        Object[] a = winners.entrySet().toArray();
        Arrays.sort(a, (Comparator) (o1, o2) -> ((Map.Entry<OfflinePlayer, Integer>) o2).getValue()
                .compareTo(((Map.Entry<OfflinePlayer, Integer>) o1).getValue()));

        List<OfflinePlayer> players = new ArrayList<>();

        for(int i = 0; i < a.length; i++){
            Object o = a[i];

            int amount = ((Map.Entry<OfflinePlayer, Integer>) o).getValue();
            OfflinePlayer p = ((Map.Entry<OfflinePlayer, Integer>) o).getKey();

            if(players.size() <= 0) players.add(p);
            else{
                for(int f = 0; f < players.size(); f++){
                    OfflinePlayer lPlayer = players.get(i);
                    if(winners.get(lPlayer) < amount){
                        players.add(f, p);
                        break;
                    }
                    if(f == players.size() - 1) //Put at end if smaller than all others
                        players.add(p);
                }
            }
        }

        for(int i = 0; i < players.size(); i++){
            rewards.put(players.get(i), t.Rewards.get(String.valueOf(i + 1)));
        }

        return rewards;
    }

    private HashMap<OfflinePlayer, List<String>> getRewards(TournamentObject t){
        HashMap<OfflinePlayer, List<String>> winners = new HashMap<>();

        t.getWinners().forEach((rank, fish) -> {
            winners.put(fish.getPlayer(), t.Rewards.get(String.valueOf(rank)));
        });

        return winners;
    }

    private void giveRewards(List<String> rewardStrs, OfflinePlayer p){
        if(p != null && rewardStrs != null){
            rewardedPlayers.add(p);
            UUID uuid = p.getUniqueId();
            for(var r : rewardStrs){
                ItemHandler.parseReward(r, uuid);
            }
        }


    }

    public static void AddReward(UUID uuid, ItemStack item){
        if(item == null) return;

        List<ItemStack> items = UnsavedRewards.get(uuid);
        if(items == null) items = new ArrayList<>();
        items.add(item);
        UnsavedRewards.put(uuid, items);
    }

    public static void AddRewards(UUID uuid, List<ItemStack> items){
        if(items == null || items.size() <= 0) return;

        if(UnsavedRewards.get(uuid) != null) items.addAll(UnsavedRewards.get(uuid));
        UnsavedRewards.put(uuid, items);
    }

    public void SaveRewards(){
        if(UnsavedRewards.size() <= 0) return;

        final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        //Grabs the collection Asynchronously
        scheduler.runTaskAsynchronously(Setup.getPlugin(), () -> {
            UnsavedRewards.forEach((uuid, rewards) -> {
                try {
                Files.createDirectories(Paths.get(Setup.dataFolder + "/Rewards"));
                String fileName = Setup.dataFolder + "/Rewards/" + uuid + ".json";
                File myObj = new File(fileName);
                myObj.createNewFile();
                FileWriter myWriter = new FileWriter(fileName, true);
                Variables.SerializeItemList(rewards).forEach(serializedItem ->{
                    String message = serializedItem + System.lineSeparator();
                    try {
                        myWriter.write(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                    myWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            scheduler.runTask(Setup.getPlugin(), () -> {
                UnsavedRewards.forEach((uuid, rewards) -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if(p != null){
                        p.sendMessage(Prefix + String.format(Variables.getMessage("claimRewards"), rewards.size()));
                        p.sendMessage(Variables.Prefix + Variables.getMessage("claimRewards2"));
                    }
                });
                UnsavedRewards.clear();
            });
        });
    }

    public List<ItemStack> LoadRewards(UUID uuid){
        List<ItemStack> items = new ArrayList<>();

        String fileName = Setup.dataFolder + "/Rewards/" + uuid + ".json";
        File file = new File(fileName);

        if(file.exists()) {
            try {
                FileReader fr = new FileReader(fileName);
                BufferedReader in = new BufferedReader(fr);
                String str;
                List<String> list = new ArrayList<>();
                while ((str = in.readLine()) != null) {
                    list.add(str);
                }
                in.close();
                items.addAll(Variables.DeserializeItemList(list));

                if(!file.delete())
                {
                    String msg = Prefix + "Failed to delete the rewards file for " + uuid;
                    Bukkit.getLogger().warning(msg);
                }

                //TODO: Delete the file afterwards
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return items;
    }

    public void Claim(Player p){
        UUID uuid = p.getUniqueId();
        List<ItemStack> rewards = LoadRewards(uuid);

        if(Utilities.getFreeSlots(p.getInventory()) <= 0){
            p.sendMessage(Prefix + Variables.getMessage("inventoryFull"));
            AddRewards(uuid, rewards);
            SaveRewards();
            return;
        }

        if(rewards.size() > 0){
            for(var i : rewards){
                ItemHandler.GivePlayer(uuid, i);
            }

            SaveRewards();
        }
        else p.sendMessage(Prefix + Variables.getMessage("noRewards"));



        //TODO: After claiming, tell player amount left
        //TODO: Possibly add on
    }

}
