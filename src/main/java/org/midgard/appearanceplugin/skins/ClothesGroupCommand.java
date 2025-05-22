package org.midgard.appearanceplugin.skins;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.midgard.appearanceplugin.AppearancePlugin;
import org.midgard.appearanceplugin.ClothesMenu;
import org.midgard.appearanceplugin.customitems.Clothes;
import org.midgard.appearanceplugin.utils.SkinUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClothesGroupCommand implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// Clothes menu
		if ((args.length == 0 || args[0].equals("menu"))) {
			if (sender instanceof ConsoleCommandSender) {
				sender.sendMessage("Only players can use this command!");
				return true;
			}
			if (!sender.hasPermission("appearanceplugin.command.menu")) {
				sender.sendMessage("You do not have permission to use this command!");
				return true;
			}

			Player player = (Player) sender;
			ClothesMenu menu = ClothesMenu.getMenu(player);
			menu.onOpen(player);
			return true;
		}

		if (args.length == 1) {
			switch (args[0]) {
				case "reload":
					if (sender.hasPermission("appearanceplugin.command.reload")) {
						AppearancePlugin.getPlugin().reloadConfig();
						Clothes.reload();
						for (Player player : Bukkit.getOnlinePlayers()) {
							if (ClothesMenu.hasOpenedMenu(player)) {
								player.closeInventory();
							}
						}
						ClothesMenu.clearMenus();
						return true;
					} else {
						sender.sendMessage("You do not have permission to use this command!");
						return true;
					}
				case "help":
					sender.sendMessage("Clothes plugin commands:");
					sender.sendMessage("/clothes menu - Opens the clothes menu");
					sender.sendMessage("/clothes reload - Reloads the plugin config");
					sender.sendMessage("/clothes skin set <skin> - Sets your skin to the specified skin");
					sender.sendMessage("/clothes skin reset - Resets your skin to the default skin");
					sender.sendMessage("/clothes skin update - Updates your skin to the latest skin");
					return true;
				case "skin":
					sender.sendMessage("Usage: /clothes skin <set|reset|update>");
					return true;
				case "data":
					sender.sendMessage("Usage: /clothes data <remove> <key-string>");
			}
		}

		if (args.length == 2) {
			if (args[0].equals("skin")) {
				if (!sender.hasPermission("appearanceplugin.command.skin")) {
					sender.sendMessage("You do not have permission to use this command!");
					return true;
				}
				switch (args[1]) {
					case "set":
						sender.sendMessage("Usage: /clothes skin set <skin>");
						return true;
					case "reset":
						if (sender instanceof Player) {
							Player player = (Player) sender;
							SkinUtils.setPlayerSkinAsync(player, "0");
							return true;
						} else {
							sender.sendMessage("Only players can use this command!" +
									" Use /clothes skin set <player> 0 to reset a player's skin from console!");
							return true;
						}
					case "update":
						if (sender instanceof Player) {
							Player player = (Player) sender;
							SkinUtils.updateSkinAsync(player);
							return true;
						} else {
							sender.sendMessage("Only players can use this command!");
							return true;
						}
				}
			} else if (args[0].equals("data")) {
				if (args[1].equals("remove")) {
					sender.sendMessage("Usage: /clothes data remove <key-string>");
					return true;
				}
			}
		}

		if (args.length == 3) {
			if (args[0].equals("skin") && args[1].equals("set")) {
				if (sender.hasPermission("appearanceplugin.command.skin")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						SkinUtils.setPlayerSkinAsync(player, args[2]);
						return true;
					} else {
						sender.sendMessage("Only players can use this command!" +
								" Use /clothes skin set <player> <skin> to set a player's skin from console!");
						return true;
					}
				} else {
					sender.sendMessage("You do not have permission to use this command!");
					return true;
				}
			} else if (args[0].equals("data") && args[1].equals("remove")) {
				if (sender.hasPermission("appearanceplugin.command.data.remove")) {
					SkinUtils.deleteSkinData(args[2]);
					return true;
				} else {
					sender.sendMessage("You do not have permission to use this command!");
					return true;
				}

			}
		}
		if (args.length == 4) {
			if (args[0].equals("skin") && args[1].equals("set")) {
				if (sender.hasPermission("appearanceplugin.command.skin.forceset")) {
					Player player = Bukkit.getPlayer(args[2]);
					if (player == null) {
						sender.sendMessage("Player not found!");
						return true;
					}

					File folder = new File(AppearancePlugin.getPlugin().getDataFolder(), "assets/0 - Basic Skins/custom-skins");
					if (folder.exists()) {
						for (String file : folder.list()) {
							if (file.startsWith(args[3].toLowerCase()) && file.endsWith(".png")) {
								if (file.startsWith(args[3].toLowerCase())) {
									SkinUtils.setPlayerSkinAsync(player, file.replace(".png", ""));
									return true;
								}
							}
						}
					}
					return true;
				} else {
					sender.sendMessage("You do not have permission to use this command!");
					return true;
				}
			}
		}

		sender.sendMessage("Invalid command! Use /clothes help for help.");
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
			command, @NotNull String label, @NotNull String[] args) {
		List<String> suggestions = new ArrayList<>();

		switch (args.length) {
			case 1:
				List<String> subCommands = Arrays.asList("menu", "reload", "skin", "help", "data");
				for (String sub : subCommands) {
					if (sub.startsWith(args[0].toLowerCase())) {
						suggestions.add(sub);
					}
				}
				break;
			case 2:
				if (args[0].toLowerCase().equals("skin")) {
					List<String> skinSubCommands = Arrays.asList("set", "reset", "update");
					for (String sub : skinSubCommands) {
						if (sub.startsWith(args[1].toLowerCase())) {
							suggestions.add(sub);
						}
					}
				} else if (args[0].toLowerCase().equals("data")) {
					if ("remove".startsWith(args[1].toLowerCase())) {
						suggestions.add("remove");
					}
				}
				break;
			case 3:
			case 4:
				if (args[0].toLowerCase().equals("skin") && args[1].toLowerCase().equals("set")) {

					File folder = new File(AppearancePlugin.getPlugin().getDataFolder(), "assets/0 - Basic Skins/custom-skins");
					if (folder.exists()) {
						for (String file : folder.list()) {
							if (file.startsWith(args[2].toLowerCase()) && file.endsWith(".png")) {
								suggestions.add(file.substring(0, file.length() - 4));
							}
						}
					}

				}

		}
		return suggestions;
	}
}
