package pw.mihou.amelia.io.rome

import com.apptastic.rssreader.RssReader
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import pw.mihou.amelia.logger
import java.util.concurrent.TimeUnit

object RssReader {

    private val reader = RssReader().setUserAgent("Amelia/2.0.0-luminous (Language=Kotlin/1.7.10, Developer=Shindou Mihou)")

    val cache: LoadingCache<String, List<ItemWrapper>> = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)
        .build(this::request)

    fun request(url: String, attempts: Int = 1): List<ItemWrapper> {
        try {
            return reader.read(url).map { item -> ItemWrapper(item) }.toList()
        } catch (exception: Exception) {
            if (attempts > 10) {
                logger.error("Failed to connect to $url after 10 attempts, discarding request...", exception)
                return emptyList()
            }

            logger.error("Attempting to reconnect to $url in $attempts second from now.", exception)
            Thread.sleep(attempts * 1000L)
            return request(url, attempts + 1)
        }
    }

    fun cached(url: String) = cache.get(url)

}