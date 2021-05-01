package pw.mihou.amelia.commands.base;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pw.mihou.amelia.commands.Limitations;
import pw.mihou.amelia.commands.base.db.PrefixManager;
import pw.mihou.amelia.commands.base.info.Commands;
import pw.mihou.amelia.templates.Message;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public abstract class Command implements MessageCreateListener {

    private final String command;
    private final long cooldown = 5L;
    private final HashMap<Long, HashMap<Long, Long>> userCooldowns = new HashMap<>();
    private final boolean limited;

    protected Command(String command, String description, String usage, boolean limited) {
        this.command = command;
        Commands.addCommand(command, description, usage, 5L);
        this.limited = limited;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        // We do not allow non-server messages (private messages, or group messages).
        if (!event.isServerMessage())
            return;

        // We don't want any messages from bots, since it makes sense not to.
        if (!event.getMessageAuthor().isRegularUser())
            return;

        event.getServer().ifPresent(server -> {
            if (event.getMessageContent().startsWith(PrefixManager.prefix(server.getId()) + command)) {

                // Checks if the user is in cooldown and the data inside the cooldown isn't null.
                if (userCooldowns.containsKey(event.getMessageAuthor().getId()) &&

                        userCooldowns.get(event.getMessageAuthor().getId()).get(server.getId()) != null) {
                    long secondsLeft = ((
                            userCooldowns.get(event.getMessageAuthor().getId()).get(event.getServer().get().getId()) / 1000) + cooldown) - (System.currentTimeMillis() / 1000);
                    if (secondsLeft > 0) {
                        Message.msg("This command is still under cooldown for " + secondsLeft + " seconds!").send(event.getChannel()).thenAccept(message -> message.addReactionAddListener(e -> {
                            // No purpose at all.
                        }).removeAfter(secondsLeft, TimeUnit.SECONDS).addRemoveHandler(() -> {
                            message.delete("Cooldown off.");
                            event.getMessage().delete("Cleanliness matters.");
                        }));
                        return;
                    }
                }

                // Feel free to improve this one, but all it does is store the data back to the Map.
                HashMap<Long, Long> n = new HashMap<>();
                n.put(server.getId(), System.currentTimeMillis());
                userCooldowns.put(event.getMessageAuthor().getId(), n);

                event.getMessageAuthor().asUser().ifPresent(user -> {

                    if (limited) {
                        if (!Limitations.isLimited(server, user)) {
                            Message.msg("You do not have permission to use this command, required permission: Manage Server, or lacking the required role to modify feeds.").send(event.getChannel());
                            return;
                        }
                    }

                    runCommand(event, user, server, event.getMessageContent().split(" "));
                });
            }
        });
    }

    protected abstract void runCommand(MessageCreateEvent event, User user, Server server, String[] args);


}
