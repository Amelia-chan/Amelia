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

import static pw.mihou.amelia.templates.TemplateMessages.*;

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
                                                                                    message.edit("✅ The test completed successfully, you can find the test results on <#"+ textChannel.getId()+">");
                                                                                }
                                                                            }), () -> message.edit(ERROR_DATE_NOT_FOUND)),
                                                                    () -> message.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE)),
                                                    () -> message.edit(ERROR_CHANNEL_NOT_FOUND))));
                } catch (NumberFormatException | ArithmeticException e) {
                    m.reply(ERROR_INT_ABOVE_LIMIT);
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
                                                                                            message.edit("✅ The test completed successfully, you can find the test results on <#"+ textChannel.getId()+">");
                                                                                        }
                                                                                    }), () -> message.edit(ERROR_DATE_NOT_FOUND)),
                                                            () -> message.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE)),
                                                    () -> message.edit(ERROR_CHANNEL_NOT_FOUND)),
                                    () -> message.edit(ERROR_FEED_NOT_FOUND))));
        }
    }
}
