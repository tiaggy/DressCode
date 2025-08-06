package me.tiaggy.dresscode;

import me.tiaggy.dresscode.commands.ClothesMenu;
import me.tiaggy.dresscode.customitems.Clothes;
import me.tiaggy.dresscode.models.PlayerData;
import me.tiaggy.dresscode.utils.IdentifierUtils;
import me.tiaggy.dresscode.utils.SkinUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Listeners implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		SkinUtils.loadPlayerSkinAsync(event.getPlayer());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		FileConfiguration config = DressCodePlugin.getPlugin().getConfig();
		if (config.get("dropItemsOnDeath") != null && config.getBoolean("dropItemsOnDeath")) {
			Player player = event.getEntity();

			ClothesMenu menu = ClothesMenu.getMenu(player);
			Inventory inventory = menu.getInventory();

			ItemStack hat = inventory.getItem(13);
			if (hat != null) {
				event.getDrops().add(hat);
			}
			ItemStack chest = inventory.getItem(22);
			if (chest != null) {
				event.getDrops().add(chest);
			}
			ItemStack legs = inventory.getItem(31);
			if (legs != null) {
				event.getDrops().add(legs);
			}
			ItemStack boots = inventory.getItem(40);
			if (boots != null) {
				event.getDrops().add(boots);
			}
			ClothesMenu.removeMenu(player);

			PlayerData playerData = DressCodePlugin.getAppearanceData().get(player.getUniqueId());
			playerData.setHat("0");
			playerData.setChest("0");
			playerData.setTrousers("0");
			playerData.setBoots("0");
			DressCodePlugin.getAppearanceData().put(player.getUniqueId(), playerData);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		SkinUtils.loadPlayerSkinAsync(event.getPlayer());
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();

		if (ClothesMenu.hasOpenedMenu(player)) {
			ClothesMenu menu = ClothesMenu.getMenu(player);
			menu.onClose(player);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (isCustomClothesMenu(event)) {
			if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
				ClothesMenu menu = ClothesMenu.getMenu((Player) event.getWhoClicked());
				menu.onClick(event);
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (isCustomClothesMenu(event)) {
			ItemStack cursor = event.getOldCursor();
			if (cursor.getAmount() == 1) {
				switch ((int) (event.getRawSlots().toArray()[0])) {
					case 13:
						String hatName = IdentifierUtils.getStringIdentifier(cursor, Keys.HAT_KEY);
						if (hatName != null && Clothes.getHats().containsKey(hatName)) {
							return;
						} else {
							event.setCancelled(true);
						}
						break;
					case 22:
						String chestName = IdentifierUtils.getStringIdentifier(cursor, Keys.CHEST_KEY);
						if (chestName != null && Clothes.getChests().containsKey(chestName)) {
							return;
						} else {
							event.setCancelled(true);
						}
						break;
					case 31:
						String legsName = IdentifierUtils.getStringIdentifier(cursor, Keys.LEGS_KEY);
						if (legsName != null && Clothes.getLegs().containsKey(legsName)) {
							return;
						} else {
							event.setCancelled(true);
						}
						break;
					case 40:
						String bootsName = IdentifierUtils.getStringIdentifier(cursor, Keys.BOOTS_KEY);
						if (bootsName != null && Clothes.getBoots().containsKey(bootsName)) {
							return;
						} else {
							event.setCancelled(true);
						}
						break;
				}
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryShiftClick(InventoryClickEvent event) {

		if (event.isShiftClick()) {
			if (isCustomClothesMenu(event) && event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
				if (event.getRawSlot() < 54) {
					return;
				}

				ItemStack clickedItem = event.getCurrentItem();
				if (clickedItem == null) {
					return;
				}
				Player player = (Player) event.getWhoClicked();
				ClothesMenu menu = ClothesMenu.getMenu(player);
				String hatName = IdentifierUtils.getStringIdentifier(clickedItem, Keys.HAT_KEY);
				if (hatName != null && Clothes.getHats().containsKey(hatName)) {
					if (menu.getInventory().getItem(13) == null) {
						menu.getInventory().setItem(13, clickedItem);

						event.getClickedInventory().setItem(event.getSlot(), null);
					}
					event.setCancelled(true);
					return;
				}
				String chestName = IdentifierUtils.getStringIdentifier(clickedItem, Keys.CHEST_KEY);
				if (chestName != null && Clothes.getChests().containsKey(chestName)) {
					if (menu.getInventory().getItem(22) == null) {
						menu.getInventory().setItem(22, clickedItem);

						event.getClickedInventory().setItem(event.getSlot(), null);
					}

					event.setCancelled(true);
					return;
				}
				String legsName = IdentifierUtils.getStringIdentifier(clickedItem, Keys.LEGS_KEY);
				if (legsName != null && Clothes.getLegs().containsKey(legsName)) {
					if (menu.getInventory().getItem(31) == null) {
						menu.getInventory().setItem(31, clickedItem);

						event.getClickedInventory().setItem(event.getSlot(), null);
					}
					event.setCancelled(true);
					return;
				}
				String bootsName = IdentifierUtils.getStringIdentifier(clickedItem, Keys.BOOTS_KEY);
				if (bootsName != null && Clothes.getBoots().containsKey(bootsName)) {
					if (menu.getInventory().getItem(40) == null) {
						menu.getInventory().setItem(40, clickedItem);

						event.getClickedInventory().setItem(event.getSlot(), null);
					}
					event.setCancelled(true);
					return;
				}
				event.setCancelled(true);
			}
		}
	}

	private boolean isCustomClothesMenu(InventoryInteractEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (ClothesMenu.hasOpenedMenu(player)) {
			if (event.getView().getTopInventory().getType() == InventoryType.CHEST) {
				event.getInventory();
				return event.getInventory().equals(event.getView().getTopInventory());
			}
		}
		return false;
	}
}
