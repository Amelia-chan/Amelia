package pw.mihou.amelia.commands.subcommands

import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.interaction.SlashCommandInteractionOption
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.io.Amatsuki
import pw.mihou.amelia.io.rome.RssReader
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.paginator.NexusPaginatorBuilder
import pw.mihou.nexus.features.paginator.enums.NexusPaginatorButtonAssignment
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorCursor
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorEvents
import tk.mihou.amatsuki.entities.user.lower.UserResults
import java.awt.Color

object RegisterAuthorSubcommand {

    fun run(event: NexusCommandEvent, subcommand: SlashCommandInteractionOption) {
        val name = subcommand.getOptionStringValueByName("name").orElseThrow()
        val channel = subcommand.getOptionChannelValueByName("channel").flatMap { it.asServerTextChannel() }.orElseThrow()

        event.respondLater().thenAccept { updater ->
            if (subcommand.name == "user") {
                Amatsuki.connector.searchUser(name).thenAccept connector@{ results ->
                    if (results.isEmpty()) {
                        event.respondNow().setContent("❌ Amelia cannot found any users that matches the query, how about trying something else?").respond()
                        return@connector
                    }

                    buttons(NexusPaginatorBuilder(results)).setEventHandler(object : NexusPaginatorEvents<UserResults> {
                        override fun onInit(
                            updater: InteractionOriginalResponseUpdater,
                            cursor: NexusPaginatorCursor<UserResults>
                        ) = updater.addEmbed(user(cursor))

                        override fun onPageChange(
                            cursor: NexusPaginatorCursor<UserResults>,
                            event: ButtonClickEvent
                        ) {
                            event.buttonInteraction.message.edit(user(cursor))
                        }

                        override fun onCancel(cursor: NexusPaginatorCursor<UserResults>?, event: ButtonClickEvent) {
                            event.buttonInteraction.message.delete()
                        }

                        override fun onSelect(
                            cursor: NexusPaginatorCursor<UserResults>,
                            buttonEvent: ButtonClickEvent
                        ) {
                            buttonEvent.buttonInteraction.message.createUpdater()
                                .removeAllComponents()
                                .removeAllEmbeds()
                                .setContent(TemplateMessages.NEUTRAL_LOADING)
                                .applyChanges()
                                .thenAccept update@{ message ->
                                    val id = cursor.item.transformToUser().join().uid
                                    val feed = "https://www.scribblehub.com/rssfeed.php?type=author&uid=$id"

                                    val latestPosts = RssReader.cached(feed)

                                    if (latestPosts.isEmpty()) {
                                        message.edit(TemplateMessages.ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE)
                                        return@update
                                    }

                                    val latestPost = latestPosts[0]

                                    if (latestPost.date == null) {
                                        message.edit(TemplateMessages.ERROR_DATE_NOT_FOUND)
                                        return@update
                                    }

                                    val result = FeedDatabase.upsert(
                                        FeedModel(
                                            id = id,
                                            unique = FeedDatabase.unique(),
                                            channel = channel.id,
                                            user = event.user.id,
                                            date = latestPost.date,
                                            name = "${cursor.item.name}'s stories",
                                            feedUrl = feed,
                                            mentions = emptyList(),
                                            server = event.server.orElseThrow().id
                                        )
                                    )

                                    if (result.wasAcknowledged()) {
                                        message.edit("✅ I will try my best to send updates for ${cursor.item.name}'s stories in ${channel.mentionTag}!")
                                        return@update
                                    }

                                    message.edit(TemplateMessages.ERROR_DATABASE_FAILED)
                                }.exceptionally {
                                    it.printStackTrace()

                                    buttonEvent.buttonInteraction.message
                                        .edit(TemplateMessages.ERROR_FAILED_TO_PERFORM_ACTION)

                                    return@exceptionally null
                                }
                            cursor.parent().parent.destroy()
                        }
                    }).build().send(event.baseEvent.interaction, updater)
                }
                return@thenAccept
            }
        }.exceptionally(ExceptionLogger.get())
    }

    private fun <Type> buttons(paginator: NexusPaginatorBuilder<Type>): NexusPaginatorBuilder<Type> = paginator
        .setButton(NexusPaginatorButtonAssignment.SELECT, Button.primary("", "Select"))
        .setButton(NexusPaginatorButtonAssignment.PREVIOUS, Button.secondary("", "Previous"))
        .setButton(NexusPaginatorButtonAssignment.NEXT, Button.secondary("", "Next"))
        .setButton(NexusPaginatorButtonAssignment.CANCEL, Button.secondary("", "Cancel"))

    private fun user(cursor: NexusPaginatorCursor<UserResults>) =
        EmbedBuilder().setTimestampToNow().setColor(Color.YELLOW).setTitle(cursor.item.name)
            .setDescription(
                "You can create a notification listener for this user by pressing the **Select** button below, "
                        + "please make sure that this is the correct user. "
                        + "If you need to look at their full profile page to be sure, "
                        + "you may visit the link ${cursor.item.url}."
            )
            .setFooter("You are looking at ${cursor.displayablePosition} out of ${cursor.maximumPages} pages")
            .setImage(cursor.item.avatar)

}