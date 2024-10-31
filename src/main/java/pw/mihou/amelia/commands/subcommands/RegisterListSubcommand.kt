package pw.mihou.amelia.commands.subcommands

import org.javacord.api.interaction.SlashCommandInteractionOption
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.db.models.FeedModel
import pw.mihou.amelia.extensions.params
import pw.mihou.amelia.extensions.toUrl
import pw.mihou.amelia.rss.reader.RssReader
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.nexus.features.command.facade.NexusCommandEvent

object RegisterListSubcommand {
    fun run(
        event: NexusCommandEvent,
        subcommand: SlashCommandInteractionOption,
    ) {
        val name = subcommand.getArgumentStringValueByName("name").orElseThrow()
        val channel =
            subcommand
                .getArgumentChannelValueByName("channel")
                .flatMap {
                    it.asServerTextChannel()
                }.orElseThrow()

        event
            .respondLaterEphemerally()
            .thenAccept { updater ->
                try {
                    val link = subcommand.getArgumentStringValueByName("link").orElseThrow().toUrl()
                    val queries = link.params

                    val host = link.host.removePrefix("www.")
                    val type = queries["type"]
                    val uid = queries["uid"]?.toIntOrNull()
                    val unq = queries["unq"]
                    val lid = queries["lid"]?.toIntOrNull()

                    if (
                        !(
                            host.equals("scribblehub.com", ignoreCase = true) ||
                                host.equals("rssscribblehub.com", ignoreCase = true)
                        ) ||
                        !link.path.equals("/rssfeed.php", ignoreCase = true) ||
                        (type == null || uid == null || unq == null) ||
                        !(
                            type.equals("global", ignoreCase = true) ||
                                type.equals("local", ignoreCase = true)
                        )
                    ) {
                        updater
                            .setContent(
                                TemplateMessages.ERROR_INVALID_READING_LIST_LINK,
                            ).update()
                        return@thenAccept
                    }

                    if (type.equals("local", ignoreCase = true) && lid == null) {
                        updater
                            .setContent(
                                TemplateMessages.ERROR_INVALID_READING_LIST_LINK,
                            ).update()
                        return@thenAccept
                    }

                    var newLink = "https://www.rssscribblehub.com/rssfeed.php?type=$type&uid=$uid&unq=$unq"

                    if (type.equals("local", ignoreCase = true)) {
                        newLink += "&lid=$lid"
                    }

                    val res = RssReader.cached(newLink)

                    if (res == null) {
                        updater
                            .setContent(
                                "❌ Amelia encountered a problem while trying to send: ScribbleHub is not accessible.",
                            ).update()
                        return@thenAccept
                    }

                    val (_, latestPosts) = res

                    if (latestPosts.isEmpty()) {
                        updater
                            .setContent(
                                TemplateMessages.ERROR_RSSSCRIBBLEHUB_NOT_ACCESSIBLE,
                            ).update()
                        return@thenAccept
                    }

                    val latestPost = latestPosts[0]

                    if (latestPost.date == null) {
                        updater.setContent(TemplateMessages.ERROR_DATE_NOT_FOUND).update()
                        return@thenAccept
                    }

                    val result =
                        FeedDatabase.upsert(
                            FeedModel(
                                id = uid,
                                unique = FeedDatabase.unique(),
                                channel = channel.id,
                                user = event.user.id,
                                date = latestPost.date,
                                name = name,
                                feedUrl = newLink,
                                mentions = emptyList(),
                                server = event.server.orElseThrow().id,
                            ),
                        )

                    if (result.wasAcknowledged()) {
                        updater
                            .setContent(
                                "✅ I will try my best to send updates for the reading list ($name) in ${channel.mentionTag}!",
                            ).update()
                        return@thenAccept
                    }

                    updater.setContent(TemplateMessages.ERROR_DATABASE_FAILED).update()
                } catch (exception: Exception) {
                    updater.setContent(TemplateMessages.ERROR_INVALID_READING_LIST_LINK).update()
                }
                return@thenAccept
            }.exceptionally(ExceptionLogger.get())
    }
}
