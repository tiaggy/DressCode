package org.midgard.appearanceplugin;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.midgard.appearanceplugin.customitems.Clothes;
import org.midgard.appearanceplugin.playerdata.PlayerData;
import org.midgard.appearanceplugin.utils.SkinUtils;
import org.midgard.appearanceplugin.utils.UniqueIdentifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.midgard.appearanceplugin.AppearancePlugin.appearanceData;
import static org.midgard.appearanceplugin.AppearancePlugin.getPlayerAppearance;

public class ClothesMenu {

	private static HashMap<UUID, ClothesMenu> menus = new HashMap<>();
	private static Set<UUID> openedMenus = new HashSet<>();
	private Inventory inventory;

	public ClothesMenu(Player player) {
		inventory = Bukkit.createInventory(null, 54, "Clothes Menu");
		for (int i = 0; i < 54; i++) {
			if (i > 47 && i < 51) {
				ItemStack skinUpdateButton = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
				ItemMeta meta = skinUpdateButton.getItemMeta();
				meta.setDisplayName(ChatColor.GOLD + "Change Clothes");
				skinUpdateButton.setItemMeta(meta);
				inventory.setItem(i, skinUpdateButton);
				continue;
			}

			if (((i - 5) % 9 == 0 || (i - 3) % 9 == 0) && i / 9 != 0 && i / 9 != 5) {
				if (!(player.hasPermission("appearanceplugin.allclothes") || player.getGameMode().equals(GameMode.CREATIVE))) {
					ItemStack nothing = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
					ItemMeta meta = nothing.getItemMeta();
					meta.setDisplayName(ChatColor.GRAY + "Clothes Menu");
					nothing.setItemMeta(meta);
					inventory.setItem(i, nothing);
					continue;
				}
				ItemStack nextButton = new ItemStack(Material.GLASS_PANE);
				ItemMeta meta = nextButton.getItemMeta();

				if ((i - 5) % 9 == 0) {
					meta.setDisplayName(ChatColor.WHITE + "Next");
				} else {
					meta.setDisplayName(ChatColor.WHITE + "Previous");
				}
				nextButton.setItemMeta(meta);
				inventory.setItem(i, nextButton);
				continue;
			}

			if ((i - 4) % 9 != 0 || i == 4) {
				ItemStack nothing = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
				ItemMeta meta = nothing.getItemMeta();
				meta.setDisplayName(ChatColor.GRAY + "Clothes Menu");
				nothing.setItemMeta(meta);
				inventory.setItem(i, nothing);
			}
		}
		PlayerData playerData = getPlayerAppearance(player);
		ItemStack hat = Clothes.getHats().get(playerData.getHat());
		if (hat != null) {
			inventory.setItem(13, hat);
		}
		ItemStack chest = Clothes.getChests().get(playerData.getChest());
		if (chest != null) {
			inventory.setItem(22, chest);
		}
		ItemStack trousers = Clothes.getLegs().get(playerData.getTrousers());
		if (trousers != null) {
			inventory.setItem(31, trousers);
		}
		ItemStack boots = Clothes.getBoots().get(playerData.getBoots());
		if (boots != null) {
			inventory.setItem(40, boots);
		}
		menus.put(player.getUniqueId(), this);
	}

	public static void clearMenus() {
		menus.clear();
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void onOpen(Player player) {
		if (hasOpenedMenu(player)) {
			player.sendMessage(ChatColor.YELLOW + "Please wait! Changing clothes...");
			return;
		}
		ItemStack prevButton = inventory.getItem(12);
		boolean hasPremiumMenu = prevButton.getType().equals(Material.GLASS_PANE);
		if (player.hasPermission("appearanceplugin.allclothes") || player.getGameMode().equals(GameMode.CREATIVE)) {
			if (!hasPremiumMenu) {
				removeMenu(player);
				ClothesMenu menu = new ClothesMenu(player);
				menu.onOpen(player);
				return;
			}
		} else if (hasPremiumMenu) {
			removeMenu(player);
			ClothesMenu menu = new ClothesMenu(player);
			menu.onOpen(player);
			return;
		}
		openedMenus.add(player.getUniqueId());
		player.openInventory(this.getInventory());
	}

	public void onClose(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean appearanceChanged = false;
				PlayerData playerData = getPlayerAppearance(player);
				ItemStack hat = inventory.getItem(13);
				ItemStack chest = inventory.getItem(22);
				ItemStack trousers = inventory.getItem(31);
				ItemStack boots = inventory.getItem(40);
				String hatName = UniqueIdentifier.getUniqueStringIdentifier(hat, Keys.HAT_KEY);
				if (hatName == null) {
					hatName = "0";
				}
				String chestName = UniqueIdentifier.getUniqueStringIdentifier(chest, Keys.CHEST_KEY);
				if (chestName == null) {
					chestName = "0";
				}
				String trousersName = UniqueIdentifier.getUniqueStringIdentifier(trousers, Keys.LEGS_KEY);
				if (trousersName == null) {
					trousersName = "0";
				}
				String bootsName = UniqueIdentifier.getUniqueStringIdentifier(boots, Keys.BOOTS_KEY);
				if (bootsName == null) {
					bootsName = "0";
				}

				if (!hatName.equals(playerData.getHat())) {
					playerData.setHat(hatName);
					appearanceChanged = true;
				}
				if (!chestName.equals(playerData.getChest())) {
					playerData.setChest(chestName);
					appearanceChanged = true;
				}
				if (!trousersName.equals(playerData.getTrousers())) {
					playerData.setTrousers(trousersName);
					appearanceChanged = true;
				}
				if (!bootsName.equals(playerData.getBoots())) {
					playerData.setBoots(bootsName);
					appearanceChanged = true;
				}
				if (appearanceChanged) {
					appearanceData.put(player.getUniqueId(), playerData);
					player.sendActionBar(ChatColor.YELLOW + "Changing Clothes...");
					SkinUtils.loadPlayerSkinAsync(player);
				}
				removeOpenedMenu(player);
			}
		}.runTaskAsynchronously(AppearancePlugin.getPlugin());
	}

	public void onClick(InventoryClickEvent event) {
		ItemStack clickedItem = event.getCurrentItem();

		if (clickedItem != null && clickedItem.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) {
			event.setCancelled(true);
			return;
		} else if (clickedItem != null && clickedItem.getType().equals(Material.GLASS_PANE)) {
			Inventory inventory = event.getClickedInventory();
			switch (event.getSlot()) {
				case 12:
				case 14:
					ItemStack currentHat = inventory.getItem(13);
					String hatName = UniqueIdentifier.getUniqueStringIdentifier(currentHat, Keys.HAT_KEY);
					handleClothesChange(inventory, Clothes.getHats(), hatName, event.getSlot(), 13);
					break;
				case 21:
				case 23:
					ItemStack currentChest = inventory.getItem(22);
					String chestName = UniqueIdentifier.getUniqueStringIdentifier(currentChest, Keys.CHEST_KEY);
					handleClothesChange(inventory, Clothes.getChests(), chestName, event.getSlot(), 22);
					break;
				case 30:
				case 32:
					ItemStack currentTrousers = inventory.getItem(31);
					String trousersName = UniqueIdentifier.getUniqueStringIdentifier(currentTrousers, Keys.LEGS_KEY);
					handleClothesChange(inventory, Clothes.getLegs(), trousersName, event.getSlot(), 31);
					break;
				case 39:
				case 41:
					ItemStack currentBoots = inventory.getItem(40);
					String bootsName = UniqueIdentifier.getUniqueStringIdentifier(currentBoots, Keys.BOOTS_KEY);
					handleClothesChange(inventory, Clothes.getBoots(), bootsName, event.getSlot(), 40);
					break;

			}
			event.setCancelled(true);

		} else {
			switch (event.getSlot()) {
				case 13:
					if (handleClothesSlotClickEvent(event, Clothes.getHats(), Keys.HAT_KEY)) {
						return;
					}
					break;
				case 22:
					if (handleClothesSlotClickEvent(event, Clothes.getChests(), Keys.CHEST_KEY)) {
						return;
					}
					break;
				case 31:
					if (handleClothesSlotClickEvent(event, Clothes.getLegs(), Keys.LEGS_KEY)) {
						return;
					}
					break;
				case 40:
					if (handleClothesSlotClickEvent(event, Clothes.getBoots(), Keys.BOOTS_KEY)) {
						return;
					}
					break;
				case 48:
				case 49:
				case 50:
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
			}
			event.setCancelled(true);
		}
	}


	private static void handleClothesChange(Inventory inventory, HashMap<String, ItemStack> clothes, String clothName, int slot, int currentSlot) {
		if (slot == currentSlot - 1) {
			ItemStack prevClothes = getPrevItem(clothes, clothName);
			if (prevClothes != null) {
				inventory.setItem(currentSlot, prevClothes);
			} else {
				inventory.setItem(currentSlot, new ItemStack(Material.AIR));
			}
		} else if (slot == currentSlot + 1) {
			ItemStack nextClothes = getNextItem(clothes, clothName);
			if (nextClothes != null) {
				inventory.setItem(currentSlot, nextClothes);
			} else {
				inventory.setItem(currentSlot, new ItemStack(Material.AIR));
			}
		}
	}

	private static boolean handleClothesSlotClickEvent(InventoryClickEvent event, HashMap<String, ItemStack> clothes, NamespacedKey key) {
		ItemStack clickedItem = event.getCursor();

		if (clickedItem == null || clickedItem.getType().equals(Material.AIR)) {
			return true;
		} else {
			String clickedItemName = UniqueIdentifier.getUniqueStringIdentifier(clickedItem, key);
			if (clickedItem == null || !clothes.containsKey(clickedItemName)) {
				return false;
			}
		}
		return true;
	}

	private static ItemStack getNextItem(HashMap<String, ItemStack> clothes, String currentCloth) {
		if (currentCloth == null) {
			if (clothes.size() > 0) {
				return clothes.get(clothes.keySet().toArray()[0]);
			} else {
				return null;
			}
		}

		boolean found = false;
		for (String cloth : clothes.keySet()) {
			if (found) {
				return clothes.get(cloth);
			}
			if (cloth.equals(currentCloth)) {
				found = true;
			}
		}
		return null;
	}

	private static ItemStack getPrevItem(HashMap<String, ItemStack> clothes, String currentCloth) {
		if (currentCloth == null) {
			if (clothes.size() > 0) {
				return clothes.get(clothes.keySet().toArray()[clothes.size() - 1]);
			} else {
				return null;
			}
		}
		ItemStack prevCloth = null;
		for (String cloth : clothes.keySet()) {
			if (cloth.equals(currentCloth)) {
				if (prevCloth != null) {
					return prevCloth;
				} else {
					return null;
				}
			}
			prevCloth = clothes.get(cloth);
		}
		return null;
	}

	public static ClothesMenu getMenu(Player player) {
		if (menus.containsKey(player.getUniqueId())) {
			return menus.get(player.getUniqueId());
		}
		ClothesMenu menu = new ClothesMenu(player);
		menus.put(player.getUniqueId(), menu);
		return menu;
	}

	public static boolean hasOpenedMenu(Player player) {
		return openedMenus.contains(player.getUniqueId());
	}

	public static void removeMenu(Player player) {
		menus.remove(player.getUniqueId());
	}

	public static void removeOpenedMenu(Player player) {
		openedMenus.remove(player.getUniqueId());
	}
}