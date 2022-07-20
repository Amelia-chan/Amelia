package pw.mihou.amelia.commands.middlewares

import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.nexus.features.command.interceptors.facades.NexusInterceptorRepository
import pw.mihou.nexus.features.messages.facade.NexusMessage

object Middlewares: NexusInterceptorRepository() {

    const val MODERATOR_ONLY = "amelia.role.moderator"

    override fun define() {
        middleware(MODERATOR_ONLY) { event ->
            if (event.server.isEmpty) {
                event.stop(NexusMessage.fromEphemereal("❌ You cannot use this command in a private channel."))
                return@middleware
            }

            val server = event.server.orElseThrow()

            if (!(server.isAdmin(event.user) || server.isOwner(event.user) || server.canManage(event.user)
                        || server.canCreateChannels(event.user))) {
                event.stop(
                    NexusMessage.fromEphemereal(TemplateMessages.ERROR_MISSING_PERMISSIONS)
                )
                return@middleware
            }
        }
    }

}