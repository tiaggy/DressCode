package org.midgard.appearanceplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.midgard.appearanceplugin.customitems.Clothes;
import org.midgard.appearanceplugin.playerdata.PlayerData;
import org.midgard.appearanceplugin.skins.ClothesGroupCommand;
import org.midgard.appearanceplugin.utils.AppearanceStorageUtility;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AppearancePlugin extends JavaPlugin {

	protected static HashMap<UUID, PlayerData> appearanceData = new HashMap<>();
	private static AppearancePlugin instance;

	public static AppearancePlugin getPlugin() {
		return instance;
	}

	public static HashMap<UUID, PlayerData> getAppearanceData() {
		return appearanceData;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		saveAllResources();

		this.instance = this;

		Keys.init();
		registerClothesFromConfig();
		getLogger().info("Loading player appearance data...");
		appearanceData = AppearanceStorageUtility.loadAppearance();
		if (appearanceData == null) {
			appearanceData = new HashMap<>();
		}
		getLogger().info(appearanceData.size() + " player appearance data loaded.");
		getServer().getPluginManager().registerEvents(new Listeners(), this);

		this.getCommand("clothes").setExecutor(new ClothesGroupCommand());
		this.getCommand("clothes").setTabCompleter(new ClothesGroupCommand());

		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			AppearanceStorageUtility.saveAppearance();
		}, 0L, 20L * 300);
	}

	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (ClothesMenu.hasOpenedMenu(player)) {
				player.closeInventory();
			}
		}
		getLogger().info("Saving player appearance data...");
		AppearanceStorageUtility.saveAppearance();
		getLogger().info("Plugin disabled...");
	}

	private void saveAllResources() {

		File[] folders = {
				new File(getDataFolder(), "assets/1 - Hats"),
				new File(getDataFolder(), "assets/0 - Basic Skins/skins"),
				new File(getDataFolder(), "assets/0 - Basic Skins/custom-skins"),
				new File(getDataFolder(), "assets/skin-data"),
				new File(getDataFolder(), "assets/temp")
		};
		for (File folder : folders) {
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}


		File assets = new File(getDataFolder(), "assets");
		for (String folder : assets.list()) {
			if (folder.equals("1 - Hats")
					|| folder.equals("2 - Chestplates")
					|| folder.equals("3 - Trousers")
					|| folder.equals("4 - Boots")) {
				for (String file : new File(assets, folder).list()) {
					if (file.endsWith(".png")) {
						return;
					}
				}
			}
		}


		String[] resources = {
				"assets/1 - Hats/crown.png",
				"assets/2 - Chestplates/gr_jacket.png",
				"assets/3 - Trousers/cargo.png",
				"assets/4 - Boots/kraduli.png"
		};

		for (String resource : resources) {
			saveResource(resource, false);
			getLogger().info("Saved resource: " + resource);
		}
	}

	public void registerClothesFromConfig() {
		getLogger().info("Loading clothes from config...");
		FileConfiguration config = getConfig();

		List<Map<?, ?>> hatsList = config.getMapList("hats");
		List<Map<?, ?>> chestplatesList = config.getMapList("chestplates");
		List<Map<?, ?>> trousersList = config.getMapList("trousers");
		List<Map<?, ?>> bootsList = config.getMapList("boots");

		getLogger().info(
				(loadClothes(hatsList, "assets/1 - Hats/", Keys.HAT_KEY) +
						loadClothes(chestplatesList, "assets/2 - Chestplates/", Keys.CHEST_KEY) +
						loadClothes(trousersList, "assets/3 - Trousers/", Keys.LEGS_KEY) +
						loadClothes(bootsList, "assets/4 - Boots/", Keys.BOOTS_KEY)) + " clothes loaded."
		);
	}

	private int loadClothes(List<Map<?, ?>> clothesList, String path, NamespacedKey key) {
		int clothesLoaded = 0;
		if (clothesList != null) {
			for (Map<?, ?> clothesMap : clothesList) {
				String fileName = (String) clothesMap.get("file_name");
				File file = new File(getDataFolder(), path + fileName + ".png");
				if (!file.exists()) {
					getLogger().warning("Clothes file not found: " + file.getAbsolutePath());
					continue;
				}

				String name = (String) clothesMap.get("name");
				String type = (String) clothesMap.get("material");
				Map<Integer, Material> craftMap = null;

				if (clothesMap.containsKey("craft")) {
					craftMap = parseCraftMap((Map<Integer, String>) clothesMap.get("craft"));
				}
				if (Clothes.register(key, type, name, fileName, craftMap)) {
					clothesLoaded++;
				}

			}
		}
		return clothesLoaded;
	}

	private Map<Integer, Material> parseCraftMap(Map<Integer, String> craftStringMap) {
		Map<Integer, Material> craftMap = new HashMap<>();
		for (Map.Entry<Integer, String> entry : craftStringMap.entrySet()) {
			Integer key = entry.getKey();
			Material value = Material.getMaterial(entry.getValue());
			if (value == null) {
				getLogger().warning("Invalid material: " + entry.getValue() + " for key: " + key);
				continue;
			}
			craftMap.put(key, value);
		}
		return craftMap;
	}

	public static PlayerData getPlayerAppearance(Player player) {
		if (!appearanceData.containsKey(player.getUniqueId())) {
			FileConfiguration config = instance.getConfig();
			String defaultHat = config.getString("defaultClothes.hat");
			if (defaultHat == null || !Clothes.getHats().containsKey(defaultHat)) {
				defaultHat = "0";
			}
			String defaultChest = config.getString("defaultClothes.chestplate");
			if (defaultChest == null || !Clothes.getChests().containsKey(defaultChest)) {
				defaultChest = "0";
			}
			String defaultTrousers = config.getString("defaultClothes.trousers");
			if (defaultTrousers == null || !Clothes.getLegs().containsKey(defaultTrousers)) {
				defaultTrousers = "0";
			}
			String defaultBoots = config.getString("defaultClothes.boots");
			if (defaultBoots == null || !Clothes.getBoots().containsKey(defaultBoots)) {
				defaultBoots = "0";
			}

			PlayerData playerData = new PlayerData("0", "0", defaultHat, defaultChest, defaultTrousers, defaultBoots);
			appearanceData.put(player.getUniqueId(), playerData);
			return playerData;
		}
		PlayerData appearance = appearanceData.get(player.getUniqueId());
		return appearance;
	}

}
