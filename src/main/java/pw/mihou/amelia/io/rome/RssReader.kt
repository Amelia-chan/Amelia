package pw.mihou.amelia.io.rome

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit
import org.w3c.dom.NodeList
import pw.mihou.amelia.Amelia
import pw.mihou.amelia.io.xml.SimpleXmlClient
import pw.mihou.amelia.logger.logger

object RssReader {
    private val cache: LoadingCache<String, Pair<Date, List<FeedItem>>> =
        Caffeine
            .newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build(this::request)

    private fun nodeListToFeedItems(nodeList: NodeList): List<FeedItem> {
        val mutableList = mutableListOf<FeedItem>()
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            mutableList.add(FeedItem(node))
        }

        return mutableList.toList()
    }

    private fun request(url: String): Pair<Date, List<FeedItem>>? {
        return try {
            val document =
                SimpleXmlClient.read(
                    url.replaceFirst(
                        "https://www.scribblehub.com",
                        "https://www.rssscribblehub.com",
                    ),
                )
            val lastBuildDate = document.getElementsByTagName("lastBuildDate").item(0)
            if (lastBuildDate.textContent == "") {
                logger.warn("$url has no last build date.")
                return Date.from(Instant.now()) to emptyList()
            }
            Amelia.formatter.parse(lastBuildDate.textContent) to
                nodeListToFeedItems(document.getElementsByTagName("item"))
        } catch (exception: Exception) {
            logger.error("Failed to connect to $url, discarding request...", exception)
            return null
        }
    }

    fun cached(url: String): Pair<Date, List<FeedItem>>? {
        synchronized(url) {
            return cache.get(url)
        }
    }
}
