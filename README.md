# 🧥 Appearance Plugin

**Appearance Plugin** is a Minecraft plugin that allows players to customize their character’s look by equipping various clothing items through an in-game clothes menu. The plugin changes player skins dynamically by layering cosmetic items like hats, armor, cloaks, and more, creating a fully personalized visual experience.

---

## ✨ Features

* 🎨 **Dynamic Appearance System**
  Players can equip or remove clothing items that visually change their skin and outfit in real-time.

* 🧰 **Clothes Menu GUI**
  A user-friendly inventory-style menu where players can equip clothing items.

* 🧥 **Clothing Item System**
  Add new clothes easily through configuration.

* 🧍 **Layered Skin Rendering**
  Clothing is visually layered over the player’s base skin without affecting gameplay.

---

## 📦 Installation

1. Download the latest `.jar` release.
2. Place it into your server’s `/plugins` directory.
3. Restart or reload the server.

---

## 🧭 Usage

1. Use `/clothes` to open the Clothes Menu.
2. Click items to equip or unequip them.
3. Your appearance updates instantly across the server!

---

## ⚙️ Configuration

The `config.yml` allows you to:

* Define new clothing items and their textures.
* Set categories (e.g., hats, shirts, shoes).
* Link items to permission nodes.
* Adjust GUI layout and item slots.

Example:

```yaml
dropItemsOnDeath: true
nsfw-censorship:
  # default clothes for the player if they don't have any (must be at the end of the file)
  top_default_keyword: "td="
  bottom_default_keyword: "bd="
defaultClothes:
  hat: crown
  chestplate: gr_jacket
  trousers: cargo
  boots: kraduli
hats:
  - name: Crown
    file_name: crown
    material: GOLDEN_HELMET
    craft:
      2: GOLD_INGOT
      4: GOLD_INGOT
      6: GOLD_INGOT
      8: GOLD_INGOT
chestplates:
  - name: Green Jacket
    file_name: gr_jacket
    material: GREEN_LEATHER_CHESTPLATE
    craft:
      1: LEATHER
      3: LEATHER
      4: LEATHER
      5: GREEN_DYE
      6: LEATHER
      7: LEATHER
      8: LEATHER
      9: LEATHER
trousers:
  - name: Cargo Pants
    file_name: cargo
    material: GREEN_LEATHER_LEGGINGS
    craft:
      1: LEATHER
      2: LEATHER
      3: LEATHER
      4: LEATHER
      5: GREEN_DYE
      6: LEATHER
      7: LEATHER
      9: LEATHER
boots:
  - name: Fancy Boots
    file_name: kraduli
    material: GOLDEN_BOOTS
    craft:
      4: LEATHER
      6: LEATHER
      7: STRING
      8: STRING
```

---

## 🧪 Adding New Clothes

1. Move the texture to the responding assets folder in `plugins/AppearancePlugin`.
2. Add the item to `config.yml` with its texture URL.
3. Reload the plugin with `/clothes reload`.

---

## 🛠 Commands

| Command              | Description                  |
| -------------------- | ---------------------------- |
| `/clothes`           | Opens the clothes menu       |

---

Created by Tiaggy.

For suggestions or bug reports, please open an issue or contact me directly.
