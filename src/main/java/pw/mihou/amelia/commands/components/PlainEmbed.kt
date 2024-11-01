@file:Suppress("NAME_SHADOWING")

package pw.mihou.amelia.commands.components

import pw.mihou.reakt.Reakt
import pw.mihou.reakt.elements.Embed

@Suppress("ktlint:standard:function-naming")
fun Reakt.Document.PlainEmbed(msg: String) =
    component("pw.mihou.amelia.PlainEmbed") {
        val msg: String by ensureProp()
        render {
            Embed {
                Body {
                    msg.append
                }
            }
        }
    }("msg" to msg)
