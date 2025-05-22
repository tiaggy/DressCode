package org.midgard.appearanceplugin.utils;

import com.google.gson.*;
import org.midgard.appearanceplugin.playerdata.PlayerData;

import java.lang.reflect.Type;

public class AppearanceTypeAdapter implements JsonSerializer<PlayerData>, JsonDeserializer<PlayerData> {

	@Override
	public JsonElement serialize(PlayerData src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("basicSkin", src.getBasicSkin());
		jsonObject.addProperty("playerState", src.getPlayerState());
		jsonObject.addProperty("hat", src.getHat());
		jsonObject.addProperty("chest", src.getChest());
		jsonObject.addProperty("trousers", src.getTrousers());
		jsonObject.addProperty("boots", src.getBoots());
		return jsonObject;
	}

	@Override
	public PlayerData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		String basicSkin = jsonObject.get("basicSkin").getAsString();
		String playerState = jsonObject.get("playerState").getAsString();
		String hat = jsonObject.get("hat").getAsString();
		String chest = jsonObject.get("chest").getAsString();
		String trousers = jsonObject.get("trousers").getAsString();
		String boots = jsonObject.get("boots").getAsString();
		return new PlayerData(basicSkin, playerState, hat, chest, trousers, boots);
	}
}
