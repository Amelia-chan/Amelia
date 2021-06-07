package pw.mihou.amelia.commands.test;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.amelia.templates.Message;

import java.util.ArrayList;

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
                                .thenAccept(message -> {
                                    FeedDB.getServer(server.getId())
                                            .getFeedModel(id)
                                            .ifPresentOrElse(feedModel -> {
                                                server.getTextChannelById(feedModel.getChannel()).ifPresentOrElse(tc -> {
                                                    ReadRSS.getLatest(feedModel.getFeedURL()).ifPresentOrElse(item ->
                                                            item.getPubDate().ifPresentOrElse(date -> {
                                                                    if (date.after(feedModel.getDate())) {
                                                                        Message.msg(Amelia.format(item, feedModel, tc.getServer())).send(tc)
                                                                                .whenComplete((msg, throwable) -> {
                                                                                    if(throwable != null){
                                                                                        message.edit("An exception was thrown, is it possible that the bot cannot write on the channel?");
                                                                                    } else {
                                                                                        message.edit("Amelia was successfully able to deliver the feed!");
                                                                                    }
                                                                                });
                                                                    }
                                                            }, () -> message.edit("We were unable to fetch the date of the feeds...")),
                                                            () -> message.edit("Amelia was not able to retrieve the RSS feed from ScribbleHub..."));
                                                }, () -> message.edit("We were unable to find the text channel ("+feedModel.getChannel()+"), please verify it exists and the bot can see and write on it!"));
                                            }, () -> message.edit("We were unable to find the feed, are you sure you are using the feed's unique id?\nPlease verify using `feeds` command."));
                                });
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

    private String getMentions(ArrayList<Long> roles, Server server) {
        StringBuilder builder = new StringBuilder();
        roles.forEach(aLong -> builder.append(server.getRoleById(aLong).map(Role::getMentionTag).orElse("[Vanished Role]")));
        return builder.toString();
    }
}
