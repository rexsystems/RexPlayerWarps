---
title: FAQ
description: Delay cancels, the offline bank, and refunds.
---

## The teleport cancelled and nobody was charged

The price is withdrawn only after `teleport.delay-seconds` has passed, and only if that visit is still active. Changing block cancels it when `teleport.cancel-on-move` is true. Damage cancels it when `teleport.cancel-on-damage` is true. Disconnecting cancels it as well. Turning your head does not.

A cancel during the delay does not call the economy. `messages.cancel-move` and `messages.cancel-damage` are the lines for the first two. A player who left does not see a message.

If `teleport.delay-seconds` is `0`, there is no window to cancel. Payment and teleport happen as soon as the confirm step, if any, is done.

## The owner was offline and the visitor still paid

The visitor pays the server economy. RexPlayerWarps then adds that amount to the warp row. It does not need the owner to be online for that write. The balance stays there across restarts.

`/rexplayerwarps collect <name>` is what moves the balance to the owner. The owner runs it while they are online. Collecting sets the stored balance to 0 first. If Vault or the give command fails, the same amount is added back and `messages.collect-failed` is sent.

An admin cannot collect someone else's bank. They can delete the warp, and that delete discards the balance. The owner's own delete is refused until they collect.

## The teleport failed and the money came back

Payment happens, then the teleport. If the teleport result is false, or it throws, the plugin deposits the same amount back to the player who paid. `messages.refunded` follows `messages.teleport-failed`.

If that deposit fails, the bank is not credited, and `messages.refund-failed` tells them the amount. A visit that never succeeds does not increment visits and does not add a recent entry.

A free visit, or an owner visit while `teleport.owner-pays` is false, has nothing to refund. A failed teleport in that case only sends `messages.teleport-failed`.

Quitting after the money was taken but before the teleport finishes also tries to return the money, including through Vault to the offline account.
