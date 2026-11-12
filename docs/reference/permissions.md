---
title: Permissions
description: Use, create, reload, admin, and the warp limit nodes.
sidebar:
  order: 2
---

| Permission | Default | Description |
|---|---|---|
| `rexplayerwarps.use` | true | Open the menu, list, favorite, rate, and collect your own bank |
| `rexplayerwarps.create` | true | Create a warp. Editing a warp you own does not need this node |
| `rexplayerwarps.reload` | op | `/rexplayerwarps reload` |
| `rexplayerwarps.admin` | op | Edit or delete someone else's warp, and visit private or whitelist warps. Collect still belongs to the owner. An admin delete throws away a remaining bank |
| `rexplayerwarps.warps.<n>` | false | The player may own `<n>` warps |

The limit is the highest `<n>` among granted nodes that match `rexplayerwarps.warps.<n>` exactly. `rexplayerwarps.warps.3` and `rexplayerwarps.warps.10` allow 10. `rexplayerwarps.warps.4.extra` is ignored. Nodes are compared without case sensitivity.

`plugin.yml` registers `rexplayerwarps.warps.1`, `.3`, `.5`, and `.10` so they show up in permission editors. Any other positive number works without being listed there. You still have to grant it. A player with only `rexplayerwarps.create` has a limit of 0.

`rexplayerwarps.admin` does not raise the limit. Give that account a `warps.<n>` node as well if they should create warps.
