---
title: Warps
description: Create a warp, visit one, and collect the bank.
sidebar:
  order: 1
---

## Create

Stand where the warp should send people and run:

```
/rexplayerwarps create market
```

The name may use letters, numbers, `_`, and `-`, up to `limits.name-length` (the plugin also caps names at 32). The warp starts with `defaults.description`, `defaults.icon`, `defaults.category`, `defaults.price`, and `defaults.access`.

You need `rexplayerwarps.create` and a limit node. The limit is the highest `rexplayerwarps.warps.<n>` you have. `rexplayerwarps.warps.3` and `rexplayerwarps.warps.10` together allow 10 warps. With no number node, the limit is 0.

The world must pass `worlds.whitelist` and `worlds.blacklist`. An empty whitelist means every world. A blacklisted world is blocked even if it is also on the whitelist.

## Change it

```
/rexplayerwarps set market description Fresh bread and tools
/rexplayerwarps set market icon BREAD
/rexplayerwarps set market category shops
/rexplayerwarps set market price 50
/rexplayerwarps set market access whitelist
```

`description`, `icon`, `category`, `price`, and `access` are the fields. Category ids come from `categories.yml`. Access is `public`, `private`, or `whitelist`. Price has to sit between `limits.price-min` and `limits.price-max`, and it cannot be negative. Descriptions longer than `limits.description-length` are rejected. The length in config is capped at 512.

`/rexplayerwarps set market description clear` stores an empty description when `description-clear` is `clear`.

## Who can visit

| Access | Who can open it |
|---|---|
| `public` | Anyone with `rexplayerwarps.use` |
| `private` | The owner, and players with `rexplayerwarps.admin` |
| `whitelist` | The owner, admins, and listed players |

```
/rexplayerwarps whitelist market add Steve
/rexplayerwarps whitelist market remove Steve
/rexplayerwarps whitelist market list
```

If Steve is online, the whitelist stores his id and name. If he is offline, it stores the name and matches that name when he visits.

## Visit

`/rexplayerwarps open` opens the menu. `/rexplayerwarps open market` opens it with that search already applied. Search matches the warp name or the owner name.

Left-click a warp to start a visit (`gui.clicks.teleport`). Right-click bookmarks it (`gui.clicks.favorite`). `/rexplayerwarps favorite market` does the same thing from chat.

If the price is above 0 and `teleport.price-confirm` is true, the first click only warns you. Click the same warp again before `teleport.confirm-seconds` runs out. The same second click is required when the warp fails the unsafe check and `unsafe.confirm` is true. Unsafe means the block at the feet, or the block under it, is one of `unsafe.blocks`, or the feet are below the world minimum when `unsafe.void` is true. The default blocks are lava, water, fire, soul fire, cactus, and sweet berry bush.

The delay is `teleport.delay-seconds`. Money is not taken when the delay starts. Changing block, taking damage, or disconnecting cancels it when `teleport.cancel-on-move` and `teleport.cancel-on-damage` are on. Looking around does not count as moving.

When the delay ends, the plugin withdraws the price and then teleports. Owners skip the price when `teleport.owner-pays` is false, and their visit does not add to the bank.

## Rate, favorites, recent

`/rexplayerwarps rate market 4` stores one rating for you. Running it again with another number replaces that rating. It does not add a second one. The menu average uses those rows.

The favorites button limits the menu to warps you bookmarked. The recent button limits it to warps you have successfully visited, up to `limits.recent-size`, newest visit first in the candidate list, then ordered by the current sort.

The sort button cycles `sorts` from top to bottom and then wraps. Keys that are not in that list are not shown. Unknown keys are ignored.

## Collect

Visitors' payments are added to the warp balance after a successful teleport. `/rexplayerwarps collect market` pays that balance to the owner and sets it back to 0. If the payout fails, the balance is put back.

The owner has to be online to collect, because the payout goes to the player running the command. Visits still add to the balance while the owner is offline.

Deleting your own warp is refused while the balance is above 0. Collect first. An admin delete removes the warp and discards the balance.
