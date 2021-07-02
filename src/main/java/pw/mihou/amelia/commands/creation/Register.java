package pw.mihou.amelia.commands.creation;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.amelia.commands.Limitations;
import pw.mihou.amelia.commands.base.db.ServerDB;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.io.AmatsukiWrapper;
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.amelia.models.FeedModel;
import pw.mihou.amelia.models.ServerModel;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.amelia.utility.StringUtils;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.pagination.Paginate;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.events.PaginateEvent;
import tk.mihou.amatsuki.entities.story.lower.StoryResults;
import tk.mihou.amatsuki.entities.user.lower.UserResults;

import java.time.Duration;
import java.util.ArrayList;

public class Register implements VelenEvent {

    /**
     * Checks if the user has the specified role
     * from the server.
     *
     * @param user   The user to check.
     * @param server The server to check.
     * @return Does the user have the role?
     */
    public static boolean hasRole(User user, Server server) {
        ServerModel model = ServerDB.getServer(server.getId());
        return model.getRole().isPresent() && user.getRoles(server)
                .stream().anyMatch(role -> role.getId() == model.getRole().get());
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();
        if (!Limitations.isLimited(server, user)) {
            pw.mihou.amelia.templates.Message.msg("You do not have permission to use this command, required permission: " +
                    "Manage Server, or lacking the required role to modify feeds.").send(event.getChannel());
            return;
        }

        if (args.length > 2) {
            if (!message.getMentionedChannels().isEmpty()
                    && message.getMentionedChannels().get(0).canYouWrite()) {
                ServerTextChannel channel = message.getMentionedChannels().get(0);
                if (DiscordRegexPattern.CHANNEL_MENTION.matcher(args[1]).matches()) {
                    String content = event.getMessageContent().substring(event.getMessageContent().indexOf(args[1]) + args[1].length() + 1);
                    if (args[0].equalsIgnoreCase("story")) {
                        AmatsukiWrapper.getConnector().searchStory(content)
                                .thenAccept(storyResults -> new Paginate<>(storyResults)
                                        .paginate(event, new PaginateEvent<>() {
                                            @Override
                                            public MessageBuilder onInit(MessageCreateEvent event, StoryResults currentItem, int arrow, Paginator<StoryResults> paginator) {
                                                return new MessageBuilder().setEmbed(embed(currentItem, arrow, paginator.size()));
                                            }

                                            @Override
                                            public void onPaginate(MessageCreateEvent event, Message paginateMessage, StoryResults currentItem, int arrow, Paginator<StoryResults> paginator) {
                                                paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
                                            }

                                            @Override
                                            public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
                                                return new MessageBuilder().setContent("**ERROR**: No results found, maybe try a deeper query?");
                                            }

                                            @Override
                                            public void onSelect(MessageCreateEvent event, Message paginateMessage, StoryResults itemSelected, int arrow, Paginator<StoryResults> paginator) {
                                                itemSelected.transformToStory().thenAccept(story -> ReadRSS.getLatest(story.getRSS()).ifPresentOrElse(item -> item.getPubDate().ifPresentOrElse(date -> {
                                                            FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(), story.getSID(), story.getRSS(),
                                                                    channel.getId(), user.getId(), story.getTitle(), date, new ArrayList<>()));
                                                            paginateMessage.delete();
                                                            event.getMessage().reply("**SUCCESS**: The bot will now send updates for the story on the channel, " + channel.getMentionTag());
                                                        }, () -> event.getMessage().reply("**ERROR**: An error occurred while attempting to retrieve date or parse the date of feed.")),
                                                        () -> event.getMessage().reply("**ERROR**: An error occurred while retrieving RSS feed, " +
                                                                "**this usually happens when the story has no chapters posted.**")));
                                            }
                                        }, Duration.ofMinutes(5))).exceptionally(ExceptionLogger.get());
                    } else {
                        AmatsukiWrapper.getConnector().searchUser(content)
                                .thenAccept(userResults -> new Paginate<>(userResults).paginate(event, new PaginateEvent<>() {
                                    @Override
                                    public MessageBuilder onInit(MessageCreateEvent event, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                        return new MessageBuilder().setEmbed(embed(currentItem, arrow, paginator.size()));
                                    }

                                    @Override
                                    public void onPaginate(MessageCreateEvent event, Message paginateMessage, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                        paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
                                    }

                                    @Override
                                    public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
                                        return new MessageBuilder().setContent("**ERROR**: No results found, maybe try a deeper query?");
                                    }

                                    @Override
                                    public void onSelect(MessageCreateEvent event, Message paginateMessage, UserResults itemSelected, int arrow, Paginator<UserResults> paginator) {
                                        itemSelected.transformToUser().thenAccept(u -> ReadRSS.getLatest(u.getRSS())
                                                .ifPresentOrElse(item -> item.getPubDate().ifPresentOrElse(date -> {
                                                            FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(),
                                                                    u.getUID(), u.getRSS(), channel.getId(), user.getId(),
                                                                    u.getName() + "'s stories", date, new ArrayList<>()));
                                                            paginateMessage.delete();
                                                            event.getMessage().reply("**SUCCESS**: The bot will now send updates for the user's stories on the channel, " + channel.getMentionTag());
                                                        }, () -> event.getMessage().reply("**ERROR**: An error occurred while attempting to retrieve date or parse the date of feed.")),
                                                        () -> event.getMessage().reply("**ERROR**: An error occurred while retrieving RSS feed, " +
                                                                "**this usually happens when the user has no stories published.**")));
                                    }
                                }, Duration.ofMinutes(5)));
                    }
                } else {
                    message.reply("**ERROR**: Invalid usage, please refer to `help register`.");
                }
            } else {
                message.reply("**ERROR**: There are no channels mentioned or the bot cannot write on the channel mentioned!");
            }
        } else {
            message.reply("**ERROR**: Invalid usage, please refer to `help register`.");
        }
    }

    /**
     * The embed for story results.
     *
     * @param result  The result received.
     * @param arrow   The current pointer's location.
     * @param maximum The maximum amount of items.
     * @return An embed.
     */
    private EmbedBuilder embed(StoryResults result, int arrow, int maximum) {
        return new Embed().setTitle(result.getName() + "(" + (arrow + 1) + "/" + maximum + ")").setDescription(StringUtils.stripToLengthWhileDotsEnd(result.getFullSynopsis(), 450))
                .setThumbnail(result.getThumbnail()).build()
                .addInlineField("Statistics", "\nReaders: " + result.getReaders() + "\nRating: " + result.getRating() + "\nViews: " + result.getViews()
                        + "\nWord Count: " + result.getWordCount())
                .addInlineField("Additional Statistics", "\nChapters: " + result.getChapters() + "\nFavorites: " + result.getFavorites() + "\nReviews: " + result.getReviews() + "\nChapter/week: " + result.getChapterPerWeek())
                .addInlineField("Additional Information", "\nAuthor: " + result.getCreator() +
                        "\nLast Updated: " + result.getLastUpdated())
                .addInlineField("Genres", result.getGenres().toString().replaceAll("[\\[\\](){}]", "")).setAuthor("Press here to read", result.getUrl(), "https://cdn.discordapp.com/attachments/778550757204426793/779630090769793074/L8g8JndJ.png");
    }

    /**
     * The embed for user results.
     *
     * @param result  The result received.
     * @param arrow   The current pointer's location.
     * @param maximum The maximum amount of items.
     * @return An embed.
     */
    private EmbedBuilder embed(UserResults result, int arrow, int maximum) {
        return new Embed().setTitle(result.getName() + "(" + (arrow + 1) + "/" + maximum + ")").setDescription("[Click here to redirect](" + result.getUrl() + ")")
                .attachImage(result.getAvatar()).build();
    }

}
