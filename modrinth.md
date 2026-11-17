# RexPlayerWarps

Player warps for Paper, Purpur, and Folia. Create a warp where you stand, set who can use it, and let visitors pay into that warp's bank. You collect the balance later, including visits that happened while you were offline.

**Java 21. Paper, Purpur, or Folia 1.21+.** Vault and PlaceholderAPI are optional.

## Features

- Name, description, icon, category, price, and access: public, private, or whitelist
- Menu with search by warp name or owner, favorites, and the warps you visited recently
- Sort buttons cycle the keys you list under `sorts`, in that order: name, owner, rating, visits, newest, oldest, price, favorites, category
- Each player rates a warp once, from 1 to 5. A later rate replaces the earlier one
- The price is taken only after the teleport delay. Moving, taking damage, or leaving cancels the visit and does not charge
- A failed teleport returns the money
- The highest `rexplayerwarps.warps.<n>` permission is how many warps that player can create
- SQLite is the default database. MySQL is optional
- Menu rows, title, filler, and button slots are in `config.yml`. Slot `-1` hides a button

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rexplayerwarps help` | Show the command list | everyone |
| `/rexplayerwarps open [search]` | Open the menu | `rexplayerwarps.use` |
| `/rexplayerwarps list` | List your warps | `rexplayerwarps.use` |
| `/rexplayerwarps create <name>` | Create a warp at your feet | `rexplayerwarps.create` |
| `/rexplayerwarps delete <name>` | Delete a warp | owner, or `rexplayerwarps.admin` |
| `/rexplayerwarps set <name> <field> <value>` | Change description, icon, category, price, or access | owner, or `rexplayerwarps.admin` |
| `/rexplayerwarps favorite <name>` | Bookmark or unbookmark a warp | `rexplayerwarps.use` |
| `/rexplayerwarps rate <name> <1-5>` | Set your rating | `rexplayerwarps.use` |
| `/rexplayerwarps collect <name>` | Take the warp bank | owner |
| `/rexplayerwarps whitelist <name> <add\|remove\|list> [player]` | Edit the whitelist | owner, or `rexplayerwarps.admin` |
| `/rexplayerwarps reload` | Reload `config.yml` and re-read `categories.yml` | `rexplayerwarps.reload` |

Aliases: `/rpwarp`, `/pwarp`

## Permissions

| Permission | Default | Description |
|---|---|---|
| `rexplayerwarps.use` | true | Open the menu, favorite, rate, and list |
| `rexplayerwarps.create` | true | Create a warp. A `rexplayerwarps.warps.<n>` node still sets the limit |
| `rexplayerwarps.reload` | op | Reload the plugin |
| `rexplayerwarps.admin` | op | Edit, delete, and visit warps you do not own |
| `rexplayerwarps.warps.<n>` | false | Allows `<n>` warps. The highest matching node is the limit |

Documentation: https://docs.rexsystems.me/rexplayerwarps/
