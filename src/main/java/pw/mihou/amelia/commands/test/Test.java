package pw.mihou.amelia.commands.test;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.util.List;

public class Test implements VelenEvent, VelenSlashEvent {

    @Override
    public void onEvent(MessageCreateEvent event, Message m, User user, String[] args) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();
        if (event.getMessageAuthor().isServerAdmin() || server.canCreateChannels(user) || server.canManage(user) || server.canManageRoles(user)) {
            if (args.length > 0) {
                try {
                    long id = Long.parseLong(args[0]);
                    m.reply(NEUTRAL_LOADING)
                            .thenAccept(message -> FeedDB.getServer(server.getId())
                                    .getFeedModel(id).ifPresent(feedModel -> server.getTextChannelById(feedModel.getChannel())
                                            .ifPresentOrElse(textChannel -> ReadRSS.getLatest(feedModel.getFeedURL())
                                                            .ifPresentOrElse(itemWrapper -> itemWrapper.getPubDate()
                                                                    .ifPresentOrElse(date -> pw.mihou.amelia.templates.Message.msg(Amelia.format(itemWrapper, feedModel, textChannel.getServer()))
                                                                            .send(textChannel).whenComplete((msg, throwable) -> {
                                                                                if (throwable != null) {
                                                                                    message.edit("❌ An exception was thrown, is it possible that I can't **write on the channel?**" +
                                                                                            "\n```java\n" + throwable.getMessage()+ "\n```");
                                                                                } else {
                                                                                    message.edit("✔ The test completed successfully, you can find the test results on <#"+ textChannel.getId()+">");
                                                                                }
                                                                            }), () -> message.edit(ERROR_DATE_NOT_FOUND)),
                                                                    () -> message.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE)),
                                                    () -> message.edit(ERROR_CHANNEL_NOT_FOUND))));
                } catch (NumberFormatException | ArithmeticException e) {
                    m.reply("❌ The feed number provided is not valid, please use `feeds` command to find the correct feed!");
                }
            } else {
                m.reply("❌ Missing arguments: `[feed id]`.");
            }
        }
    }

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();
        int feed = event.getOptionIntValueByName("feed").orElseThrow();

        if (server.isAdmin(user) || server.canCreateChannels(user) || server.canManage(user) || server.canManageRoles(user)) {
            event.respondLater().thenAccept(updater -> updater.setContent(NEUTRAL_LOADING).update()
                    .thenAccept(message -> FeedDB.getServer(server.getId())
                            .getFeedModel(feed).ifPresentOrElse(feedModel -> server.getTextChannelById(feedModel.getChannel())
                                            .ifPresentOrElse(textChannel -> ReadRSS.getLatest(feedModel.getFeedURL()).ifPresentOrElse(itemWrapper -> itemWrapper.getPubDate()
                                                                    .ifPresentOrElse(date -> pw.mihou.amelia.templates.Message.msg(Amelia.format(itemWrapper, feedModel, server)).send(textChannel)
                                                                                    .whenComplete((m, throwable) -> {
                                                                                        if (throwable != null) {
                                                                                            message.edit("❌ An exception was thrown, is it possible that I can't **write on the channel?**" +
                                                                                                    "\n```java\n" + throwable.getMessage()+ "\n```");
                                                                                        } else {
                                                                                            message.edit("✔ The test completed successfully, you can find the test results on <#"+ textChannel.getId()+">");
                                                                                        }
                                                                                    }), () -> message.edit(ERROR_DATE_NOT_FOUND)),
                                                            () -> message.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE)),
                                                    () -> message.edit(ERROR_CHANNEL_NOT_FOUND)),
                                    () -> message.edit(ERROR_FEED_NOT_FOUND))));
        }
    }

    private static final String NEUTRAL_LOADING = "<a:manaWinterLoading:880162110947094628> Please wait...";
    private static final String ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE = "❌ Amelia was unable to fetch the RSS feed from ScribbleHub, is it down?";
    private static final String ERROR_CHANNEL_NOT_FOUND = "❌ Amelia was unable to find the text channel, are you sure that I can **see**, **write** and **read** on the channel?";
    private static final String ERROR_FEED_NOT_FOUND = "❌ We were unable to find the feed, are you sure it exists?";
    private static final String ERROR_DATE_NOT_FOUND = "❌ Amelia was unable to fetch the date of the feed, please try contacting our support team if it still doesn't work at https://manabot.fun/support";
}
