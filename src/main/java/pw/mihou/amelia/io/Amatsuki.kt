package pw.mihou.amelia.io

import pw.mihou.amelia.io.rome.ItemWrapper
import pw.mihou.amelia.logger
import pw.mihou.amelia.models.FeedModel
import tk.mihou.amatsuki.api.Amatsuki
import java.util.concurrent.TimeUnit

object Amatsuki {

    val connector = Amatsuki()
        .setLifespan(24, TimeUnit.HOURS)
        .setUserAgent("Amelia/2.0.0-luminous (Language=Kotlin/1.7.10)")
        .setCache(true)

    private val BASE_STORY_URL: (Int) -> String = { id -> "https://scribblehub.com/series/$id/amelia-discord-bot/" }
    private val BASE_USER_URL: (Int) -> String = { id -> "https://scribblehub.com/profile/$id/amelia-discord-bot/" }

    fun authorFrom(item: ItemWrapper, feed: FeedModel): String {
        try {
            if (item.author.isNotEmpty()) return item.author

            if (feed.feedUrl.contains("type=author")) {
                return connector.getUserFromUrl(BASE_USER_URL.invoke(feed.id)).join().name
            }

            return connector.getStoryFromUrl(BASE_STORY_URL.invoke(feed.id)).join().creator
        } catch (exception: Exception) {
            logger.error("Failed to propagate author from the ${feed.feedUrl}, an exception was raised.", exception)
            throw exception
        }
    }

}