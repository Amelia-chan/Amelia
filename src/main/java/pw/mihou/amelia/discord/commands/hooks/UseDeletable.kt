package pw.mihou.amelia.discord.commands.hooks

import pw.mihou.amelia.logger.logger
import pw.mihou.reakt.Reakt

val Reakt.useDeletable: Reakt.Writable<Boolean> get() =
    Reakt.Writable(false).apply {
        subscribe { _, newValue ->
            if (newValue) {
                logger.info("Deleting the message...")
                this@useDeletable.delete()
            }
        }
    }
