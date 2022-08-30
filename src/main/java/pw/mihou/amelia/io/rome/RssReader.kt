package pw.mihou.amelia.io.rome

import com.apptastic.rssreader.RssReader
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import pw.mihou.amelia.logger
import pw.mihou.amelia.scheduledExecutorService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object RssReader {

    private val reader = RssReader().setUserAgent("Amelia/2.0.0-luminous (Language=Kotlin/1.7.10, Developer=Shindou Mihou)")

    private val cache: LoadingCache<String, List<ItemWrapper>> = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)
        .build(this::request)

    private fun request(url: String): List<ItemWrapper> {
        return try {
            reader.read(url).map { item -> ItemWrapper(item) }.toList()
        } catch (exception: Exception) {
            val future: CompletableFuture<List<ItemWrapper>> = CompletableFuture()
            retry(url = url, attempts = 1, future = future)

            future.join()
        }
    }

    private fun retry(url: String, attempts: Int, future: CompletableFuture<List<ItemWrapper>>) {
        try {
            future.complete(reader.read(url).map { item -> ItemWrapper(item) }.toList())
        } catch (exception: Exception) {
            if (attempts > 10){
                logger.error("Failed to connect to $url after 10 attempts, discarding request...", exception)
                future.complete(emptyList())
                return
            }

            logger.error("Attempting to reconnect to $url in $attempts second(s) from now.", exception)
            scheduledExecutorService.schedule({ retry(url, attempts + 1, future) }, attempts.toLong(),
                TimeUnit.SECONDS)
        }
    }

    fun cached(url: String): List<ItemWrapper> {
        synchronized(url) {
            return cache.get(url)
        }
    }

}