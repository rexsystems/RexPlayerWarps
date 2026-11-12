---
title: RexPlayerWarps
description: Player warps, ratings, and a warp bank for Paper, Purpur, and Folia.
---

RexPlayerWarps lets players create warps at their feet. Visitors open a menu, pay the price after a delay, and the money stays on the warp until the owner collects it.

Messages use MiniMessage. Legacy `&` codes, `&#RRGGBB`, and `#RRGGBB` also work. The same parser is used for chat, titles, the action bar, the boss bar, and item text in the menu.

## Files

```
plugins/RexPlayerWarps/
  config.yml
  categories.yml
  warps.db
```

`warps.db` is created only when `storage.type` is `sqlite`. A reload that changes `config.yml` writes `config.yml.bak.<timestamp>` first, then adds any keys that were missing. Values you already set stay as they are. `categories.yml` is created if it is missing and is not overwritten.

## Next

- [Installation](/rexplayerwarps/getting-started/installation/)
- [Warps](/rexplayerwarps/guides/warps/)
- [Categories](/rexplayerwarps/configuration/categories/)
- [Commands](/rexplayerwarps/reference/commands/)
