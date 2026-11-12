---
title: Commands
description: /rexplayerwarps and the rpwarp and pwarp aliases.
sidebar:
  order: 1
---

The command is `/rexplayerwarps`. Aliases are `/rpwarp` and `/pwarp`. With no arguments, it shows help.

| Command | What it does |
|---|---|
| `/rexplayerwarps help` | Prints `messages.help` |
| `/rexplayerwarps create <name>` | Saves a warp at your feet |
| `/rexplayerwarps delete <name>` | Deletes that warp. Owners are blocked while the bank is above 0 |
| `/rexplayerwarps set <name> description <text>` | Replaces the description. The clear word stores an empty one |
| `/rexplayerwarps set <name> icon <material>` | Sets the menu icon |
| `/rexplayerwarps set <name> category <id>` | Sets a category from `categories.yml` |
| `/rexplayerwarps set <name> price <amount>` | Sets the visit price |
| `/rexplayerwarps set <name> access <public\|private\|whitelist>` | Sets who can visit |
| `/rexplayerwarps list` | Lists warps you own, with access, price, visits, and bank |
| `/rexplayerwarps open [search]` | Opens the menu. Extra words become the search |
| `/rexplayerwarps favorite <name>` | Toggles your bookmark |
| `/rexplayerwarps rate <name> <1-5>` | Sets your rating. A second rate updates the same row |
| `/rexplayerwarps collect <name>` | Pays you the warp bank |
| `/rexplayerwarps whitelist <name> add <player>` | Adds a player |
| `/rexplayerwarps whitelist <name> remove <player>` | Removes a player |
| `/rexplayerwarps whitelist <name> list` | Prints the names |
| `/rexplayerwarps reload` | Reloads `config.yml` and re-reads `categories.yml` |

Console can run `help` and `reload`. The other commands need a player.

There is no separate confirm command. A price or an unsafe spot is confirmed by clicking that warp again.
