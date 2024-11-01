package pw.mihou.amelia.feeds

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.user.User
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.db.models.FeedModel
import pw.mihou.amelia.rss.reader.RssReader
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.models.user.UserResultOrAuthor

object Feeds {
    fun tryRegisterUser(
        id: Int,
        channel: ServerTextChannel,
        user: User,
        selectedUser: UserResultOrAuthor,
    ): String {
        val feed = "https://www.rssscribblehub.com/rssfeed.php?type=author&uid=$id"

        val res =
            RssReader.cached(feed)
                ?: return "❌ Amelia encountered a problem while trying to send: ScribbleHub is" +
                    " not accessible."

        val (_, latestPosts) = res

        if (latestPosts.isEmpty()) {
            return TemplateMessages.ERROR_RSSSCRIBBLEHUB_NOT_ACCESSIBLE
        }

        val latestPost = latestPosts[0]

        if (latestPost.date == null) {
            return TemplateMessages.ERROR_DATE_NOT_FOUND
        }

        val result =
            FeedDatabase.upsert(
                FeedModel(
                    id = id,
                    unique = FeedDatabase.unique(),
                    channel = channel.id,
                    user = user.id,
                    date = latestPost.date,
                    name = "${selectedUser.name}'s stories",
                    feedUrl = feed,
                    mentions = emptyList(),
                    server = channel.server.id,
                ),
            )

        if (result.wasAcknowledged()) {
            return "✅ I will try my best to send updates for ${selectedUser.name}'s stories in ${channel.mentionTag}!"
        }

        return TemplateMessages.ERROR_DATABASE_FAILED
    }
}
