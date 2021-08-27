package pw.mihou.amelia.commands.creation;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
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
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;
import pw.mihou.velen.pagination.Paginate;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.events.PaginateButtonEvent;
import tk.mihou.amatsuki.entities.story.lower.StoryResults;
import tk.mihou.amatsuki.entities.user.lower.UserResults;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static pw.mihou.amelia.templates.TemplateMessages.*;

// We're using Slash Event to make it easier to remove Velen Event once the
// message intents thingy is out.
public class Register implements VelenEvent, VelenSlashEvent {

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
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args, List<SlashCommandInteractionOption> options,
                        InteractionImmediateResponseBuilder firstResponder) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();

        if(!Limitations.isLimited(server, user)) {
            firstResponder.setContent(ERROR_MISSING_PERMISSIONS).setFlags(MessageFlag.EPHEMERAL).respond();
            return;
        }

        String type = event.getFirstOption().orElseThrow().getName();

        ServerChannel c = event.getFirstOption().orElseThrow().getFirstOptionChannelValue().orElseThrow();
        String query = event.getFirstOption().orElseThrow().getSecondOptionStringValue().orElseThrow();

        if (c.asServerTextChannel().isEmpty()) {
            firstResponder.setContent(ERROR_OPTION_TEXT_CHANNEL_REQUIRED).setFlags(MessageFlag.EPHEMERAL).respond();
            return;
        }

        ServerTextChannel channel = c.asServerTextChannel().get();
        event.respondLater().thenAccept(updater -> {
            // Handle user request here.
            if (type.equalsIgnoreCase("user")) {
                AmatsukiWrapper.getConnector().searchUser(query)
                        .thenAccept(userResults -> new Paginate<>(userResults)
                                .paginateWithButtons(UUID.randomUUID().toString().replaceAll("-", ""), originalEvent.getInteraction(),
                                        new PaginateButtonEvent<>() {
                                            @Override
                                            public void onSelect(InteractionImmediateResponseBuilder responder, Interaction event, Message paginateMessage,
                                                                 UserResults itemSelected, int arrow, Paginator<UserResults> paginator) {
                                                paginateMessage.delete().thenAccept(unused -> channel.sendMessage(NEUTRAL_LOADING).thenAccept(m -> {
                                                    int uid = Integer.parseInt(itemSelected.getUrl().replaceAll("[^\\d]", ""));
                                                    String rss = "https://www.scribblehub.com/rssfeed.php?type=author&uid=" + uid;
                                                    ReadRSS.getLatest(rss).ifPresentOrElse(item -> item.getPubDate().ifPresentOrElse(date -> {
                                                        FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(),
                                                                uid, rss, channel.getId(), user.getId(), itemSelected.getName() + "'s stories", date, new ArrayList<>()));
                                                        m.edit(SUCCESS_STORY + channel.getMentionTag());
                                                        }, () -> m.edit(ERROR_DATE_NOT_FOUND)), () -> m.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE));
                                                }));
                                                responder.respond();
                                            }

                                            @Override
                                            public void onPaginate(InteractionImmediateResponseBuilder responder, Interaction event, Message paginateMessage, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                                paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
                                                responder.respond();
                                            }

                                            @Override
                                            public InteractionOriginalResponseUpdater onInit(Interaction event, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                                return updater.addEmbed(embed(currentItem, arrow, paginator.size()));
                                            }

                                            @Override
                                            public InteractionOriginalResponseUpdater onEmptyPaginator(Interaction event) {
                                                return updater.setContent("❌ No results found, maybe try a more detailed query?");
                                            }
                                        }, Duration.ofMinutes(5)));
            }

            // Handle story request here.
            if (type.equalsIgnoreCase("story")) {
                AmatsukiWrapper.getConnector().searchStory(query)
                        .thenAccept(storyResults -> new Paginate<>(storyResults)
                                .paginateWithButtons(UUID.randomUUID().toString().replaceAll("-", ""), originalEvent.getInteraction(),
                                        new PaginateButtonEvent<>() {
                                            @Override
                                            public void onSelect(InteractionImmediateResponseBuilder responder, Interaction event, Message paginateMessage,
                                                                 StoryResults itemSelected, int arrow, Paginator<StoryResults> paginator) {
                                                paginateMessage.delete().thenAccept(unused -> channel.sendMessage("Please wait while we fetch data from ScribbleHub...").thenAccept(m -> {
                                                    int uid = Integer.parseInt(itemSelected.getUrl().replaceAll("[^\\d]", ""));
                                                    String rss = "https://www.scribblehub.com/rssfeed.php?type=series&sid=" + uid;
                                                    ReadRSS.getLatest(rss).ifPresentOrElse(item -> item.getPubDate().ifPresentOrElse(date -> {
                                                        FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(), uid, rss,
                                                                                channel.getId(), user.getId(), itemSelected.getName(), date, new ArrayList<>()));
                                                        m.edit(SUCCESS_STORY + channel.getMentionTag());
                                                        }, () -> m.edit(ERROR_DATE_NOT_FOUND)), () -> m.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE));
                                                }));
                                                responder.respond();
                                            }

                                            @Override
                                            public void onPaginate(InteractionImmediateResponseBuilder responder, Interaction event, Message paginateMessage, StoryResults currentItem, int arrow, Paginator<StoryResults> paginator) {
                                                paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
                                                responder.respond();
                                            }

                                            @Override
                                            public InteractionOriginalResponseUpdater onInit(Interaction event, StoryResults currentItem, int arrow, Paginator<StoryResults> paginator) {
                                                return updater.addEmbed(embed(currentItem, arrow, paginator.size()));
                                            }

                                            @Override
                                            public InteractionOriginalResponseUpdater onEmptyPaginator(Interaction event) {
                                                return updater.setContent("❌ No results found, maybe try a more detailed query?");
                                            }
                                        }, Duration.ofMinutes(5)));
            }
        });
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();

        if (args.length > 2) {
            if (!message.getMentionedChannels().isEmpty()
                    && message.getMentionedChannels().get(0).canYouWrite()) {
                ServerTextChannel channel = message.getMentionedChannels().get(0);
                if (DiscordRegexPattern.CHANNEL_MENTION.matcher(args[1]).matches()) {
                    String content = event.getMessageContent().substring(event.getMessageContent().indexOf(args[1]) + args[1].length() + 1);
                    if (args[0].equalsIgnoreCase("story")) {
                        AmatsukiWrapper.getConnector().searchStory(content)
                                .thenAccept(storyResults -> new Paginate<>(storyResults)
                                        .paginateWithButtons(UUID.randomUUID().toString().replaceAll("-", ""), event,
                                                new PaginateButtonEvent<>() {
                                                    @Override
                                                    public MessageBuilder onInit(MessageCreateEvent event, StoryResults currentItem, int arrow, Paginator<StoryResults> paginator) {
                                                        return new MessageBuilder().setEmbed(embed(currentItem, arrow, paginator.size()));
                                                    }

                                                    @Override
                                                    public void onPaginate(InteractionImmediateResponseBuilder responder, MessageCreateEvent event, Message paginateMessage,
                                                                           StoryResults currentItem, int arrow, Paginator<StoryResults> paginator) {
                                                        paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
                                                        responder.respond();
                                                    }

                                                    @Override
                                                    public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
                                                        return new MessageBuilder().setContent("❌ No results found, maybe try a more detailed query?");
                                                    }

                                                    @Override
                                                    public void onSelect(InteractionImmediateResponseBuilder responder, MessageCreateEvent event,
                                                                         Message paginateMessage, StoryResults itemSelected, int arrow, Paginator<StoryResults> paginator) {
                                                        paginateMessage.delete().thenAccept(unused -> event.getMessage().reply(NEUTRAL_LOADING).thenAccept(m -> {
                                                            int uid = Integer.parseInt(itemSelected.getUrl().replaceAll("[^\\d]", ""));
                                                            String rss = "https://www.scribblehub.com/rssfeed.php?type=series&sid=" + uid;
                                                            ReadRSS.getLatest(rss).ifPresentOrElse(item -> item.getPubDate().ifPresentOrElse(date -> {
                                                                FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(), uid, rss,
                                                                        channel.getId(), user.getId(), itemSelected.getName(), date, new ArrayList<>()));
                                                                m.edit(SUCCESS_STORY + channel.getMentionTag());
                                                                }, () -> m.edit(ERROR_DATE_NOT_FOUND)),
                                                                    () -> m.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE));
                                                        }));
                                                        responder.respond();
                                                    }
                                                }, Duration.ofMinutes(5))).exceptionally(ExceptionLogger.get());
                    } else {
                        AmatsukiWrapper.getConnector().searchUser(content)
                                .thenAccept(userResults -> new Paginate<>(userResults).paginateWithButtons(UUID.randomUUID().toString().replaceAll("-", ""), event,
                                        new PaginateButtonEvent<>() {
                                            @Override
                                            public MessageBuilder onInit(MessageCreateEvent event, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                                return new MessageBuilder().setEmbed(embed(currentItem, arrow, paginator.size()));
                                            }

                                            @Override
                                            public void onPaginate(InteractionImmediateResponseBuilder responder, MessageCreateEvent event, Message paginateMessage,
                                                                   UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                                paginateMessage.edit(embed(currentItem, arrow, paginator.size()));
                                                responder.respond();
                                            }

                                            @Override
                                            public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
                                                return new MessageBuilder().setContent("❌ No results found, maybe try a more detailed query?");
                                            }

                                            @Override
                                            public void onSelect(InteractionImmediateResponseBuilder responder, MessageCreateEvent event,
                                                                 Message paginateMessage, UserResults itemSelected, int arrow, Paginator<UserResults> paginator) {
                                                paginateMessage.delete().thenAccept(unused -> event.getMessage().reply(NEUTRAL_LOADING).thenAccept(m -> {
                                                    int uid = Integer.parseInt(itemSelected.getUrl().replaceAll("[^\\d]", ""));
                                                    String rss = "https://www.scribblehub.com/rssfeed.php?type=author&uid=" + uid;
                                                    ReadRSS.getLatest(rss).ifPresentOrElse(item -> item.getPubDate().ifPresentOrElse(date -> {
                                                        FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(),uid, rss, channel.getId(), user.getId(),
                                                                itemSelected.getName() + "'s stories", date, new ArrayList<>()));

                                                        m.edit(SUCCESS_USER + channel.getMentionTag());
                                                        }, () -> m.edit(ERROR_DATE_NOT_FOUND)), () -> m.edit(ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE));
                                                }));

                                                responder.respond();
                                            }
                                        }, Duration.ofMinutes(5)));
                    }
                } else {
                    message.reply(REFER_USAGE);
                }
            } else {
                message.reply(ERROR_OPTION_CHANNEL_NOT_FOUND);
            }
        } else {
            message.reply(REFER_USAGE);
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

    private static final String SUCCESS_USER = "✅ The bot will now send updates for the user's stories on the channel, ";
    private static final String SUCCESS_STORY = "✅ The bot will now send updates for the story on the channel, ";
    private static final String REFER_USAGE = "❌ Invalid usage, please refer to `help register`.";
}
