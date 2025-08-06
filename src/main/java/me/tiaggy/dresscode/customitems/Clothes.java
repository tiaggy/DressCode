package me.tiaggy.dresscode.customitems;

import me.tiaggy.dresscode.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.tiaggy.dresscode.DressCodePlugin.getPlugin;
import static me.tiaggy.dresscode.DressCodePlugin.getPluginLogger;

public final class Clothes {
	private static final HashMap<String, ItemStack> hats = new HashMap<>();
	private static final HashMap<String, ItemStack> chests = new HashMap<>();
	private static final HashMap<String, ItemStack> legs = new HashMap<>();
	private static final HashMap<String, ItemStack> boots = new HashMap<>();

	public static HashMap<String, ItemStack> getHats() {
		return hats;
	}

	public static HashMap<String, ItemStack> getChests() {
		return chests;
	}

	public static HashMap<String, ItemStack> getLegs() {
		return legs;
	}

	public static HashMap<String, ItemStack> getBoots() {
		return boots;
	}

	private static final HashMap<String, NamespacedKey> clothesRecipeKeys = new HashMap<>();

	public static void reload() {
		for (NamespacedKey key : clothesRecipeKeys.values()) {
			Bukkit.removeRecipe(key);
		}
		clothesRecipeKeys.clear();
		hats.clear();
		chests.clear();
		legs.clear();
		boots.clear();
		getPlugin().registerClothesFromConfig();
	}

	public static boolean register(NamespacedKey key, String type, String clothName, String clothFIleName, Map<Integer, Material> craftMap) {
		JavaPlugin plugin = getPlugin();
		ItemStack superBoots = createClothes(key, type, clothName, clothFIleName);
		if (superBoots == null) {

			getPluginLogger().warning("Invalid clothes type: " + type);
			return false;
		}


		if (craftMap != null) {
			NamespacedKey recipeKey = new NamespacedKey(plugin, clothFIleName + "_Recipe");
			clothesRecipeKeys.put(clothFIleName, recipeKey);
			ShapedRecipe recipe = getShapedRecipe(recipeKey, superBoots);
			for (int i = 1; i < 10; i++) {
				if (craftMap.containsKey(i)) {
					recipe.setIngredient((char) ('A' + i - 1), craftMap.get(i));
				}
			}
			Bukkit.addRecipe(recipe);
		}
		return true;
	}

	@NotNull
	private static ShapedRecipe getShapedRecipe(NamespacedKey recipeKey, ItemStack superBoots) {
		ShapedRecipe recipe = new ShapedRecipe(recipeKey, superBoots);
		recipe.shape("ABC",
				"DEF",
				"GHI");
		return recipe;
	}

	private static ItemStack createClothes(NamespacedKey key, String type, String clothName, String clothFIleName) {
		Color color = null;
		if (type.toUpperCase().contains("_LEATHER_")) {
			String[] split = type.split("_");
			color = getColorFromString(split[0]);
			if (color == null) {
				getPluginLogger().warning("Invalid color: " + type.split("_")[1] + " for " + clothName);
			}
			type = "LEATHER_" + split[2];
		}

		Material material = Material.getMaterial(type.toUpperCase());
		if (material == null) {
			getPluginLogger().warning("Invalid material: " + type + " for " + clothName);
			return null;
		}
		ItemStack clothes = new ItemStack(material);

		ItemMeta clothesMeta = clothes.getItemMeta();
		setNamespaceKey(clothesMeta, key, clothFIleName);
		clothesMeta.customName(Component.text(clothName).color(NamedTextColor.GOLD));
		clothesMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		clothesMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		if (!Bukkit.getVersion().contains("1.15")) {
			clothesMeta.addItemFlags(ItemFlag.HIDE_DYE);
		}
		clothesMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
		clothes.setItemMeta(clothesMeta);

		if (color != null) {
			LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) clothesMeta;
			leatherArmorMeta.setColor(color);
			clothes.setItemMeta(leatherArmorMeta);
		}


		if (key.equals(Keys.HAT_KEY)) {
			hats.put(clothFIleName, clothes);
		} else if (key.equals(Keys.CHEST_KEY)) {
			chests.put(clothFIleName, clothes);
		} else if (key.equals(Keys.LEGS_KEY)) {
			legs.put(clothFIleName, clothes);
		} else if (key.equals(Keys.BOOTS_KEY)) {
			boots.put(clothFIleName, clothes);
		}
		return clothes;
	}

	private static void setNamespaceKey(ItemMeta itemMeta, NamespacedKey namespacedKey, String value) {
		itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
	}

	private static org.bukkit.Color getColorFromString(String name) {
		try {
			return (Color) org.bukkit.Color.class.getField(name).get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}
}
