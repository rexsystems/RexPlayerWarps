# RexPlayerWarps

Player warps for Paper, Purpur, and Folia. A player creates a warp at their feet, other players pay to visit, and the price sits in that warp's bank until the owner collects it.

<p>
  <img src="https://img.shields.io/badge/Minecraft-1.21%2B-62B47A?style=for-the-badge" alt="Minecraft 1.21+">
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
</p>

## Features

- Create a warp with a name, description, icon, category, price, and access: public, private, or whitelist
- Menu search by warp name or owner name, plus recent visits and favorites
- Sort cycle from `sorts` in `config.yml`, in the order written there
- One rating per player, from 1 to 5. Rating again updates that same rating
- Visitors pay when the teleport delay finishes. The warp stores the money, including while the owner is offline
- Unsafe spots and prices ask for a second click. Moving, damage, or quitting during the delay cancels without taking money
- If the teleport fails after payment, the visitor is refunded
- SQLite by default, MySQL optional. Database work runs off the region thread
- Warp limit is the highest `rexplayerwarps.warps.<n>` the player has

## Requirements

- Java 21
- Paper, Purpur, or Folia 1.21+
- Vault is optional. Without it, free warps still work. Paid warps need Vault or `economy.mode: command`
- PlaceholderAPI is optional

## Install

1. Put the jar in `plugins/`
2. Restart
3. Edit `plugins/RexPlayerWarps/config.yml` and, if you want, `categories.yml`
4. Run `/rexplayerwarps reload`

Grant a warp limit, for example `rexplayerwarps.warps.3`, or players with `rexplayerwarps.create` still cannot create a warp.

## Commands

| Command | Permission |
|---|---|
| `/rexplayerwarps help` | everyone |
| `/rexplayerwarps open [search]` | `rexplayerwarps.use` |
| `/rexplayerwarps list` | `rexplayerwarps.use` |
| `/rexplayerwarps create <name>` | `rexplayerwarps.create` and a `rexplayerwarps.warps.<n>` node |
| `/rexplayerwarps set <name> <field> <value>` | owner, or `rexplayerwarps.admin` |
| `/rexplayerwarps delete <name>` | owner, or `rexplayerwarps.admin` |
| `/rexplayerwarps favorite <name>` | `rexplayerwarps.use` |
| `/rexplayerwarps rate <name> <1-5>` | `rexplayerwarps.use` |
| `/rexplayerwarps collect <name>` | owner |
| `/rexplayerwarps whitelist <name> <add\|remove\|list> [player]` | owner, or `rexplayerwarps.admin` |
| `/rexplayerwarps reload` | `rexplayerwarps.reload` |

Aliases: `/rpwarp`, `/pwarp`

## Documentation

The pages in [docs/](docs/index.md) drop into a Starlight site at `src/content/docs/rexplayerwarps/`. Paste `docs/sidebar.mjs` into the site sidebar.

Full docs: [https://docs.rexsystems.me/rexplayerwarps/](https://docs.rexsystems.me/rexplayerwarps/)

## License

Licensed under the [GNU Affero General Public License v3.0](LICENSE).
