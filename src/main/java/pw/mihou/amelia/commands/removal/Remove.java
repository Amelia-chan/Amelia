package pw.mihou.amelia.commands.removal;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.commands.Limitations;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.util.List;

import static pw.mihou.amelia.templates.TemplateMessages.*;

public class Remove implements VelenEvent, VelenSlashEvent {

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
                        event.getMessage().reply(SUCCESS);
                    } else {
                        event.getMessage().reply(ERROR_FEED_NOT_FOUND);
                    }
                } else {
                    event.getMessage().reply(ERROR_INT_BELOW_ZERO);
                }
            } catch (ArithmeticException | NumberFormatException e) {
                event.getMessage().reply(ERROR_INT_ABOVE_LIMIT);
            }
        }
    }

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();
        int id = event.getOptionIntValueByName("feed").orElseThrow();

        if(!Limitations.isLimited(server, user)) {
            firstResponder.setContent(ERROR_MISSING_PERMISSIONS).setFlags(MessageFlag.EPHEMERAL).respond();
            return;
        }

        event.respondLater().thenAccept(updater -> FeedDB.getServer(server.getId()).getFeedModel(id).ifPresentOrElse(feedModel -> {
            FeedDB.getServer(server.getId()).removeFeed(id);
            updater.setContent(SUCCESS).update();
        }, () -> updater.setContent(ERROR_FEED_NOT_FOUND).update()));
    }

    private static final String SUCCESS = "✅ The feed has been removed!";
}
