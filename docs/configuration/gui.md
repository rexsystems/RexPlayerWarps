---
title: Menu
description: Rows, filler, warp slots, and buttons.
sidebar:
  order: 5
---

```yaml
gui:
  rows: 6
  title: "<dark_gray>Warps <gray>({page}/{pages})"
  favorite-glow: true
  clicks:
    teleport: LEFT
    favorite: RIGHT
  filler:
    material: GRAY_STAINED_GLASS_PANE
    name: " "
    lore: []
    custom-model-data: 0
    glow: false
  warp-slots: [10, 11, 12]
  buttons:
    close:
      slot: -1
      material: BARRIER
      name: "<red>Close"
      lore: []
      custom-model-data: 0
      glow: false
```

`rows` is clamped to 1-6, so the inventory is 9, 18, 27, 36, 45, or 54 slots. Slots outside that inventory are ignored.

`filler.material` of `AIR`, or a name that is not an item, skips the filler. `custom-model-data` of 0 does not set a model. `glow: true` adds an enchantment and hides it.

`warp-slots` are where warp icons go, in order, skipping any slot used by a visible button. The page size is how many of those slots are left.

Buttons:

| Id | Default job |
|---|---|
| `previous` / `next` | Change page |
| `sort` | Next key in `sorts` |
| `search` | Close the menu and wait for chat |
| `recent` | Toggle warps you have visited |
| `favorites` | Toggle bookmarked warps |
| `category` | Cycle All, then each category |
| `close` | Close the menu |

`slot: -1` does not place the button. The default close button is hidden that way. Clicks in the player's own inventory are not cancelled, except shift-click, which would otherwise push items into the menu. A click outside the window is not cancelled.

`gui.clicks.teleport` and `gui.clicks.favorite` are Bukkit click names such as `LEFT` and `RIGHT`. Shift-click matches the same button (`SHIFT_LEFT` counts as `LEFT`).

`favorite-glow` enchants a warp icon the viewer has bookmarked.

The search button tells the player to type a warp name or an owner name. That one chat message is cancelled so it is not broadcast. Other chat is left alone. A command clears the waiting state without being cancelled. The word in `search-cancel` clears the filter.
