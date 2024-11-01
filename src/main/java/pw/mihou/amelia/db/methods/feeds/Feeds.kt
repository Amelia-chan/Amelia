package pw.mihou.amelia.db.methods.feeds

import com.mongodb.client.model.Filters
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.interaction.SlashCommandInteractionOption
import pw.mihou.amelia.commands.templates.TemplateMessages
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.db.models.FeedModel
import pw.mihou.amelia.rss.reader.RssReader
import pw.mihou.models.user.UserResultOrAuthor

object Feeds {
    fun findFeedBySubcommand(
        server: Server,
        subcommand: SlashCommandInteractionOption,
    ): FeedModel? {
        var feed: FeedModel? = null

        if (subcommand.name == "id") {
            feed = FeedDatabase.get(subcommand.getArgumentLongValueByName("value").orElseThrow())

            if (feed != null && feed.server != server.id) {
                feed = null
            }
        }

        if (subcommand.name == "name") {
            feed =
                FeedDatabase.connection
                    .find(
                        Filters.and(
                            Filters.eq("server", server.id),
                            Filters.text(
                                subcommand.getArgumentStringValueByName("value").orElseThrow(),
                            ),
                        ),
                    ).map { FeedModel.from(it) }
                    .first()
        }
        return feed
    }

    fun tryRegisterUser(
        id: Int,
        channel: ServerTextChannel,
        user: User,
        selectedUser: UserResultOrAuthor,
    ): String {
        val feed = "https://www.rssscribblehub.com/rssfeed.php?type=author&uid=$id"

        val res =
            RssReader.cached(feed)
                ?: return TemplateMessages.ERROR_RSSSCRIBBLEHUB_NOT_ACCESSIBLE

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
