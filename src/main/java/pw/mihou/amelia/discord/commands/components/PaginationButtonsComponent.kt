@file:Suppress("NAME_SHADOWING")

package pw.mihou.amelia.discord.commands.components

import org.javacord.api.entity.user.User
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.elements.PrimaryButton
import pw.mihou.reakt.elements.SecondaryButton

@Suppress("ktlint:standard:function-naming")
fun Reakt.Document.PaginatedButtons(
    page: Reakt.Writable<Int>,
    delete: Reakt.Writable<Boolean>? = null,
    onSelect: (() -> Unit)? = null,
    user: User,
    lastPage: Int? = null,
) = component("pw.mihou.amelia.components.PaginationButtons") {
    var page by writableProp<Int>("page")

    val lastPage: Int? by prop()

    val deleteDelegate: Reakt.Writable<Boolean>? = prop("delete")
    val user: User by ensureProp()

    render {
        SecondaryButton("", emoji = "⬅\uFE0F", disabled = page <= 0) {
            it.buttonInteraction.acknowledge()
            if (user.id != it.buttonInteraction.user.id) {
                return@SecondaryButton
            }
            page--
        }
        if (deleteDelegate != null) {
            SecondaryButton("", emoji = "\uD83D\uDDD1\uFE0F") {
                it.buttonInteraction.acknowledge()
                if (user.id != it.buttonInteraction.user.id) {
                    return@SecondaryButton
                }
                deleteDelegate?.set(true)
            }
        }
        if (onSelect != null) {
            PrimaryButton("Select") {
                it.buttonInteraction.acknowledge()
                if (user.id != it.buttonInteraction.user.id) {
                    return@PrimaryButton
                }
                onSelect()
            }
        }
        SecondaryButton(
            "",
            emoji = "➡\uFE0F",
            disabled =
                if (lastPage != null) {
                    (page + 1) >= lastPage!!
                } else {
                    false
                },
        ) {
            it.buttonInteraction.acknowledge()
            if (user.id != it.buttonInteraction.user.id) {
                return@SecondaryButton
            }
            page++
        }
    }
}("page" to page, "delete" to delete, "lastPage" to lastPage, "user" to user)
