---
title: Settings
description: Limits, delay, economy, sorts, and worlds.
sidebar:
  order: 1
---

`config-version` is for the updater. Do not delete keys to "reset" them. Remove a value only when you want the next reload to copy the default back in. The updater writes `config.yml.bak.<timestamp>` before it saves.

## Worlds

`worlds.whitelist` empty means every world. `worlds.blacklist` always blocks. The check applies to creating a warp and to teleporting to one. Owners can still see their own warps in `/rexplayerwarps list` and delete them.

## Defaults and limits

| Key | Role |
|---|---|
| `defaults.description` | Text on a new warp |
| `defaults.icon` | Material name |
| `defaults.category` | Category id |
| `defaults.price` | Decimal price |
| `defaults.access` | `public`, `private`, or `whitelist` |
| `limits.name-length` | Clamped to 1-32 |
| `limits.description-length` | Clamped to 1-512 |
| `limits.price-min` | Lowest price, never below 0 |
| `limits.price-max` | Highest price |
| `limits.search-length` | Chat search cap, clamped to 1-64 |
| `limits.recent-size` | How many recent visits the menu keeps |
| `number-format` | `full` (`10.50`) or `short` (`1.2K`, `3.4M`, `5B`) |
| `description-clear` | Word that clears a description |
| `search-cancel` | Chat word that clears the menu search |

## Unsafe spots

`unsafe.blocks` is the material list. The default is `LAVA`, `WATER`, `FIRE`, `SOUL_FIRE`, `CACTUS`, and `SWEET_BERRY_BUSH`. The check looks at the block at the saved feet position and the block directly under it. `unsafe.void` is true when the feet Y is below the world minimum height. `unsafe.confirm` asks for a second click before the delay starts.

## Teleport

| Key | Role |
|---|---|
| `teleport.delay-seconds` | Wait before money is taken. `0` charges and teleports immediately |
| `teleport.cancel-on-move` | Cancel when the player changes block |
| `teleport.cancel-on-damage` | Cancel when the player takes damage |
| `teleport.owner-pays` | When false, the owner does not pay and the bank is not credited |
| `teleport.price-confirm` | Second click when the price is above 0 |
| `teleport.confirm-seconds` | How long that second click stays valid |

A reload does not cancel a delay that has already started. That visit keeps the price and the economy it started with.

## Economy

`economy.mode` is `vault` or `command`.

Vault uses the economy registered in Vault. If Vault is missing, free warps still work and paid warps refuse with `messages.economy-missing`.

Command mode reads `economy.command.balance-placeholder` through PlaceholderAPI, then runs `economy.command.take` and `economy.command.give` as console. `{player}` and `{amount}` are replaced. `{amount}` is a plain number such as `10.50`. PlaceholderAPI has to be installed for the balance check.

## Sorts

`sorts` is the cycle order. Only these keys are recognized:

`name`, `owner`, `rating`, `visits`, `newest`, `oldest`, `price`, `favorites`, `category`

The menu shows them in the order you list. Duplicates and unknown keys are dropped. If the list is empty, the menu sorts by name. `sort-labels` is the text on the sort button.

`name` and `owner` and `category` are A to Z. `rating`, `visits`, and `favorites` put the larger number first. `newest` is the latest `created_at`. `oldest` is the earliest. `price` is cheapest first. Equal rows fall back to the warp name.

## Feedback

`sounds.<name>.sound` empty disables that sound. Volume and pitch are the Bukkit values.

`titles.countdown` and `titles.arrived` have `enabled`, `title`, `subtitle`, `fade-in`, `stay`, and `fade-out` in ticks.

`actionbar.countdown` and `bossbar.countdown` are off unless `enabled` is true. Boss bar colors are `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, and `WHITE`. Styles are `SOLID`, `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, and `SEGMENTED_20`.

`particles.teleport` plays at the player after a successful teleport. `count` is clamped to 0-200.
