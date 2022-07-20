package pw.mihou.amelia.commands

import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import pw.mihou.amelia.commands.middlewares.Middlewares
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.io.AmatsukiWrapper
import pw.mihou.amelia.io.rome.ReadRSS
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.amelia.utility.StringUtils
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.paginator.NexusPaginatorBuilder
import pw.mihou.nexus.features.paginator.enums.NexusPaginatorButtonAssignment
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorCursor
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorEvents
import tk.mihou.amatsuki.entities.story.lower.StoryResults
import tk.mihou.amatsuki.entities.user.lower.UserResults
import java.awt.Color

@Suppress("UNUSED")
object RegisterCommand : NexusHandler {

    private const val name = "register"
    private const val description = "Registers a feed from a user or a story."

    private val options = listOf(
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "user",
            "Creates a notification for all story updates of a given user from ScribbleHub.",
            listOf(
                SlashCommandOption.createStringOption("name", "The username of the author in ScribbleHub.", true),
                SlashCommandOption.createChannelOption(
                    "channel", "The channel to send updates for this feed.",
                    true, listOf(ChannelType.SERVER_TEXT_CHANNEL)
                )
            )
        ),
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "story",
            "Creates a notification for all updates of the given story from ScribbleHub.",
            listOf(
                SlashCommandOption.createStringOption("name", "The name of the story in ScribbleHub.", true),
                SlashCommandOption.createChannelOption(
                    "channel", "The channel to send updates for this feed.",
                    true, listOf(ChannelType.SERVER_TEXT_CHANNEL)
                )
            )
        )
    )

    private val middlewares = listOf(Middlewares.MODERATOR_ONLY)

    override fun onEvent(event: NexusCommandEvent) {
        val subcommand = event.options.first()
        val _name = subcommand.getOptionStringValueByName("name").orElseThrow()
        val channel = subcommand.getOptionChannelValueByName("channel").flatMap { it.asServerTextChannel() }.orElseThrow()

        if (!(channel.canYouSee() && channel.canYouWrite() && channel.canYouReadMessageHistory())) {
            event.respondNow().setContent(TemplateMessages.ERROR_CHANNEL_NOT_FOUND).respond()
            return
        }

        event.respondLater().thenAccept { updater ->
            if (subcommand.name == "user") {
                AmatsukiWrapper.getConnector().searchUser(_name).thenAccept connector@{ results ->
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
                                .replaceMessage()
                                .thenAccept update@{ message ->
                                    val id = cursor.item.transformToUser().join().uid
                                    val feed = "https://www.scribblehub.com/rssfeed.php?type=author&uid=$id"

                                    val latestPost = ReadRSS.getLatest(feed).orElse(null)

                                    if (latestPost == null) {
                                        message.edit(TemplateMessages.ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE)
                                        return@update
                                    }

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
                                }
                            cursor.parent().parent.destroy()
                        }
                    }).build().send(event.baseEvent.interaction, updater)
                }
                return@thenAccept
            }

            if (subcommand.name == "story") {
                AmatsukiWrapper.getConnector().searchStory(_name).thenAccept connector@{ results ->
                    if (results.isEmpty()) {
                        event.respondNow().setContent("❌ Amelia cannot found any stories that matches the query, how about trying something else?").respond()
                        return@connector
                    }

                    buttons(NexusPaginatorBuilder(results)).setEventHandler(object : NexusPaginatorEvents<StoryResults> {
                        override fun onInit(
                            updater: InteractionOriginalResponseUpdater,
                            cursor: NexusPaginatorCursor<StoryResults>
                        ) = updater.addEmbed(story(cursor))

                        override fun onPageChange(
                            cursor: NexusPaginatorCursor<StoryResults>,
                            event: ButtonClickEvent
                        ) {
                            event.buttonInteraction.message.edit(story(cursor))
                        }

                        override fun onCancel(cursor: NexusPaginatorCursor<StoryResults>?, event: ButtonClickEvent) {
                            event.buttonInteraction.message.delete()
                        }

                        override fun onSelect(
                            cursor: NexusPaginatorCursor<StoryResults>,
                            buttonEvent: ButtonClickEvent
                        ) {
                            buttonEvent.buttonInteraction.message.createUpdater()
                                .removeAllComponents()
                                .removeAllEmbeds()
                                .setContent(TemplateMessages.NEUTRAL_LOADING)
                                .replaceMessage()
                                .thenAccept update@{ message ->
                                    val id = cursor.item.transformToStory().join().sid
                                    val feed = "https://www.scribblehub.com/rssfeed.php?type=series&sid=$id"

                                    val latestPost = ReadRSS.getLatest(feed).orElse(null)

                                    if (latestPost == null) {
                                        message.edit(TemplateMessages.ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE)
                                        return@update
                                    }

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
                                            name = cursor.item.name,
                                            feedUrl = feed,
                                            mentions = emptyList(),
                                            server = event.server.orElseThrow().id
                                        )
                                    )

                                    if (result.wasAcknowledged()) {
                                        message.edit("✅ I will try my best to send updates for ${cursor.item.name} in ${channel.mentionTag}!")
                                        return@update
                                    }

                                    message.edit(TemplateMessages.ERROR_DATABASE_FAILED)
                                }
                            cursor.parent().parent.destroy()
                        }
                    }).build().send(event.baseEvent.interaction, updater)
                }
                return@thenAccept
            }
        }
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

    private fun story(cursor: NexusPaginatorCursor<StoryResults>) =
        EmbedBuilder().setTimestampToNow().setColor(Color.YELLOW).setTitle(cursor.item.name)
            .setDescription(
                "You can create a notification listener for this story by pressing the **Select** button below, "
                        + "please make sure that this is the correct story. "
                        + "If you need to look at their full story page to be sure, "
                        + "you may visit the link ${cursor.item.url}."
            )
            .addField("Synopsis", StringUtils.stripToLengthWhileDotsEnd(cursor.item.fullSynopsis, 512))
            .setFooter("You are looking at ${cursor.displayablePosition} out of ${cursor.maximumPages} pages")
            .setThumbnail(cursor.item.thumbnail)

}