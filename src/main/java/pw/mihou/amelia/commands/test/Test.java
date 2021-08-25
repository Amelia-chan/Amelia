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
                    if (FeedDB.validate(id)) {
                        m.reply("Attempting to perform test fetch...")
                                .thenAccept(message -> FeedDB.getServer(server.getId())
                                        .getFeedModel(id).ifPresent(feedModel -> server.getTextChannelById(feedModel.getChannel())
                                                .ifPresentOrElse(textChannel -> ReadRSS.getLatest(feedModel.getFeedURL())
                                                                .ifPresentOrElse(itemWrapper -> itemWrapper.getPubDate()
                                                                        .ifPresentOrElse(date ->
                                                                                pw.mihou.amelia.templates.Message.msg(Amelia.format(itemWrapper, feedModel, textChannel.getServer())).send(textChannel)
                                                                                        .whenComplete((msg, throwable) -> {
                                                                                            if (throwable != null) {
                                                                                                message.edit("An exception was thrown, is it possible that the bot cannot write on the channel?");
                                                                                                message.reply("Exception: \n```" + throwable.getMessage() + "```");
                                                                                            } else {
                                                                                                message.edit("Amelia was able to deliver the feed!");
                                                                                            }
                                                                                        }), () -> message.edit("We were unable to fetch the date of the feeds...")), () -> message.edit("Amelia was not able to retrieve the RSS feed from ScribbleHub...")),
                                                        () -> message.edit("We were unable to find the text channel (" + feedModel.getChannel() + "), " +
                                                                "please verify it exists and the bot can see and write on it!"))));
                    } else {
                        m.reply("Error: We couldn't find the feed, are you sure you are using the feed's unique id." +
                                "\nPlease verify using `feeds`");
                    }
                } catch (NumberFormatException | ArithmeticException e) {
                    m.reply("Error: Number format exception, or arithmetic exception.");
                }
            } else {
                m.reply("Error: Lacking arguments [feed id]");
            }
        }
    }

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent,
                        SlashCommandInteraction event,
                        User user,
                        VelenArguments velenArguments,
                        List<SlashCommandInteractionOption> list,
                        InteractionImmediateResponseBuilder interactionImmediateResponseBuilder) {
        if (event.getServer().isEmpty()) {
            return;
        }

        int feedId = event.getOptionIntValueByName("feedId").orElseThrow();
        Server server = event.getServer().get();

        if (server.isAdmin(user) || server.canCreateChannels(user) || server.canManage(user) || server.canManageRoles(user)) {
            if (!list.isEmpty()) {
                try {
                    if (FeedDB.validate(feedId)) {
                        event.respondLater().thenAccept(updater -> {
                            updater.setContent("Attempting to perform test fetch...")
                                    .update()
                                    .thenAccept(message -> FeedDB.getServer(server.getId())
                                            .getFeedModel(feedId).ifPresent(feedModel -> server.getTextChannelById(feedModel.getChannel())
                                                    .ifPresentOrElse(textChannel -> ReadRSS.getLatest(feedModel.getFeedURL())
                                                                    .ifPresentOrElse(itemWrapper -> itemWrapper.getPubDate()
                                                                            .ifPresentOrElse(date ->
                                                                                    pw.mihou.amelia.templates.Message.msg(Amelia.format(itemWrapper, feedModel, textChannel.getServer())).send(textChannel)
                                                                                            .whenComplete((msg, throwable) -> {
                                                                                                if (throwable != null) {
                                                                                                    message.edit("An exception was thrown, is it possible that the bot cannot write on the channel?");
                                                                                                    message.reply("Exception: \n```" + throwable.getMessage() + "```");
                                                                                                } else {
                                                                                                    message.edit("Amelia was able to deliver the feed!");
                                                                                                }
                                                                                            }), () -> message.edit("We were unable to fetch the date of the feeds...")), () -> message.edit("Amelia was not able to retrieve the RSS feed from ScribbleHub...")),
                                                            () -> message.edit("We were unable to find the text channel (" + feedModel.getChannel() + "), " +
                                                                    "please verify it exists and the bot can see and write on it!"))));
                        });
                    } else {
                        event.respondLater().thenAccept(updater -> updater.setContent("Error: We couldn't find the feed, are you sure you are using the feed's unique id." +
                                "\nPlease verify using `feeds`").update());
                    }
                } catch (NumberFormatException | ArithmeticException e) {
                    event.respondLater().thenAccept(updater -> updater.setContent("Error: Number format exception, or arithmetic exception."));
                }
            } else {
                event.respondLater().thenAccept(updater -> updater.setContent("Error: Lacking arguments [feed id]").update());
            }
        }
    }
}
