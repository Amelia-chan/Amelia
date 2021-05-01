package pw.mihou.amelia.commands.removal;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.Limitations;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.templates.Message;

public class RemoveCommand extends Command {

    public RemoveCommand() {
        super("remove", "Removes a feed from the server, can only be done by the user who added the feed or a user with Manage Server permission.", "remove [feed id] [@channel]", true);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        if (args.length > 2) {
            if (!event.getMessage().getMentionedChannels().isEmpty()) {
                try {
                    int id = Integer.parseInt(args[1]);
                    ServerTextChannel channel = event.getMessage().getMentionedChannels().get(0);
                    if (id > 0) {
                        if (FeedDB.validate(id)) {
                            FeedDB.getServer(server.getId()).getChannel(channel.getId()).getFeedModel(id).ifPresentOrElse(feedModel -> {
                                    FeedDB.getServer(server.getId()).getChannel(channel.getId()).removeFeed(id);
                                    Message.msg("The feed has been removed.").send(event.getChannel());
                            }, () -> Message.msg("There is no feed with the id [" + id + "] located on the channel: " + channel.getMentionTag()
                                    + "\nPlease verify using `feeds` command.").send(event.getChannel()));
                        } else {
                            Message.msg("There is no feed with the id [" + id + "] located on the channel: " + channel.getMentionTag()
                                    + "\nPlease verify using `feeds` command.").send(event.getChannel());
                        }
                    } else {
                        Message.msg("Error: ID is below 0 which is not possible!").send(event.getChannel());
                    }
                } catch (ArithmeticException | NumberFormatException e) {
                    Message.msg("The value [" + args[1] + "] cannot be transformed into a integer, or number." +
                            "\nPlease try getting the identification number of the feed by using `feeds`").send(event.getChannel());
                }
            } else {
                Message.msg("Error: There is no channel mentioned.").send(event.getChannel());
            }
        } else {
            Message.msg("Error: Invalid usage [`remove [id] [@channel]`]").send(event.getChannel());
        }
    }
}
