package pw.mihou.commons.discord.delegates

import pw.mihou.amelia.discord.delegates.DiscordClientDelegate

interface DiscordClientInterface {
    fun connect() {
        DiscordClientDelegate.connect()
    }
}
