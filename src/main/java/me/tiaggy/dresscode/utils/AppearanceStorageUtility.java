package me.tiaggy.dresscode.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.tiaggy.dresscode.DressCodePlugin;
import me.tiaggy.dresscode.models.PlayerData;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;

import static me.tiaggy.dresscode.DressCodePlugin.getPluginLogger;

public class AppearanceStorageUtility {

	private static final String FILE_NAME = DressCodePlugin.getPlugin().getDataFolder().getAbsolutePath() + "/appearance.json";
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(PlayerData.class, new AppearanceTypeAdapter())
			.setPrettyPrinting()
			.create();
	private static final Type appearanceMapType = new TypeToken<HashMap<UUID, PlayerData>>() {
	}.getType();

	public static void saveAppearance() {
		try (Writer writer = new FileWriter(FILE_NAME)) {
			gson.toJson(DressCodePlugin.getAppearanceData(), writer);
		} catch (IOException e) {
			getPluginLogger().severe("Failed to save appearance data: " + e.getMessage());
		}
	}

	public static HashMap<UUID, PlayerData> loadAppearance() {
		File file = new File(FILE_NAME);
		if (!file.exists()) {
			return new HashMap<>();
		}

		try (Reader reader = new FileReader(file)) {
			return gson.fromJson(reader, appearanceMapType);
		} catch (IOException e) {
			getPluginLogger().severe("Failed to load appearance data: " + e.getMessage());
			return new HashMap<>();
		}
	}
}
