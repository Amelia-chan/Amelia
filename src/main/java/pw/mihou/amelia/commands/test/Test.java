package pw.mihou.amelia.commands.test;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.velen.interfaces.VelenEvent;

public class Test implements VelenEvent {

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
}
