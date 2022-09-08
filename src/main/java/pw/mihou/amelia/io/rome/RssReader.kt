package pw.mihou.amelia.io.rome

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.w3c.dom.NodeList
import pw.mihou.amelia.io.xml.SimpleXmlClient
import pw.mihou.amelia.logger
import java.util.concurrent.TimeUnit

object RssReader {

    private val cache: LoadingCache<String, List<FeedItem>> = Caffeine.newBuilder()
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

    private fun request(url: String): List<FeedItem>? {
        return try {
            nodeListToFeedItems(SimpleXmlClient.read(url).getElementsByTagName("item"))
        } catch (exception: Exception) {
            logger.error("Failed to connect to $url, discarding request...", exception)
            return null
        }
    }

    fun cached(url: String): List<FeedItem>? {
        synchronized(url) {
            return cache.get(url)
        }
    }

}