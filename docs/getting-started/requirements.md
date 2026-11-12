---
title: Requirements
description: Java 21 and Paper 1.21 or newer.
sidebar:
  order: 2
---

- Java 21
- Paper, Purpur, or Folia 1.21+
- Vault, optional, for `economy.mode: vault`
- PlaceholderAPI, optional, for placeholders and for `economy.mode: command`

The plugin declares `folia-supported: true`. Database queries run on the async scheduler. Inventory and teleport work is handed back to the player scheduler, and block checks at the warp run on that location's region scheduler.

SQLite and MySQL drivers are packed inside the jar under `me.rexsystems.rexPlayerWarps.libs`. You do not install them on the server.
