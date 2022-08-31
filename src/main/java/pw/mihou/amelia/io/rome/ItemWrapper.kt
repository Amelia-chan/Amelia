package pw.mihou.amelia.io.rome

import com.apptastic.rssreader.Item
import pw.mihou.amelia.Amelia.formatter
import pw.mihou.amelia.logger
import java.text.ParseException
import java.util.*

class ItemWrapper(item: Item) {

    val title: String
    val date: Date?
    val author: String
    val link: String
    val category: String?
    private val description: String

    init {
        title = item.title.orElse("")
        description = item.description.orElse("")
        author = item.author.orElse("")
        link = item.link.orElse("")
        date = item.pubDate.map { source: String ->
            try {
                return@map formatter.parse(source)
            } catch (e: ParseException) {
                logger.error("Amelia wasn't able to parse the date {}, exception: {}", source, e.message)
                return@map null
            }
        }.orElse(null)
        category = item.category.orElse(null)
    }

    fun valid(): Boolean {
        return link.isNotEmpty() && link.isNotBlank() && title.isNotEmpty() && title.isNotBlank()
    }

    override fun toString(): String {
        return "{title=$title, date=$date, author=$author, link=$link, description=$description, category=$category}"
    }
}