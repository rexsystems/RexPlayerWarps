---
title: Storage
description: SQLite by default, MySQL if you set it.
sidebar:
  order: 3
---

```yaml
storage:
  type: sqlite
  sqlite:
    file: warps.db
  mysql:
    host: 127.0.0.1
    port: 3306
    database: rexplayerwarps
    username: root
    password: ""
    pool-size: 4
```

`storage.type` is `sqlite` or `mysql`. Anything else is treated as SQLite. The SQLite file is inside `plugins/RexPlayerWarps/` unless `storage.sqlite.file` is an absolute path. SQLite uses one connection. MySQL pool size is clamped from 1 to 20.

Tables:

| Table | What it stores |
|---|---|
| `warps` | Location, description, icon, category, price, access, bank balance, visits |
| `ratings` | One score per warp and player. A new score updates that row |
| `favorites` | One bookmark per warp and player |
| `whitelist` | Name, and the player id when it was known |
| `recent` | The last time each player successfully visited each warp |

Prices and the bank are stored as cents so the collect check is exact.

Queries run through the async scheduler. The plugin does not run SQL on the region thread. Results are applied on the player scheduler or, for startup, the global region scheduler. A command sent before the database has finished loading tells the player that warps are still loading.

Changing `storage.type` in a reload does not reconnect. Restart the server after you switch between SQLite and MySQL or change the MySQL host. Reload still reloads messages, the menu, categories, and the delay.

HikariCP, the SQLite driver, and the MySQL driver are shaded into the jar and relocated to `me.rexsystems.rexPlayerWarps.libs`.
