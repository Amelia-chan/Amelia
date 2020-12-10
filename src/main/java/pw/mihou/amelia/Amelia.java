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
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.amelia.listeners.BotJoinCommand;
import pw.mihou.amelia.listeners.BotLeaveListener;
import pw.mihou.amelia.templates.Message;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Amelia {

    private static final String token = System.getenv("amelia_token");

    public static void main(String[] args) {
        // Logger Setup.
        FallbackLoggerConfiguration.setTrace(true);

        // The DiscordAPI Builder.
        new DiscordApiBuilder()
                .setToken(token) // Logins with the bot token.
                .setAllIntentsExcept(Intent.GUILD_MESSAGE_TYPING, Intent.DIRECT_MESSAGE_TYPING, Intent.GUILD_INTEGRATIONS, Intent.GUILD_WEBHOOKS,
                        Intent.GUILD_BANS, Intent.GUILD_EMOJIS, Intent.GUILD_INVITES, Intent.GUILD_VOICE_STATES) // Excludes all the intents we won't be using for more performance.
                .setRecommendedTotalShards() // Asks Discord to set a recommended total shard for us, optional but preferred.
                .join() // Wait until Discord replies.
                .loginAllShards().forEach(shard -> shard.thenAccept(Amelia::onShardLogin).exceptionally(ExceptionLogger.get())); // After they reply, we then direct each shard to a onShardLogin.
    }

    private static void onShardLogin(DiscordApi api){

        /* Performance optimizations **/

        api.setAutomaticMessageCacheCleanupEnabled(true);
        api.setMessageCacheSize(10, 60 * 5);
        api.setReconnectDelay(attempt -> attempt * 2);
        FeedDB.preloadAllModels();
        api.updateActivity(ActivityType.WATCHING, "The bot is starting up...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Calls all the shutdowns needed.
            api.disconnect();
            MongoDB.shutdown();
            Scheduler.shutdown();
        }));
        System.out.println("-> Javacord Optimizations and Shutdown hook is now ready!");

        registerAllCommands(api);
        System.out.println("-> All commmands are now registered!");
        api.updateActivity(ActivityType.WATCHING, "People read stories!");
        System.out.println("-> The bot has started with everything in place!");
        Scheduler.schedule(() -> {
            FeedDB.retrieveAllModels().thenAccept(feedModels -> feedModels.forEach(feedModel -> {
                ReadRSS.getLatest(feedModel.getFeedURL()).ifPresentOrElse(syndEntry -> {
                    if(syndEntry.getPublishedDate().after(feedModel.getDate())){
                        api.getServerTextChannelById(feedModel.getChannel()).ifPresent(tc -> feedModel.setPublishedDate(syndEntry.getPublishedDate()).update(tc.getServer().getId()).thenAccept(unused -> Message.msg("\uD83D\uDCD6 **"+syndEntry.getTitle()+" by "+syndEntry.getAuthor()+".**" +
                                "\n"+syndEntry.getLink()+"\n\n"+getMentions(feedModel.getMentions(), tc.getServer())).send(tc)));
                    }
                }, () -> Logger.getLogger("Amelia-chan").log(Level.SEVERE, "We couldn't connect to ScribbleHub: " + feedModel.getFeedURL()));
                // Thread.sleep is here, so we don't overload ScribbleHub.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
            System.out.println("-> RSS feed deployment, complete.");
        }, 0, 10, TimeUnit.MINUTES);
    }

    private static String getMentions(ArrayList<Long> roles, Server server){
        StringBuilder builder = new StringBuilder();
        roles.forEach(aLong -> builder.append(server.getRoleById(aLong).map(Role::getMentionTag).orElse("[Vanished Role]")));
        return builder.toString();
    }

    private static void registerAllCommands(DiscordApi api){
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
