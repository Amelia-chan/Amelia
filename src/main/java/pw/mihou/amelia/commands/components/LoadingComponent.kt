package pw.mihou.amelia.commands.components

import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.reakt.Reakt

val Reakt.Document.Loading get() =
    component("pw.mihou.amelia.loading") {
        render {
            PlainEmbed(TemplateMessages.NEUTRAL_LOADING)
        }
    }
