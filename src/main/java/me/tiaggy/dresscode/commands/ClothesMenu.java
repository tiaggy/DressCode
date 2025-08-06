package me.tiaggy.dresscode.commands;

import lombok.Getter;
import me.tiaggy.dresscode.DressCodePlugin;
import me.tiaggy.dresscode.Keys;
import me.tiaggy.dresscode.customitems.Clothes;
import me.tiaggy.dresscode.models.PlayerData;
import me.tiaggy.dresscode.utils.IdentifierUtils;
import me.tiaggy.dresscode.utils.SkinUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class ClothesMenu {

	private static final HashMap<UUID, ClothesMenu> menus = new HashMap<>();
	private static final Set<UUID> openedMenus = new HashSet<>();
	private final Inventory inventory;

	public ClothesMenu(Player player) {
		inventory = Bukkit.createInventory(null, 54, Component.text("Clothes Menu"));
		for (int i = 0; i < 54; i++) {
			if (i > 47 && i < 51) {
				ItemStack skinUpdateButton = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
				ItemMeta meta = skinUpdateButton.getItemMeta();
				meta.customName(Component.text("Change Clothes").color(NamedTextColor.GOLD));
				skinUpdateButton.setItemMeta(meta);
				inventory.setItem(i, skinUpdateButton);
				continue;
			}

			if (((i - 5) % 9 == 0 || (i - 3) % 9 == 0) && i / 9 != 0 && i / 9 != 5) {
				if (!(player.hasPermission("dresscode.allclothes") || player.getGameMode().equals(GameMode.CREATIVE))) {
					ItemStack nothing = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
					ItemMeta meta = nothing.getItemMeta();
					meta.customName(Component.text("Clothes Menu").color(NamedTextColor.GRAY));
					nothing.setItemMeta(meta);
					inventory.setItem(i, nothing);
					continue;
				}
				ItemStack nextButton = new ItemStack(Material.GLASS_PANE);
				ItemMeta meta = nextButton.getItemMeta();

				if ((i - 5) % 9 == 0) {
					meta.customName(Component.text("Next").color(NamedTextColor.WHITE));
				} else {
					meta.customName(Component.text("Previous").color(NamedTextColor.WHITE));
				}
				nextButton.setItemMeta(meta);
				inventory.setItem(i, nextButton);
				continue;
			}

			if ((i - 4) % 9 != 0 || i == 4) {
				ItemStack nothing = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
				ItemMeta meta = nothing.getItemMeta();
				meta.customName(Component.text("Clothes Menu").color(NamedTextColor.GRAY));
				nothing.setItemMeta(meta);
				inventory.setItem(i, nothing);
			}
		}
		PlayerData playerData = DressCodePlugin.getPlayerAppearance(player);
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

	public void onOpen(Player player) {
		if (hasOpenedMenu(player)) {
			player.sendMessage(Component.text("Please wait! Changing clothes...").color(NamedTextColor.YELLOW));
			return;
		}
		ItemStack prevButton = inventory.getItem(12);
		if (prevButton == null || !prevButton.hasItemMeta() || !prevButton.getItemMeta().hasDisplayName()) {
			player.sendMessage(Component.text("An error occurred while opening the menu. Please try again.").color(NamedTextColor.RED));
			return;
		}

		boolean hasPremiumMenu = prevButton.getType().equals(Material.GLASS_PANE);
		if (player.hasPermission("dresscode.allclothes") || player.getGameMode().equals(GameMode.CREATIVE)) {
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
				PlayerData playerData = DressCodePlugin.getPlayerAppearance(player);
				ItemStack hat = inventory.getItem(13);
				ItemStack chest = inventory.getItem(22);
				ItemStack trousers = inventory.getItem(31);
				ItemStack boots = inventory.getItem(40);
				String hatName = IdentifierUtils.getStringIdentifier(hat, Keys.HAT_KEY);
				if (hatName == null) {
					hatName = "0";
				}
				String chestName = IdentifierUtils.getStringIdentifier(chest, Keys.CHEST_KEY);
				if (chestName == null) {
					chestName = "0";
				}
				String trousersName = IdentifierUtils.getStringIdentifier(trousers, Keys.LEGS_KEY);
				if (trousersName == null) {
					trousersName = "0";
				}
				String bootsName = IdentifierUtils.getStringIdentifier(boots, Keys.BOOTS_KEY);
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
					DressCodePlugin.getAppearanceData().put(player.getUniqueId(), playerData);
					player.sendActionBar(Component.text("Changing Clothes...").color(NamedTextColor.YELLOW));
					SkinUtils.loadPlayerSkinAsync(player);
				}
				removeOpenedMenu(player);
			}
		}.runTaskAsynchronously(DressCodePlugin.getPlugin());
	}

	public void onClick(InventoryClickEvent event) {
		ItemStack clickedItem = event.getCurrentItem();

		if (clickedItem != null && clickedItem.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) {
			event.setCancelled(true);
			return;
		}
		if (clickedItem != null && clickedItem.getType().equals(Material.GLASS_PANE)) {
			Inventory inventory = event.getClickedInventory();
			if (inventory == null || !inventory.equals(this.getInventory())) {
				return;
			}
			switch (event.getSlot()) {
				case 12:
				case 14:
					ItemStack currentHat = inventory.getItem(13);
					String hatName = IdentifierUtils.getStringIdentifier(currentHat, Keys.HAT_KEY);
					handleClothesChange(inventory, Clothes.getHats(), hatName, event.getSlot(), 13);
					break;
				case 21:
				case 23:
					ItemStack currentChest = inventory.getItem(22);
					String chestName = IdentifierUtils.getStringIdentifier(currentChest, Keys.CHEST_KEY);
					handleClothesChange(inventory, Clothes.getChests(), chestName, event.getSlot(), 22);
					break;
				case 30:
				case 32:
					ItemStack currentTrousers = inventory.getItem(31);
					String trousersName = IdentifierUtils.getStringIdentifier(currentTrousers, Keys.LEGS_KEY);
					handleClothesChange(inventory, Clothes.getLegs(), trousersName, event.getSlot(), 31);
					break;
				case 39:
				case 41:
					ItemStack currentBoots = inventory.getItem(40);
					String bootsName = IdentifierUtils.getStringIdentifier(currentBoots, Keys.BOOTS_KEY);
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

		if (clickedItem.getType().equals(Material.AIR)) {
			return true;
		} else {
			String clickedItemName = IdentifierUtils.getStringIdentifier(clickedItem, key);
			return clothes.containsKey(clickedItemName);
		}
	}

	private static ItemStack getNextItem(HashMap<String, ItemStack> clothes, String currentCloth) {
		if (currentCloth == null) {
			if (!clothes.isEmpty()) {
				return clothes.values().stream().findFirst().get();
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
			if (!clothes.isEmpty()) {
				return (ItemStack) clothes.values().toArray()[clothes.size() - 1];
			} else {
				return null;
			}
		}
		ItemStack prevCloth = null;
		for (String cloth : clothes.keySet()) {
			if (cloth.equals(currentCloth)) {
				return prevCloth;
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