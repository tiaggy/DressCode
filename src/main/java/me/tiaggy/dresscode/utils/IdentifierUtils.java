package me.tiaggy.dresscode.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class IdentifierUtils {

	public static String getStringIdentifier(ItemStack item, NamespacedKey key) {
		if (item == null) {
			return null;
		}

		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			PersistentDataContainer data = meta.getPersistentDataContainer();
			if (data.has(key, PersistentDataType.STRING)) {
				return data.get(key, PersistentDataType.STRING);
			}
		}
		return null;
	}
}
