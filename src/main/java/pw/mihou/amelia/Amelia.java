package pw.mihou.amelia;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;
import pw.mihou.amelia.commands.creation.RegisterCommand;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.commands.db.MessageDB;
import pw.mihou.amelia.commands.feeds.FeedsCommand;
import pw.mihou.amelia.commands.feeds.SubscribeCommand;
import pw.mihou.amelia.commands.feeds.UnsubscribeCommand;
import pw.mihou.amelia.commands.invite.InviteCommand;
import pw.mihou.amelia.commands.miscellanous.PingCommand;
import pw.mihou.amelia.commands.removal.RemoveCommand;
import pw.mihou.amelia.commands.settings.Settings;
import pw.mihou.amelia.commands.support.HelpCommand;
import pw.mihou.amelia.commands.test.TestCommand;
import pw.mihou.amelia.db.MongoDB;
import pw.mihou.amelia.io.Scheduler;
import pw.mihou.amelia.io.StoryHandler;
import pw.mihou.amelia.io.Terminal;
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.amelia.listeners.BotJoinCommand;
import pw.mihou.amelia.listeners.BotLeaveListener;
import pw.mihou.amelia.templates.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Amelia {

    private static final String token = System.getenv("amelia_token");
    private static final HashMap<Integer, DiscordApi> shards = new HashMap<>();

    public static void main(String[] args) {
        // Logger Setup.
        FallbackLoggerConfiguration.setTrace(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shards.values().forEach(DiscordApi::disconnect);
            MongoDB.shutdown();
            Scheduler.shutdown();
        }));

        // The DiscordAPI Builder.
        new DiscordApiBuilder()
                .setToken(token) // Logins with the bot token.
                .setAllIntentsExcept(Intent.GUILD_MESSAGE_TYPING, Intent.DIRECT_MESSAGE_TYPING, Intent.GUILD_INTEGRATIONS, Intent.GUILD_WEBHOOKS,
                        Intent.GUILD_BANS, Intent.GUILD_EMOJIS, Intent.GUILD_INVITES, Intent.GUILD_VOICE_STATES) // Excludes all the intents we won't be using for more performance.
                .setRecommendedTotalShards() // Asks Discord to set a recommended total shard for us, optional but preferred.
                .join() // Wait until Discord replies.
                .loginAllShards().forEach(shard -> shard.thenAccept(Amelia::onShardLogin).exceptionally(ExceptionLogger.get())); // After they reply, we then direct each shard to a onShardLogin.
    }

    private static int determineNextTarget() {
        return LocalDateTime.now().getMinute() % 10 != 0 ? (LocalDateTime.now().getMinute() + (10 - LocalDateTime.now().getMinute() % 10)) - LocalDateTime.now().getMinute() : 0;
    }

    private static void onShardLogin(DiscordApi api) {

        shards.put(api.getCurrentShard(), api);

        /* Performance optimizations **/
        api.setAutomaticMessageCacheCleanupEnabled(true);
        api.setMessageCacheSize(10, 1);
        api.setReconnectDelay(attempt -> attempt * 2);
        FeedDB.preloadAllModels();
        api.updateActivity(ActivityType.WATCHING, "The bot is starting up...");
        Terminal.log("Javacord Optimizations and Shutdown hook is now ready!");

        registerAllCommands(api);
        Terminal.log("All commands are now registered.");
        api.updateActivity(ActivityType.WATCHING, "People read stories!");
        Terminal.log("The bot has started!");
        int initial = determineNextTarget();
        Terminal.log("The scheduler will be delayed for " + initial + " minutes for synchronization.");
        Scheduler.schedule(() -> FeedDB.retrieveAllModels().thenAccept(feedModels -> feedModels.forEach(feedModel -> {
            // We want them all to be executed in different threads to speed up everything.
            CompletableFuture.runAsync(() -> ReadRSS.getLatest(feedModel.getFeedURL()).ifPresentOrElse(syndEntry -> {
                if (syndEntry.getPublishedDate().after(feedModel.getDate())) {
                    api.getServerTextChannelById(feedModel.getChannel()).ifPresent(tc -> {
                        feedModel.setPublishedDate(syndEntry.getPublishedDate()).update(tc.getServer().getId()).thenAccept(unused ->
                                Message.msg(MessageDB.getFormat(tc.getServer().getId())
                                        .replaceAll("\\{title}", syndEntry.getTitle())
                                        .replaceAll("\\{author}", StoryHandler.getAuthor(syndEntry.getAuthor(), feedModel.getId()))
                                        .replaceAll("\\{link}", syndEntry.getLink())
                                        .replaceAll("\\{subscribed}", getMentions(feedModel.getMentions(), tc.getServer()))).send(tc));
                        System.out.printf("[%s]: RSS feed deployed for: %s with feed id: [%d]\n", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()), tc.getServer().getName(), feedModel.getUnique());
                    });
                }
            }, () -> Logger.getLogger("Amelia-chan").log(Level.SEVERE, "We couldn't connect to ScribbleHub: " + feedModel.getFeedURL())), Scheduler.getExecutorService());
        })), initial, 10, TimeUnit.MINUTES);
    }

    private static String getMentions(ArrayList<Long> roles, Server server) {
        StringBuilder builder = new StringBuilder();
        roles.forEach(aLong -> builder.append(server.getRoleById(aLong).map(Role::getMentionTag).orElse("[Vanished Role]")));
        return builder.toString();
    }

    private static void registerAllCommands(DiscordApi api) {
        api.addListener(new HelpCommand());
        api.addListener(new RegisterCommand());
        api.addListener(new FeedsCommand());
        api.addListener(new RemoveCommand());
        api.addListener(new SubscribeCommand());
        api.addListener(new Settings());
        api.addListener(new UnsubscribeCommand());
        api.addListener(new InviteCommand());
        api.addListener(new BotJoinCommand());
        api.addListener(new BotLeaveListener());
        api.addListener(new TestCommand());
        api.addListener(new PingCommand());
    }

}
