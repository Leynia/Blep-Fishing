package com.kunfury.blepFishing.CollectionLog;

import com.kunfury.blepFishing.Config.Variables;
import com.kunfury.blepFishing.Objects.CollectionLogObject;
import com.kunfury.blepFishing.Objects.FishObject;
import com.kunfury.blepFishing.Setup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectionHandler {


    public CollectionLogObject GetLog(Player p){
        CollectionLogObject logObject = null;

        String UUID = p.getUniqueId().toString();

        if(Variables.CollectionLogs.stream().anyMatch(log -> UUID.equals(log.getUUID())))
            logObject = Variables.CollectionLogs.stream()
                        .filter(log -> log.getUUID().equals(UUID))
                        .collect(toSingleton());

        if(logObject != null) return logObject;
        logObject = new CollectionLogObject(p);
        Variables.CollectionLogs.add(logObject);
        SaveLog();
        return logObject;

    }

    public void CaughtFish(Player p, FishObject fish){
        CollectionLogObject log = GetLog(p);
        log.CaughtFish(fish);
    }


    public void CraftedBag(Player p, String bagType){
        CollectionLogObject log = GetLog(p);

        //Bukkit.broadcastMessage("Crafted Bag: " + bagType);
    }

    public void SaveLog(){
        try {
            String logPath = Setup.dataFolder + "/Data" + "/collections.data";
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(logPath));

            output.writeObject(Variables.CollectionLogs);
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }


}
