package pw.mihou.amelia.commands.test;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.amelia.templates.Message;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "Test runs a feed.", "test", true);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        // Add more security.
        if (event.getMessageAuthor().isServerAdmin() || server.canCreateChannels(user) || server.canManage(user) || server.canManageRoles(user)) {
            if (args.length > 1) {
                try {
                    long id = Long.parseLong(args[1]);
                    if (FeedDB.validate(id)) {
                        event.getMessage().reply("Attempting to perform test fetch...")
                                .thenAccept(message -> FeedDB.getServer(server.getId())
                                .getFeedModel(id).ifPresent(feedModel -> server.getTextChannelById(feedModel.getChannel())
                                        .ifPresentOrElse(textChannel -> ReadRSS.getLatest(feedModel.getFeedURL())
                                                .ifPresentOrElse(itemWrapper -> itemWrapper.getPubDate()
                                                        .ifPresentOrElse(date ->
                                                                Message.msg(Amelia.format(itemWrapper, feedModel, textChannel.getServer())).send(textChannel)
                                                                        .whenComplete((msg, throwable) -> {
                                                                            if(throwable != null) {
                                                                                message.edit("An exception was thrown, is it possible that the bot cannot write on the channel?");
                                                                                message.reply("Exception: \n```"+throwable.getMessage()+"```");
                                                                            } else {
                                                                                message.edit("Amelia was able to deliver the feed!");
                                                                            }
                                                                        }), () -> message.edit("We were unable to fetch the date of the feeds...")), () -> message.edit("Amelia was not able to retrieve the RSS feed from ScribbleHub...")),
                                                () -> message.edit("We were unable to find the text channel ("+feedModel.getChannel()+"), " +
                                                        "please verify it exists and the bot can see and write on it!"))));
                    } else {
                        event.getMessage().reply("Error: We couldn't find the feed, are you sure you are using the feed's unique id." +
                                "\nPlease verify using `feeds`");
                    }
                } catch (NumberFormatException | ArithmeticException e) {
                    event.getMessage().reply("Error: Number format exception, or arithmetic exception.");
                }
            } else {
                event.getMessage().reply("Error: Lacking arguments [feed id]");
            }
        }
    }
}
