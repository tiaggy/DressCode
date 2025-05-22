package org.midgard.appearanceplugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.midgard.appearanceplugin.AppearancePlugin;
import org.midgard.appearanceplugin.playerdata.PlayerData;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;

public class AppearanceStorageUtility {
	private static final String FILE_NAME = AppearancePlugin.getPlugin().getDataFolder().getAbsolutePath() + "/appearance.json";
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(PlayerData.class, new AppearanceTypeAdapter())
			.setPrettyPrinting()
			.create();
	private static final Type appearanceMapType = new TypeToken<HashMap<UUID, PlayerData>>() {
	}.getType();

	public static void saveAppearance() {
		try (Writer writer = new FileWriter(FILE_NAME)) {
			gson.toJson(AppearancePlugin.getAppearanceData(), writer);
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
			return new HashMap<>();
		}
	}
}
