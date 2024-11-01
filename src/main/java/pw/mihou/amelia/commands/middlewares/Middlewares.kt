package pw.mihou.amelia.commands.middlewares

import pw.mihou.amelia.commands.templates.TemplateMessages
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.messages.NexusMessage

object Middlewares {
    val MODERATOR_ONLY =
        Nexus.interceptors.middleware("amelia.role.moderator") { event ->
            if (event.server.isEmpty) {
                event.stop(
                    NexusMessage.from("❌ You cannot use this command in a private channel.", true),
                )
                return@middleware
            }

            val server = event.server.orElseThrow()

            if (!(
                    server.isAdmin(event.user) ||
                        server.isOwner(event.user) ||
                        server.canManage(event.user) ||
                        server.canCreateChannels(event.user)
                )
            ) {
                event.stop(
                    NexusMessage.from(TemplateMessages.ERROR_MISSING_PERMISSIONS, true),
                )
                return@middleware
            }
        }
}
