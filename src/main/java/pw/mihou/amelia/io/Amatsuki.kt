package pw.mihou.amelia.io

import com.github.benmanes.caffeine.cache.Caffeine
import pw.mihou.Amaririsu
import pw.mihou.amelia.io.rome.FeedItem
import pw.mihou.amelia.logger
import pw.mihou.amelia.models.FeedModel
import pw.mihou.cache.Cacheable
import java.time.Duration

object Amatsuki {

     val cache = Caffeine.newBuilder()
         .expireAfterWrite(Duration.ofHours(24))
         .build<String, Cacheable>()

    private val BASE_STORY_URL: (Int) -> String = { id -> "https://scribblehub.com/series/$id/amelia-discord-bot/" }
    private val BASE_USER_URL: (Int) -> String = { id -> "https://scribblehub.com/profile/$id/amelia-discord-bot/" }

    fun authorFrom(item: FeedItem, feed: FeedModel): String {
        try {
            if (item.author != null) return item.author

            if (feed.feedUrl.contains("type=author")) {
                return Amaririsu.user(BASE_USER_URL.invoke(feed.id)).name
            }

            if (item.category != null) {
                return Amaririsu.series(BASE_STORY_URL.invoke(item.category)) {
                  includeTags = false
                  includeSynopsis = false
                  includeGenres = false
                }.author.name
            }

            return Amaririsu.series(BASE_STORY_URL.invoke(feed.id)) {
                includeTags = false
                includeSynopsis = false
                includeGenres = false
            }.author.name
        } catch (exception: Exception) {
            logger.error("Failed to propagate author from the ${feed.feedUrl} with related " +
                    "[id=${feed.id}, category=${item.category}], an exception was raised.", exception)
            throw exception
        }
    }

}