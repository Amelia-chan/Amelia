package pw.mihou.amelia.discord.commands.components

import pw.mihou.amelia.discord.commands.templates.TemplateMessages
import pw.mihou.reakt.Reakt

val Reakt.Document.Loading get() =
    component("pw.mihou.amelia.loading") {
        render {
            PlainEmbed(TemplateMessages.NEUTRAL_LOADING)
        }
    }
