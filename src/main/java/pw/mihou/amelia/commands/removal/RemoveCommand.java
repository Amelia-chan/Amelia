package pw.mihou.amelia.commands.removal;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.db.FeedDB;

public class RemoveCommand extends Command {

    public RemoveCommand() {
        super("remove", "Removes a feed from the server, can only be done by the user who added the feed or a user with Manage Server permission.", "remove [feed id]", true);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        if (args.length > 1) {
            try {
                int id = Integer.parseInt(args[1]);
                if (id > 0) {
                    if (FeedDB.validate(id)) {
                        FeedDB.getServer(server.getId()).removeFeed(id);
                        event.getMessage().reply("The feed has been removed.");
                    } else {
                        event.getMessage().reply("There is no feed with the **unique** identification `[" + id + "]`. \nPlease verify using `feeds` command.");
                    }
                } else {
                    event.getMessage().reply("Error: ID is below 0 which is not possible!");
                }
            } catch (ArithmeticException | NumberFormatException e) {
                event.getMessage().reply("The value [" + args[1] + "] cannot be transformed into a integer, or number." +
                        "\nPlease try getting the identification number of the feed by using `feeds`");
            }
        } else {
            event.getMessage().reply("Error: Invalid usage [`remove [id]`]");
        }
    }
}
