@file:Suppress("NAME_SHADOWING")

package pw.mihou.amelia.discord.commands.components

import pw.mihou.models.user.UserResultOrAuthor
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.elements.Embed

@Suppress("ktlint:standard:function-naming")
fun Reakt.Document.UserResultEmbed(
    user: UserResultOrAuthor,
    page: Int,
    lastPage: Int,
) = component("pw.mihou.amelia.UserResultEmbed") {
    val user by ensureProp<UserResultOrAuthor>()
    val page by ensureProp<Int>()
    val lastPage by ensureProp<Int>()

    render {
        Embed {
            Color(java.awt.Color.YELLOW)
            Title(user.name)
            Body(spaced = true) {
                (
                    "Profile link: ".bold +
                        link(user.url.replaceFirst("https://", ""), user.url)
                ).append
                (
                    "You can create a feed for this user by pressing the **✅** " +
                        "button, enabling you to receive notifications for new chapters " +
                        "of all stories of the user."
                ).italicized
                    .append
            }
            Image(user.avatar)
            CurrentTimestamp()
            Footer(
                "You are looking at ${page + 1} out of $lastPage pages",
            )
        }
    }
}("user" to user, "page" to page, "lastPage" to lastPage)
