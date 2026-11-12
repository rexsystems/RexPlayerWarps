---
title: Categories
description: Edit categories.yml without losing your list on reload.
sidebar:
  order: 2
---

`categories.yml` is created beside `config.yml` the first time the plugin starts, and again if you delete the file and reload. RexPlayerWarps does not overwrite it and does not merge new keys into it.

```yaml
categories:
  general:
    display: "<white>General"
    icon: COMPASS
  shops:
    display: "<gold>Shops"
    icon: EMERALD
```

The key (`general`, `shops`) is the id used by `/rexplayerwarps set <warp> category <id>` and by `defaults.category`. Ids are read in lowercase.

`display` is MiniMessage. The menu's category button shows the plain text from that display so the button's own colors stay in `config.yml`. `icon` is a Bukkit material. When a category is selected, the category button uses that material.

The category button cycles All, then each id in file order. Slot `-1` on `gui.buttons.category` hides the button. Warps can still store a category.

If `defaults.category` is not in the file, new warps use the first category in the file. If the file has no categories, the configured id is stored anyway and you can fix the file later.

Reload re-reads the file. It does not reset an open teleport.
