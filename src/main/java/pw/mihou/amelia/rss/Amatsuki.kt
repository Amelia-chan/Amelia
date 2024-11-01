package pw.mihou.amelia.rss

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import org.jsoup.Jsoup
import pw.mihou.Amaririsu
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.db.models.FeedModel
import pw.mihou.amelia.logger.logger
import pw.mihou.amelia.rss.reader.FeedItem
import pw.mihou.cache.Cache
import pw.mihou.cache.Cacheable

object Amatsuki {
    val cache =
        Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofHours(24))
            .build<String, Cacheable>()

    private val BASE_STORY_URL: (Int) -> String = { id ->
        "${Configuration.BASE_SCRIBBLEHUB_URL}/series/$id/amelia-discord-bot/"
    }
    private val BASE_USER_URL: (
        Int,
    ) -> String = { id -> "${Configuration.BASE_SCRIBBLEHUB_URL}/profile/$id/amelia-discord-bot/" }

    fun init() {
        Amaririsu.set(
            object : Cache {
                override fun get(uri: String): Cacheable? = cache.getIfPresent(uri)

                override fun set(
                    uri: String,
                    item: Cacheable,
                ) {
                    cache.put(uri, item)
                }
            },
        )
        Amaririsu.connector = { url ->
            val replacedUrl =
                url.replaceFirst(
                    "https://www.scribblehub.com",
                    Configuration.BASE_SCRIBBLEHUB_URL,
                )
            logger.info("Connecting $replacedUrl")
            var conn =
                Jsoup
                    .connect(replacedUrl)
            Configuration.BASE_AUTHORIZATION_TOKEN?.let {
                conn = conn.header("Authorization", it)
            }
            conn.get()
        }
    }

    fun authorFrom(
        item: FeedItem,
        feed: FeedModel,
    ): String {
        try {
            if (item.author != null) return item.author

            if (feed.feedUrl.contains("type=author")) {
                return Amaririsu.user(BASE_USER_URL.invoke(feed.id)).name
            }

            if (item.category != null) {
                return Amaririsu
                    .series(BASE_STORY_URL.invoke(item.category)) {
                        includeTags = false
                        includeSynopsis = false
                        includeGenres = false
                    }.author.name
            }

            return Amaririsu
                .series(BASE_STORY_URL.invoke(feed.id)) {
                    includeTags = false
                    includeSynopsis = false
                    includeGenres = false
                }.author.name
        } catch (exception: Exception) {
            logger.error(
                "Failed to propagate author from the ${feed.feedUrl} with related " +
                    "[id=${feed.id}, category=${item.category}], an exception was raised.",
                exception,
            )
            throw exception
        }
    }
}
