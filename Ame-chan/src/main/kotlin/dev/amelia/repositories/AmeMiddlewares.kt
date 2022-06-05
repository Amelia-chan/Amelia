package dev.amelia.repositories

import pw.mihou.nexus.features.command.interceptors.facades.NexusInterceptorRepository
import pw.mihou.nexus.features.messages.facade.NexusMessage

object AmeMiddlewares: NexusInterceptorRepository() {

    const val AMELIA_MODERATOR_ONLY = "ame.auth.moderator"

    override fun define() {
        middleware(AMELIA_MODERATOR_ONLY) { event ->
            val server = event.server.orElse(null)
            event.stopIf(
                server == null
                    || !server.canManage(event.user)
                    || !server.isAdmin(event.user)
                    || !server.isOwner(event.user),
                NexusMessage.from("${AmeEmojiRepository.FIGHT_ME} You aren't permitted to use this command!")
            )
        }
    }

}