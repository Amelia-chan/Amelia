package pw.mihou.amelia.utility

import pw.mihou.amelia.extensions.params
import pw.mihou.amelia.extensions.toUrl

/**
 * Redacts the reading list link, this exposes the user and the list number if
 * there is any list number.
 *
 * @param url The URL to redact.
 * @return The standard redacted list link contents.
 */
fun redactListLink(url: String): String {
    var link = url

    val (userId, listId) = userIdAndListIdFrom(link)
    link = "[**REDACTED**](https://github.com/Amelia-chan/Amelia/discussions/19) "

    link += "[[User](https://scribblehub.com/profile/$userId/from-amelia)"
    if (listId != null) {
        link += ", Reading List No. $listId"
    }
    link += "]"

    return link
}

/**
 * Gets the user id and the list id from the given url.
 *
 * @param url the URL to request from.
 * @return The user id and list id if there is any.
 */
fun userIdAndListIdFrom(url: String): Pair<Int, Int?> {
    val link = url.toUrl()

    val queries = link.params
    val uid = queries["uid"]!!.toInt()
    val lid = queries["lid"]?.toIntOrNull()

    return Pair(uid, lid)
}