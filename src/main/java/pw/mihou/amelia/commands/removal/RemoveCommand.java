package pw.mihou.amelia.commands.removal;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.db.FeedDB;

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
                                    event.getMessage().reply("The feed has been removed.");
                            }, () -> event.getMessage().reply("There is no feed with the id [" + id + "] located on the channel: " + channel.getMentionTag()
                                    + "\nPlease verify using `feeds` command."));
                        } else {
                            event.getMessage().reply("There is no feed with the id [" + id + "] located on the channel: " + channel.getMentionTag()
                                    + "\nPlease verify using `feeds` command.");
                        }
                    } else {
                        event.getMessage().reply("Error: ID is below 0 which is not possible!");
                    }
                } catch (ArithmeticException | NumberFormatException e) {
                    event.getMessage().reply("The value [" + args[1] + "] cannot be transformed into a integer, or number." +
                            "\nPlease try getting the identification number of the feed by using `feeds`");
                }
            } else {
                event.getMessage().reply("Error: There is no channel mentioned.");
            }
        } else {
            event.getMessage().reply("Error: Invalid usage [`remove [id] [@channel]`]");
        }
    }
}
