package pw.mihou.amelia;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.util.logging.ExceptionLogger;
import org.slf4j.LoggerFactory;
import pw.mihou.amelia.clients.ClientHandler;
import pw.mihou.amelia.commands.Limitations;
import pw.mihou.amelia.commands.base.db.ServerDB;
import pw.mihou.amelia.commands.creation.Register;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.commands.db.MessageDB;
import pw.mihou.amelia.commands.feeds.Feeds;
import pw.mihou.amelia.commands.feeds.Modify;
import pw.mihou.amelia.commands.invite.Invite;
import pw.mihou.amelia.commands.miscellanous.Ping;
import pw.mihou.amelia.commands.notifier.Author;
import pw.mihou.amelia.commands.notifier.IAm;
import pw.mihou.amelia.commands.removal.Remove;
import pw.mihou.amelia.commands.settings.Settings;
import pw.mihou.amelia.commands.support.Help;
import pw.mihou.amelia.commands.test.Test;
import pw.mihou.amelia.db.MongoDB;
import pw.mihou.amelia.db.UserDB;
import pw.mihou.amelia.io.Scheduler;
import pw.mihou.amelia.io.StoryHandler;
import pw.mihou.amelia.io.rome.ItemWrapper;
import pw.mihou.amelia.listeners.BotJoinCommand;
import pw.mihou.amelia.listeners.BotLeaveListener;
import pw.mihou.amelia.models.FeedModel;
import pw.mihou.amelia.utility.ColorPalette;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.messages.types.VelenConditionalMessage;
import pw.mihou.velen.interfaces.messages.types.VelenPermissionMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRatelimitMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRoleMessage;
import pw.mihou.velen.modules.core.SlashCommandChecker;
import pw.mihou.velen.modules.modes.SlashCommandCheckerMode;
import pw.mihou.velen.prefix.VelenPrefixManager;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Amelia {

    public static final Logger log = (Logger) LoggerFactory.getLogger("Amelia Client");
    public static final SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss");
    public static final HashMap<Integer, DiscordApi> shards = new HashMap<>();
    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String token = System.getenv("amelia_token");
    private static final String version = "1.5.5";
    private static final String build = "STATIC";
    public static boolean connected = false;
    public static Velen velen;

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

        velen = Velen.builder()
                .setPrefixManager(new VelenPrefixManager("a.", key -> ServerDB.getServer(key).getPrefix()))
                .setDefaultCooldownTime(Duration.ofSeconds(5))
                .setNoPermissionMessage(VelenPermissionMessage.ofNormal((list, user, textChannel, s) -> "You do not have permission to use this command, required permissions: " +
                        list.stream().map(Enum::name).collect(Collectors.joining(", "))))
                .setNoRoleMessage(VelenRoleMessage.ofNormal((roles, user, channel, command) -> "You need to have either of the roles: " + roles + " to use this command!"))
                .setRatelimitedMessage(VelenRatelimitMessage.ofNormal((remainingSeconds, user, channel, command) -> "You are currently on cooldown, please wait " + remainingSeconds + " seconds!"))
                .build();

        registerAllCommands();
        new DiscordApiBuilder()
                .setToken(token)
                .setAllNonPrivilegedIntents()
                .setUserCacheEnabled(true)
                .addListener(velen)
                .addListener(new BotJoinCommand())
                .addListener(new BotLeaveListener())
                .addReconnectListener(e -> {
                    // This is to keep this adorable little activity message.
                    e.getApi().updateActivity(ActivityType.WATCHING, "People read stories!");
                }).setTotalShards(1)
                .loginAllShards()
                .forEach(shard -> shard.
                        thenAccept(Amelia::onShardLogin)
                        .exceptionally(ExceptionLogger.get()));
    }

    private static void onShardLogin(DiscordApi api) {
        shards.put(api.getCurrentShard(), api);

        if(shards.size() == 1) {
            new SlashCommandChecker(api, SlashCommandCheckerMode.NORMAL)
                    .run(velen).thenAccept(integer -> Amelia.log.info("Successfully updated {} slash commands!", integer));
        }

        api.setAutomaticMessageCacheCleanupEnabled(true);
        api.setMessageCacheSize(10, 1);
        api.setReconnectDelay(attempt -> attempt * 2);
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

    private static void registerAllCommands() {
        VelenCommand.ofHybrid("register", "Registers either a user or a story's RSS feed.", velen, new Register(), new Register())
                .addOptions(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "user", "You are registering a ScribbleHub user for RSS updates.",
                                Arrays.asList(
                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "channel", "Where should the updates be sent towards?", true),
                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "name", "The name of the user on ScribbleHub.", true)
                                )
                        ),
                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "story", "You are registering a ScribbleHub story for RSS updates.",
                                Arrays.asList(
                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "channel", "Where should the updates be sent towards?", true),
                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "name", "The name of the story on ScribbleHub.", true)
                                )
                        )
                ).setUsage("register [story/user] [#channel] [query]")
                .setCategory("Feeds")
                .addShortcuts("add", "reg", "create")
                .setServerOnly(true)
                .addCondition(event -> event.getServer().isPresent() && event.getMessageAuthor().asUser().isPresent() &&
                        Limitations.isLimited(event.getServer().get(), event.getMessageAuthor().asUser().get()))
                .setConditionalMessage(VelenConditionalMessage.ofNormal((user, textChannel, s) -> "You do not have permission to use this command, required permission: " +
                        "Manage Server, or lacking the required role to modify feeds."))
                .attach();

        VelenCommand.ofHybrid("feeds", "Returns back all the feeds on the server.", velen, new Feeds(), new Feeds())
                .setUsage("feeds")
                .setCategory("Feeds")
                .setServerOnly(true)
                .addShortcuts("feed", "fe")
                .attach();

        VelenCommand.ofHybrid("subscribe", "Subscribe a role to the feed.", velen, new Modify(true), new Modify(true))
                .setUsage("subscribe [feed unique id] [roles]")
                .addOptions(
                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "feed", "The UNIQUE ID of the feed to modify.", true),
                        SlashCommandOption.create(SlashCommandOptionType.ROLE, "role", "The role to subscribe from the feed.", true)
                )
                .setServerOnly(true)
                .addShortcut("sub")
                .setCategory("Feeds")
                .addCondition(event -> event.getServer().isPresent() && event.getMessageAuthor().asUser().isPresent() &&
                        Limitations.isLimited(event.getServer().get(), event.getMessageAuthor().asUser().get()))
                .setConditionalMessage(VelenConditionalMessage.ofNormal((user, textChannel, s) -> "You do not have permission to use this command, required permission: " +
                        "Manage Server, or lacking the required role to modify feeds."))
                .attach();

        VelenCommand.ofHybrid("unsubscribe", "Unsubscribe a role to the feed.", velen, new Modify(false), new Modify(false))
                .addOptions(
                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "feed", "The UNIQUE ID of the feed to modify.", true),
                        SlashCommandOption.create(SlashCommandOptionType.ROLE, "role", "The role to unsubscribe from the feed.", true)
                        )
                .setUsage("unsubscribe [feed unique id] [roles]")
                .setServerOnly(true)
                .addShortcut("unsub")
                .setCategory("Feeds")
                .addCondition(event -> event.getServer().isPresent() && event.getMessageAuthor().asUser().isPresent() &&
                        Limitations.isLimited(event.getServer().get(), event.getMessageAuthor().asUser().get()))
                .setConditionalMessage(VelenConditionalMessage.ofNormal((user, textChannel, s) -> "You do not have permission to use this command, required permission: " +
                        "Manage Server, or lacking the required role to modify feeds."))
                .attach();

        VelenCommand.ofHybrid("invite", "Want an invitation link for the bot?", velen, new Invite(), new Invite())
                .setCategory("Miscellaneous")
                .setServerOnly(false)
                .addShortcut("inv")
                .setUsage("invite")
                .attach();

        VelenCommand.ofHybrid("ping", "Pings the bot to test if it is alive.", velen, new Ping(), new Ping())
                .setCategory("Miscellaneous")
                .setServerOnly(false)
                .addShortcuts("pong")
                .setUsage("ping")
                .attach();

        VelenCommand.of("settings", "Modifies the settings for Amelia-chan in the server.", velen, new Settings())
                .setCategory("Miscellaneous")
                .addShortcuts("config")
                .setServerOnly(true)
                .setUsage("settings prefix [prefix], settings limit, settings role [@role]")
                .attach();

        VelenCommand.ofHybrid("remove", "Removes a feed from the server, " +
                "can only be done by the user who added the feed or a user with Manage Server permission.", velen, new Remove(), new Remove())
                .setUsage("remove [feed id]")
                .addOption(SlashCommandOption.create(SlashCommandOptionType.INTEGER, "feed", "The ID of the feed to test, you can find via feeds command.", true))
                .setServerOnly(true)
                .setCategory("Feeds")
                .addShortcuts("rm", "rem")
                .addCondition(event -> event.getServer().isPresent() && event.getMessageAuthor().asUser().isPresent() &&
                        Limitations.isLimited(event.getServer().get(), event.getMessageAuthor().asUser().get()))
                .setConditionalMessage(VelenConditionalMessage.ofNormal((user, textChannel, s) -> "You do not have permission to use this command, required permission: " +
                        "Manage Server, or lacking the required role to modify feeds."))
                .attach();

        VelenCommand.ofHybrid("author", "Retrieve or manage all the accounts associated with your Discord.", velen, new Author(), new Author())
                .addOptions(
                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "remove", "Stop getting notifications for a certain ScribbleHub account when it's stories goes into trending.",
                                List.of(
                                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "id", "The unique id of the account associated, can be found on author me command.", true)
                                )),
                        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "me", "Retrieves the list of ScribbleHub accounts associated in your Discord account.")
                ).setUsage("author remove [id], author me")
                .setServerOnly(false)
                .setCategory("Trending Notifications")
                .addShortcuts("auth", "writer")
                .attach();

        VelenCommand.ofHybrid("iam", "Associate your ScribbleHub account to your Discord account to notify you whenever you trend (top 9).", velen,
                new IAm(), new IAm())
                .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "name", "The name of your account.", true))
                .setUsage("iam [username]")
                .setServerOnly(false)
                .setCategory("Trending Notifications")
                .addShortcut("ami")
                .attach();

        VelenCommand.ofHybrid("test", "Test run a feed.", velen, new Test(), new Test())
                .setUsage("test [feed id]")
                .addOption(SlashCommandOption.create(SlashCommandOptionType.INTEGER, "feed", "The ID of the feed to test, you can find via feeds command.", true))
                .setCategory("Miscellaneous")
                .addShortcuts("run")
                .setServerOnly(true)
                .attach();

        VelenCommand.ofHybrid("help", "The general help command of Amelia.", velen, new Help(), new Help())
                .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "command", "The command to search for.", false))
                .setUsage("help, help [command]")
                .setServerOnly(false)
                .addShortcuts("hel")
                .setCategory("Miscellaneous")
                .attach();
    }

    public static String getMentions(ArrayList<Long> roles, Server server) {
        return roles.stream().map(aLong -> server.getRoleById(aLong).map(Role::getMentionTag))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.joining());
    }

    private static String banner() {
        return "y         _                  _   _         _            _              _          _          \n" +
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
