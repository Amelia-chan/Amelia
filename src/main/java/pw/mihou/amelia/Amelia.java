package pw.mihou.amelia;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.logging.ExceptionLogger;
import org.slf4j.LoggerFactory;
import pw.mihou.amelia.clients.ClientHandler;
import pw.mihou.amelia.commands.creation.RegisterCommand;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.commands.db.MessageDB;
import pw.mihou.amelia.commands.feeds.FeedsCommand;
import pw.mihou.amelia.commands.feeds.SubscribeCommand;
import pw.mihou.amelia.commands.feeds.UnsubscribeCommand;
import pw.mihou.amelia.commands.invite.InviteCommand;
import pw.mihou.amelia.commands.miscellanous.PingCommand;
import pw.mihou.amelia.commands.notifier.AuthorCommand;
import pw.mihou.amelia.commands.notifier.IAmCommand;
import pw.mihou.amelia.commands.removal.RemoveCommand;
import pw.mihou.amelia.commands.settings.Settings;
import pw.mihou.amelia.commands.support.HelpCommand;
import pw.mihou.amelia.commands.test.TestCommand;
import pw.mihou.amelia.db.MongoDB;
import pw.mihou.amelia.db.UserDB;
import pw.mihou.amelia.io.Scheduler;
import pw.mihou.amelia.io.StoryHandler;
import pw.mihou.amelia.io.rome.ItemWrapper;
import pw.mihou.amelia.listeners.BotJoinCommand;
import pw.mihou.amelia.listeners.BotLeaveListener;
import pw.mihou.amelia.models.FeedModel;
import pw.mihou.amelia.utility.ColorPalette;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class Amelia {

    public static final Logger log = (Logger) LoggerFactory.getLogger("Amelia Client");
    private static final String token = System.getenv("amelia_token");
    public static final SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss");
    public static final HashMap<Integer, DiscordApi> shards = new HashMap<>();
    private static final String version = "1.5";
    private static final String build = "BETA";
    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    public static boolean connected = false;

    static {
        log.setLevel(Level.DEBUG);
    }

    public static void main(String[] args) {
        ((Logger) LoggerFactory.getLogger("org.mongodb.driver")).setLevel(Level.ERROR);
        System.out.println(banner().replaceAll("y", ColorPalette.ANSI_YELLOW).replaceAll("re", ColorPalette.ANSI_RESET));
        System.out.printf("Version: %s, Creator: Shindou Mihou, Build: %s\n", version, build);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ClientHandler.close();
            shards.values().forEach(DiscordApi::disconnect);
            MongoDB.shutdown();
            Scheduler.shutdown();
            Scheduler.getExecutorService().shutdown();
        }));
        ClientHandler.connect();
        FeedDB.preloadAllModels();
        UserDB.load();
        new DiscordApiBuilder()
                .setToken(token)
                .setAllIntentsExcept
                        (Intent.GUILD_MESSAGE_TYPING, Intent.DIRECT_MESSAGE_TYPING,
                        Intent.GUILD_INTEGRATIONS, Intent.GUILD_WEBHOOKS,
                        Intent.GUILD_BANS, Intent.GUILD_EMOJIS,
                                Intent.GUILD_INVITES, Intent.GUILD_VOICE_STATES,
                                Intent.GUILD_PRESENCES)
                .setTotalShards(1)
                .loginAllShards()
                .forEach(shard -> shard.
                        thenAccept(Amelia::onShardLogin)
                        .exceptionally(ExceptionLogger.get()));
    }

    private static void onShardLogin(DiscordApi api) {

        shards.put(api.getCurrentShard(), api);

        /* Performance optimizations **/
        api.setAutomaticMessageCacheCleanupEnabled(true);
        api.setMessageCacheSize(10, 1);
        api.setReconnectDelay(attempt -> attempt * 2);
        api.updateActivity(ActivityType.WATCHING, "The bot is starting up...");

        registerAllCommands(api);
        Amelia.log.info("All commands are now registered.");
        api.updateActivity(ActivityType.WATCHING, "People read stories!");
        Amelia.log.info("The bot has successfully booted up in shard [{}] with {} servers.", api.getCurrentShard(), api.getServers().size());
    }

    public static String format(ItemWrapper item, FeedModel feedModel, Server server) {
        if (item.valid()) {
            return MessageDB.getFormat(server.getId())
                    .replaceAll("\\{title}", item.getTitle())
                    .replaceAll("\\{author}", StoryHandler.getAuthor(item.getAuthor(), feedModel.getId(), feedModel.getFeedURL()))
                    .replaceAll("\\{link}", item.getLink())
                    .replaceAll("\\{subscribed}", getMentions(feedModel.getMentions(), server));
        } else {
            log.error("Title and link is not present on {}, full item: {}", feedModel.getFeedURL(), item);
            return "";
        }
    }

    public static String getMentions(ArrayList<Long> roles, Server server) {
        return roles.stream().map(aLong -> server.getRoleById(aLong).map(Role::getMentionTag))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.joining());
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
        api.addListener(new AuthorCommand());
        api.addListener(new IAmCommand());
    }

    private static String banner(){
        return  "y         _                  _   _         _            _              _          _          \n" +
                "y        / /\\               /\\_\\/\\_\\ _    /\\ \\         _\\ \\           /\\ \\       / /\\        \n" +
                "y       / /  \\             / / / / //\\_\\ /  \\ \\       /\\__ \\          \\ \\ \\     / /  \\       \n" +
                "y      / / /\\ \\           /\\ \\/ \\ \\/ / // /\\ \\ \\     / /_ \\_\\         /\\ \\_\\   / / /\\ \\      \n" +
                "y     / / /\\ \\ \\         /  \\____\\__/ // / /\\ \\_\\   / / /\\/_/        / /\\/_/  / / /\\ \\ \\     \n" +
                "y    / / /  \\ \\ \\       / /\\/________// /_/_ \\/_/  / / /            / / /    / / /  \\ \\ \\    \n" +
                "y   / / /___/ /\\ \\     / / /\\/_// / // /____/\\    / / /            / / /    / / /___/ /\\ \\   \n" +
                "y  / / /_____/ /\\ \\   / / /    / / // /\\____\\/   / / / ____       / / /    / / /_____/ /\\ \\  \n" +
                "y / /_________/\\ \\ \\ / / /    / / // / /______  / /_/_/ ___/\\ ___/ / /__  / /_________/\\ \\ \\ \n" +
                "y/ / /_       __\\ \\_\\\\/_/    / / // / /_______\\/_______/\\__\\//\\__\\/_/___\\/ / /_       __\\ \\_\\\n" +
                "y\\_\\___\\     /____/_/        \\/_/ \\/__________/\\_______\\/    \\/_________/\\_\\___\\     /____/_/re";
    }

}
