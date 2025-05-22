package org.midgard.appearanceplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class Keys {

	//Clothing Keys
	public static NamespacedKey HAT_KEY;
	public static NamespacedKey CHEST_KEY;
	public static NamespacedKey LEGS_KEY;
	public static NamespacedKey BOOTS_KEY;

	public static void init() {
		JavaPlugin plugin = AppearancePlugin.getPlugin();
		HAT_KEY = new NamespacedKey(plugin, "hat_id");
		CHEST_KEY = new NamespacedKey(plugin, "chest_id");
		LEGS_KEY = new NamespacedKey(plugin, "legs_id");
		BOOTS_KEY = new NamespacedKey(plugin, "boots_id");
	}
}
