package pw.mihou.amelia.rss.reader

import java.text.ParseException
import java.util.*
import org.w3c.dom.Node
import pw.mihou.amelia.Amelia.formatter
import pw.mihou.amelia.logger.logger

class FeedItem(
    node: Node,
) {
    val title: String?
    val date: Date?
    val author: String?
    val link: String?
    val category: Int?

    init {
        var localTitle: String? = null
        var localDate: Date? = null
        var localAuthor: String? = null
        var localLink: String? = null
        var localCategory: Int? = null

        for (i in 0 until node.childNodes.length) {
            val childNode = node.childNodes.item(i)

            when (childNode.nodeName) {
                "title" -> {
                    localTitle = childNode.textContent
                }
                "category" -> {
                    localCategory = childNode.textContent.toIntOrNull()
                }
                "link" -> {
                    localLink = childNode.textContent
                }
                "dc:creator" -> {
                    localAuthor = childNode.textContent
                }
                "pubDate" -> {
                    try {
                        localDate = formatter.parse(childNode.textContent)
                    } catch (e: ParseException) {
                        logger.error(
                            "Amelia wasn't able to parse the date {}, exception: {}",
                            childNode.textContent,
                            e.message,
                        )
                    }
                }
            }
        }

        title = localTitle
        author = localAuthor
        link = localLink
        category = localCategory
        date = localDate
    }

    override fun toString(): String =
        "{title=$title, date=$date, author=$author, link=$link, category=$category}"
}
