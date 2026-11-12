---
title: Placeholders
description: Count, bank, and limit for PlaceholderAPI.
sidebar:
  order: 3
---

These register when PlaceholderAPI is already installed at enable. They read the in-memory warp list, not the database, so they stay off the region thread's SQL path. Until storage has finished loading they are empty.

| Placeholder | Description |
|---|---|
| `%rexplayerwarps_count%` | How many warps that player owns |
| `%rexplayerwarps_bank%` | Sum of their warp banks, using `number-format` |
| `%rexplayerwarps_limit%` | Highest `rexplayerwarps.warps.<n>` for an online player. Offline lookups return `0` |

Inside `config.yml`, menu and message tokens use braces, such as `{warp}` and `{player}`. Those are not PlaceholderAPI identifiers. External placeholders in a message are resolved when PlaceholderAPI is present.
