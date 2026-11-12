---
title: Messages
description: Chat lines, titles, and menu item text.
sidebar:
  order: 4
---

Every player-facing string is under `messages`, `titles`, `actionbar`, `bossbar`, or the menu item names. MiniMessage, `&` codes, `&#RRGGBB`, and `#RRGGBB` all go through the same parser. A broken MiniMessage tag falls back to legacy colors instead of dropping the line.

`{prefix}` and `%prefix%` use `messages.prefix`. Names inserted from the server are escaped so a player name cannot inject tags.

PlaceholderAPI placeholders are resolved after the plugin's own `{tokens}`, and only when PlaceholderAPI is installed.

## Chat

| Key | When it is sent |
|---|---|
| `messages.help` | `/rexplayerwarps help` |
| `messages.no-permission` | Missing permission |
| `messages.players-only` | Console tried a player command |
| `messages.storage-starting` | Database is still opening |
| `messages.reloaded` | Reload finished |
| `messages.usage` | Not enough arguments |
| `messages.created` | Warp created |
| `messages.name-invalid` | Name failed the length or character check |
| `messages.name-taken` | That name already exists |
| `messages.limit` | The player is at their warp limit |
| `messages.world-blocked` | World list refused the warp |
| `messages.not-found` | Unknown warp, or a hidden one |
| `messages.not-owner` | Not allowed to edit it |
| `messages.deleted` | Warp removed |
| `messages.delete-balance` | Owner tried to delete a warp that still has a bank |
| `messages.updated` | `/set` saved |
| `messages.description-long` | Description over the max length |
| `messages.icon-invalid` | Material is not an item |
| `messages.category-unknown` | Category id is not in `categories.yml` |
| `messages.price-invalid` | Price was not a number |
| `messages.price-range` | Price outside min and max |
| `messages.access-invalid` | Access was not public, private, or whitelist |
| `messages.field-unknown` | `/set` field was not recognized |
| `messages.list-header` | Start of `/list` |
| `messages.list-line` | One owned warp |
| `messages.list-empty` | The player owns none |
| `messages.favorite-on` / `favorite-off` | Bookmark toggled |
| `messages.rated` | Rating saved |
| `messages.rate-range` | Score was not 1-5 |
| `messages.collect-empty` | Bank was already 0 |
| `messages.collected` | Payout succeeded |
| `messages.collect-failed` | Payout failed and the bank was restored |
| `messages.whitelist-added` / `whitelist-removed` | Whitelist edit |
| `messages.whitelist-have` / `whitelist-missing` | Already listed, or not listed |
| `messages.whitelist-header` / `whitelist-line` / `whitelist-empty` | `/whitelist list` |
| `messages.whitelist-usage` | Wrong whitelist arguments |
| `messages.search-prompt` | Menu is waiting for chat |
| `messages.search-set` / `search-cleared` / `search-long` | Search chat result |
| `messages.confirm-price` | Price needs a second click |
| `messages.confirm-unsafe` | Unsafe spot needs a second click |
| `messages.confirm-both` | Price and unsafe spot |
| `messages.teleporting` | Delay started |
| `messages.cancel-move` / `cancel-damage` | Delay cancelled. No money was taken |
| `messages.arrived` | Teleport succeeded |
| `messages.teleport-failed` | Teleport returned false |
| `messages.refunded` / `refund-failed` | Money returned, or the refund call failed |
| `messages.broke` | The player could not pay |
| `messages.economy-missing` | Paid warp but no Vault or command economy |
| `messages.cannot-visit` | Private or whitelist blocked them |
| `messages.already-teleporting` | A delay or payment is already running |
| `messages.world-missing` | The destination world is unloaded |
| `messages.warp-removed` | The warp disappeared before the teleport |

Common tokens are `{warp}`, `{owner}`, `{player}`, `{price}`, `{balance}`, `{visits}`, `{access}`, `{category}`, `{score}`, `{limit}`, `{count}`, `{seconds}`, `{block}`, `{search}`, `{cancel}`, `{min}`, `{max}`, and `{field}`.

## Menu text

`messages.warp-name` and `messages.warp-lore` are the warp icon. `{favorite}` in a lore line is replaced with `messages.lore-favorite-yes` or `messages.lore-favorite-no` without escaping that line's colors.

`messages.rating-empty` is used when nobody has rated the warp. `messages.category-all` and `messages.search-all` are the labels for "no category filter" and "no search".

`gui.title` receives `{page}`, `{pages}`, `{sort}`, `{search}`, `{category}`, and `{player}`. Button names and lore receive the same set.
