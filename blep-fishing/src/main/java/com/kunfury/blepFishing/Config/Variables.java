package com.kunfury.blepFishing.Config;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.kunfury.blepFishing.Objects.*;
import com.kunfury.blepFishing.Tournament.Tournament;
import org.bukkit.conversations.ConversationFactory;

import com.kunfury.blepFishing.Setup;
import com.kunfury.blepFishing.Signs.FishSign;

import com.kunfury.blepFishing.Conversations.ConFactory;
import com.kunfury.blepFishing.Tournament.SaveTournaments;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class Variables {

	//region Static Lists
	public static List<BaseFishObject> BaseFishList = new ArrayList<>(); //Available fish to catch
	public static List<RarityObject> RarityList = new ArrayList<>(); //Available rarities
	public static List<AreaObject> AreaList = new ArrayList<>(); //Available Areas

	public static List<TournamentObjectOld> Tournaments = new ArrayList<>();

	public static List<String> AllowedWorlds = new ArrayList<>();
	//endregion

	//region Static Booleans
	public static boolean HighPriority = false;
	public static boolean TournamentOnly = false;
	public static boolean TournamentRunning;
	public static boolean WorldsWhitelist = false;
	public static boolean RequireAreaPerm = false;
	public static boolean AllowWanderingTraders = false;
	public static boolean LegendaryFishAnnounce = true;
	//public static boolean UseEconomy = true;
	public static boolean EnableFishBags = true;
	public static boolean DebugMode = false;
	//endregion

	//region Unique Static
		//Dictionary containing lists of all caught fish
		//String =
	public static HashMap<String, List<FishObject>> FishDict = new HashMap<>();
	public static List<CollectionLogObject> CollectionLogs = new ArrayList<>();
	public static ConversationFactory ConFactory = new ConFactory().GetFactory();
	private static ResourceBundle Messages;
	public static double TraderMod = 1;
	//endregion



	//endregion


	//region Private Variables
	private static List<String> FishNameList = new ArrayList<>();

	//endregion

	public static int RarityTotalWeight;
	public static int FishTotalWeight;
	public static double ParrotBonus;
	public static double BoatBonus;
	
	public static String Prefix;
	
	public static String CurrSym = "$"; //The global currency symbol
	public static String SizeSym = "\""; //The global size symbol


	/**
	 * @param f The fish to be saved
	 * Desc: Stores the fish in the Dict and then to file
	 * Key is the fish name and the data stored is a list of the fish
	 */
	//Handles saving the fish to the local dictionary
	public static void AddToFishDict(FishObject f) {
		String fishName = f.Name.toUpperCase(); //Gets the name of the fish to be saved
		
		List<FishObject> list = new ArrayList<>();
		
		if(FishDict.get(fishName) != null) {
			list = FishDict.get(fishName);
		}
		
		
		list.add(f);

		if(list.size() <= 0) return;
		FishDict.put(fishName, list);
		UpdateFishData();

		new FishSign().UpdateSigns();
	}
	
	public static List<FishObject> GetFishList(String fishName) {
		fishName = fishName.toUpperCase();
		boolean fishFound = false;

		if(fishName.equalsIgnoreCase("ALL")) fishFound = true;
		else{
			//Ensures the fish exists, else returns null
			for (BaseFishObject f : BaseFishList) {
				if(fishName.equals(f.Name.toUpperCase())) {
					fishFound = true;
					break;
				}
			}
		}
		
		if(fishFound || fishName.equals("ALL")) { //If the fish is found, get all caught
			List<FishObject> fishList = new ArrayList<>();
			
			if(!fishName.equals("ALL")) {
				fishList = FishDict.get(fishName);
			}else {
				 for (Entry<String, List<FishObject>> entry : FishDict.entrySet()) {
					fishList.addAll(entry.getValue());				 
				 }
			}		
			if(fishList != null && fishList.size() > 0)
				Collections.sort(fishList, Collections.reverseOrder());
			else
				fishList = new ArrayList<>();
			return fishList;
		}else
			return null;
		
	}

	public static void UpdateFishData(){
		try {
			String dictPath = Setup.dataFolder + "/Data" + "/fish.data";
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(dictPath));

			output.writeObject(FishDict);
			output.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void AddTournament(TournamentObjectOld tourney) {
		if(tourney != null)
			Tournaments.add(tourney);
		new SaveTournaments();

		new Tournament().CheckActiveTournaments();
	}

	public static List<String> SerializeItemList(List<ItemStack> items){
		List<String> serializedItems = new ArrayList<>();
		items.forEach(item -> {
			try {
				ByteArrayOutputStream io = new ByteArrayOutputStream();
				BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
				os.writeObject(item);
				os.flush();

				byte[] serializeObject = io.toByteArray();
				serializedItems.add(Base64.getEncoder().encodeToString(serializeObject));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return serializedItems;
	}

	public static List<ItemStack> DeserializeItemList(List<String> data){
		List<ItemStack> items = new ArrayList<>();

		data.forEach(d -> {
			byte[] serializedObject = Base64.getDecoder().decode(d);

			try {
				ByteArrayInputStream in = new ByteArrayInputStream(serializedObject);
				BukkitObjectInputStream is= new BukkitObjectInputStream(in);
				items.add((ItemStack) is.readObject());
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		});


		return items;
	}

	public static List<String> GetFishNames(){
		if(FishNameList.size() < BaseFishList.size()){
			FishNameList.clear();
			for(BaseFishObject f : BaseFishList){
				FishNameList.add(f.Name);
			}
		}


		return FishNameList;
	}

	public static String getMessage(String key){
		if(Messages == null) return "Not Found";
		else{
			return Messages.getString(key);
		}
	}

	public static void setMessagesBundle(ResourceBundle resource){
		if(resource != null)Messages = resource;
		else{
			Messages = ResourceBundle.getBundle("resource");
		}

	}
}
