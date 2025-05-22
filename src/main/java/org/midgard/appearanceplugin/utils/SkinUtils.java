package org.midgard.appearanceplugin.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.midgard.appearanceplugin.AppearancePlugin;
import org.midgard.appearanceplugin.playerdata.PlayerData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.midgard.appearanceplugin.AppearancePlugin.getPlayerAppearance;

public class SkinUtils {
	private final static String pluginFolder = AppearancePlugin.getPlugin().getDataFolder().getAbsolutePath();
	private final static String assetsFolder = pluginFolder + "/assets/";
	private final static String tempFolder = assetsFolder + "temp/";
	private final static String skinDataFolder = assetsFolder + "skin-data/";
	private final static String playerSkinFolder = assetsFolder + "0 - Basic Skins/skins/";
	private final static String customSkinFolder = assetsFolder + "0 - Basic Skins/custom-skins/";
	private final static String hatFolder = assetsFolder + "1 - Hats/";
	private final static String chestFolder = assetsFolder + "2 - Chestplates/";
	private final static String trousersFolder = assetsFolder + "3 - Trousers/";
	private final static String bootsFolder = assetsFolder + "4 - Boots/";


	public static void setPlayerSkinAsync(Player player, String newSkin) {
		new BukkitRunnable() {
			@Override
			public void run() {
				PlayerData playerData = getPlayerAppearance(player);
				playerData.setBasicSkin(newSkin);
				AppearancePlugin.getAppearanceData().put(player.getUniqueId(), playerData);
				try {
					updateSkin(player, playerData);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}.runTaskAsynchronously(AppearancePlugin.getPlugin());
	}

	public static void loadPlayerSkinAsync(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				PlayerData playerData = getPlayerAppearance(player);
				try {
					updateSkin(player, playerData);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}.runTaskAsynchronously(AppearancePlugin.getPlugin());
	}

	public static void updateSkinAsync(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {

				deleteSkinData(player.getName());
				PlayerData playerData = getPlayerAppearance(player);
				if (playerData.getBasicSkin().equals("0")) {
					File originalSkin = new File(playerSkinFolder + player.getName() + "_original.png");
					File skin = new File(playerSkinFolder + player.getName() + ".png");

					if (originalSkin.exists()) {
						originalSkin.delete();
					}
					if (skin.exists()) {
						skin.delete();
					}
				}

				try {
					updateSkin(player, playerData);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}.runTaskAsynchronously(AppearancePlugin.getPlugin());
	}

	public static void deleteSkinData(String keyString) {
		int filesDeleted = 0;
		File folder = new File(skinDataFolder);
		File[] files = folder.listFiles((dir, name) -> name.contains(keyString));
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					boolean deleted = file.delete();
					if (!deleted) {
						Bukkit.getLogger().warning("Failed to delete: " + file.getName());
					} else {
						filesDeleted++;
					}
				}
			}
		}
		Bukkit.getLogger().info("Deleted " + filesDeleted + " skin data files for: " + keyString);
	}

	private static void downloadDefaultSkin(Player player) throws IOException, InterruptedException {
		String skinFilePath = playerSkinFolder + player.getName();

		File skin = new File(skinFilePath + "_original.png");
		File mergeSkin = new File(skinFilePath + ".png");
		if (skin.exists() && mergeSkin.exists()) {
			return;
		}
		String skinUrl;
		if (Bukkit.getOnlineMode()) {
			skinUrl = getSkinUrl(player.getUniqueId().toString());
		} else {
			skinUrl = getSkinUrl(getUUID(player.getName()));
		}

		if (skinUrl != null) {
			File playerSkinFolder = new File(skinFilePath);
			if (!playerSkinFolder.exists()) {
				playerSkinFolder.mkdirs();
			}

			URL url = new URL(skinUrl);
			InputStream inputStream = url.openStream();


			Path pathToOriginal = Paths.get(skinFilePath + "_original.png");
			Files.deleteIfExists(pathToOriginal);
			Files.copy(inputStream, pathToOriginal);
			inputStream.close();

			Path pathToMergeSkin = Paths.get(skinFilePath + ".png");
			Files.deleteIfExists(pathToMergeSkin);
			Files.copy(pathToOriginal, pathToMergeSkin);
			//removing 2nd layer of skin
			BufferedImage basicSkin = ImageIO.read(new File(skinFilePath + ".png"));
			setAreaTransparent(basicSkin, 0, 32, 56, 48);
			setAreaTransparent(basicSkin, 48, 48, 64, 64);
			setAreaTransparent(basicSkin, 0, 48, 16, 64);
			ImageIO.write(basicSkin, "PNG", new File(skinFilePath + ".png"));

		}
	}

	private static void updateSkin(Player player, PlayerData playerData) throws IOException, InterruptedException {
		String skinDataFileName = SkinUtils.getSkinDataFileName(playerData, player.getName());
		String skinFileName = SkinUtils.getSkinFileName(playerData);
		String[] defaultSkin;
		try {
			defaultSkin = getSkinData(skinDataFolder + skinDataFileName + ".json");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (defaultSkin == null) {
			//Check if player has its skin downloaded
			if (playerData.getBasicSkin().equals("0")) {
				try {
					downloadDefaultSkin(player);
				} catch (IOException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			//Generate PNG skin of current player appearance
			try {
				generatePngSkin(skinFileName, player.getName());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			//Get signed texture of a skin from Mojang
			JsonObject texture = null;

			while (texture == null) {
				try {
					texture = generateSkinData(skinFileName);
				} catch (IOException e) {
					Bukkit.getLogger().warning("Failed to get skin data for: " + skinFileName);
				}
			}
			saveSkinData(skinDataFileName, texture);

			String pngFilePath = tempFolder + skinFileName + ".png";
			File pngFile = new File(pngFilePath);
			if (pngFile.exists()) {
				pngFile.delete();
			}

			if (!playerData.isDifferentTo(getPlayerAppearance(player))) {
				try {
					updateSkin(player, playerData);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

		} else {
			PlayerProfile playerProfile = player.getPlayerProfile();
			playerProfile.setProperty(new ProfileProperty("textures", defaultSkin[0], defaultSkin[1]));
			new BukkitRunnable() {
				@Override
				public void run() {
					player.setPlayerProfile(playerProfile);
				}
			}.runTask(AppearancePlugin.getPlugin());

		}

	}

	private static String getSkinUrl(String uuid) throws IOException {
		URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		if (connection.getResponseCode() == 200) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(response.toString()).getAsJsonObject();

			JsonArray properties = json.getAsJsonArray("properties");
			for (JsonElement element : properties) {
				JsonObject property = element.getAsJsonObject();
				if (property.get("name").getAsString().equals("textures")) {
					String value = property.get("value").getAsString();
					String decodedValue = new String(Base64.getDecoder().decode(value));
					JsonObject textureJson = parser.parse(decodedValue).getAsJsonObject();
					return textureJson.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
				}
			}
		}
		return null;
	}

	private static String getUUID(String username) throws IOException {
		URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		if (connection.getResponseCode() == 200) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();

			JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
			return json.get("id").getAsString();
		}

		return null;
	}

	private static final String DEFAULT_BOTTOM_REGEX = AppearancePlugin.getPlugin().getConfig().getString("nsfw-censorship.bottom_default_keyword");
	private static final String DEFAULT_TOP_REGEX = AppearancePlugin.getPlugin().getConfig().getString("nsfw-censorship.top_default_keyword");

	private static void generatePngSkin(String fileName, String playerName) throws IOException {

		String filepath = tempFolder + fileName + ".png";
		File f = new File(filepath);
		if (!f.exists()) {
			String[] skinParts = fileName.split("#");
			String basicSkin = skinParts[0];
			//String playerState = skinParts[1];
			String feet = skinParts[2];
			String legs = skinParts[3];
			String body = skinParts[4];
			String head = skinParts[5];
			if (legs.equals("0") && DEFAULT_BOTTOM_REGEX != null) {
				Pattern pattern = Pattern.compile("\\!" + DEFAULT_BOTTOM_REGEX + "(.*?)\\!");
				Matcher matcher = pattern.matcher(basicSkin);

				if (matcher.find()) {
					String result = matcher.group(1);
					legs = result;
				}
			}
			if (body.equals("0") && DEFAULT_TOP_REGEX != null) {
				Pattern pattern = Pattern.compile("\\!" + DEFAULT_TOP_REGEX + "(.*?)\\!");
				Matcher matcher = pattern.matcher(basicSkin);

				if (matcher.find()) {
					String result = matcher.group(1);
					body = result;
				}
			}

			BufferedImage mergedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
			Graphics newskin = mergedImage.getGraphics();

			BufferedImage readBase;
			if (customSkinExists(basicSkin)) {
				readBase = ImageIO.read(new File(customSkinFolder + basicSkin + ".png"));

				if (fileName.contains("#0#0#0#0#0") && !basicSkin.contains(DEFAULT_TOP_REGEX) && !basicSkin.contains(DEFAULT_BOTTOM_REGEX)) {
					newskin.drawImage(readBase, 0, 0, null);
					newskin.dispose();
					ImageIO.write(mergedImage, "PNG", new File(filepath));
					return;
				}

			} else {
				if (fileName.contains("#0#0#0#0#0")) {
					readBase = ImageIO.read(new File(playerSkinFolder + playerName + "_original.png"));
					newskin.drawImage(readBase, 0, 0, null);
					newskin.dispose();
					ImageIO.write(mergedImage, "PNG", new File(filepath));
					return;
				} else {
					readBase = ImageIO.read(new File(playerSkinFolder + playerName + ".png"));
				}
			}
			newskin.drawImage(readBase, 0, 0, null);


//			if (!playerState.equals("0")) {
//				drawSkinOnSkin(newskin, playerStateFolder + playerState + ".png");
//			}
			if (!legs.equals("0")) {
				drawSkinOnSkin(newskin, trousersFolder + legs + ".png");
			}
			if (!feet.equals("0")) {
				drawSkinOnSkin(newskin, bootsFolder + feet + ".png");
			}
			if (!body.equals("0")) {
				drawSkinOnSkin(newskin, chestFolder + body + ".png");
			}
			if (!head.equals("0")) {
				drawSkinOnSkin(newskin, hatFolder + head + ".png");
			}
			newskin.dispose();
			ImageIO.write(mergedImage, "PNG", new File(filepath));
		}
	}

	private static boolean customSkinExists(String skinName) {
		if (skinName.equals("0")) {
			return false;
		}
		File customSkinFile = new File(customSkinFolder + skinName + ".png");
		return customSkinFile.exists();
	}

	private static String getSkinDataFileName(PlayerData playerData, String playerName) {
		String basicSkin = playerData.getBasicSkin();
		if (!customSkinExists(basicSkin)) {
			basicSkin = playerName;
		}
		return basicSkin + "#" + getPlayerClothesFileNamePart(playerData);
	}

	public static String getSkinFileName(PlayerData playerData) {
		return playerData.getBasicSkin() + getPlayerClothesFileNamePart(playerData);
	}

	private static String getPlayerClothesFileNamePart(PlayerData playerData) {
		String playerState = playerData.getPlayerState();
		String hat = playerData.getHat();
		String chest = playerData.getChest();
		String trousers = playerData.getTrousers();
		String boots = playerData.getBoots();
		return "#" + playerState + "#" + boots + "#" + trousers + "#" + chest + "#" + hat;
	}

	private static void setAreaTransparent(BufferedImage image, int x1, int y1, int x2, int y2) {
		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
					image.setRGB(x, y, 0x00000000);
				}
			}
		}
	}

	private static void drawSkinOnSkin(Graphics newskin, String skinPath) throws IOException {
		BufferedImage readSkin = ImageIO.read(new File(skinPath));
		newskin.drawImage(readSkin, 0, 0, null);
	}

	private static String[] getSkinData(String filePath) throws IOException {
		File skinFile = new File(filePath);
		if (skinFile.exists()) {
			FileReader reader = new FileReader(skinFile);

			JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();

			reader.close();
			return new String[]{jsonObject.get("value").getAsString(), jsonObject.get("signature").getAsString()};
		}
		return null;
	}

	private static JsonObject generateSkinData(String skinFileName) throws IOException {
		File skinFile = new File(tempFolder + skinFileName + ".png");
		if (!skinFile.exists()) {
			return null;
		}

		String url = "https://api.mineskin.org/generate/upload";
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", skinFile);

		HttpEntity entity = builder.build();
		post.setEntity(entity);

		CloseableHttpResponse response = client.execute(post);

		String responseString = EntityUtils.toString(response.getEntity());
		JsonObject jsonResponse = new JsonParser().parse(responseString).getAsJsonObject();

		JsonObject data = jsonResponse.getAsJsonObject("data");
		if (data == null) {
			return null;
		}
		JsonObject textureData = data.getAsJsonObject("texture");

		client.close();
		return textureData;
	}

	private static void saveSkinData(String fileName, JsonObject skinData) {
		if (skinData == null) {
			return;
		}
		try (FileWriter fileWriter = new FileWriter(skinDataFolder + fileName + ".json")) {
			fileWriter.write(skinData.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
