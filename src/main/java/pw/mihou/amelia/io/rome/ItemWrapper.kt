package pw.mihou.amelia.io.rome

import com.apptastic.rssreader.Item
import com.squareup.moshi.JsonClass
import pw.mihou.amelia.Amelia.formatter
import pw.mihou.amelia.logger
import java.text.ParseException
import java.util.*

@JsonClass(generateAdapter = true)
class ItemWrapper {

    val title: String
    val date: Date?
    val author: String
    val link: String
    private val description: String

    constructor(item: Item) {
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
    }

    constructor(title: String, date: Date?, author: String, link: String, description: String) {
        this.title = title
        this.date = date
        this.author = author
        this.link = link
        this.description = description
    }

    fun valid(): Boolean {
        return link.isNotEmpty() && link.isNotBlank() && title.isNotEmpty() && title.isNotBlank()
    }
}