package pw.mihou.amelia.commands.removal;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.velen.interfaces.VelenEvent;

public class Remove implements VelenEvent {

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();

        if (args.length > 0) {
            try {
                int id = Integer.parseInt(args[0]);
                if (id > 0) {
                    if (FeedDB.validate(id)) {
                        FeedDB.getServer(server.getId()).removeFeed(id);
                        event.getMessage().reply("The feed has been removed.");
                    } else {
                        event.getMessage().reply("**ERROR**: There is no feed with the **unique** identification " +
                                "`[" + id + "]`. \nPlease verify using `feeds` command.");
                    }
                } else {
                    event.getMessage().reply("**ERROR**: ID is below 0 which is not possible!");
                }
            } catch (ArithmeticException | NumberFormatException e) {
                event.getMessage().reply("**ERROR**: The value [" + args[0] + "] cannot be transformed into a integer, or number." +
                        "\nPlease try getting the identification number of the feed by using `feeds`");
            }
        }
    }
}
