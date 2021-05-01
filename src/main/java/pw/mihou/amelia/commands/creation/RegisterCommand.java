package pw.mihou.amelia.commands.creation;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.base.db.ServerDB;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.io.rome.ReadRSS;
import pw.mihou.amelia.models.FeedModel;
import pw.mihou.amelia.models.ServerModel;
import pw.mihou.amelia.models.StoryNavigators;
import pw.mihou.amelia.models.UserNavigators;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.amelia.utility.StringUtils;
import tk.mihou.amatsuki.api.Amatsuki;
import tk.mihou.amatsuki.entities.story.lower.StoryResults;
import tk.mihou.amatsuki.entities.user.lower.UserResults;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RegisterCommand extends Command {

    private final Amatsuki amatsuki = new Amatsuki();

    public RegisterCommand() {
        super("register", "Registers either a user, or a story's RSS feed.", "register [story/user] [#channel] [@user]", true);
    }

    public static boolean hasRole(User user, Server server) {
        ServerModel model = ServerDB.getServer(server.getId());
        if (model.getRole().isPresent()) {
            for (Role role : user.getRoles(server)) {
                if (role.getId() == model.getRole().get()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {

        if (args.length > 3) {
            if (!event.getMessage().getMentionedChannels().isEmpty()) {
                if (event.getMessage().getMentionedChannels().get(0).canYouWrite()) {
                    ServerTextChannel channel = event.getMessage().getMentionedChannels().get(0);
                    if (args[2].startsWith("<#") && args[2].endsWith(">")) {
                        String content = event.getMessageContent().replaceAll(args[0] + " " + args[1] + " " + args[2] + " ", "");
                        if (isStory(args[1])) {
                            // Story version.
                            amatsuki.searchStory(content).thenAccept(storyResults -> {
                                if (!storyResults.isEmpty()) {
                                    StoryNavigators navigators = new StoryNavigators(storyResults);
                                    event.getMessage().reply(storyResultEmbed(navigators.current(), navigators.getArrow(), navigators.getMaximum())).thenAccept(message -> {
                                        if (navigators.getMaximum() > 1) {
                                            message.addReactions("⬅", "👎", "👍", "➡");
                                        } else {
                                            message.addReactions("👎", "👍");
                                        }
                                        message.addReactionAddListener(e -> {
                                            if (e.getUserId() == event.getMessageAuthor().getId()) {
                                                if (e.getEmoji().equalsEmoji("➡") && navigators.getMaximum() > 1) {
                                                    if (navigators.getArrow() < navigators.getMaximum() - 1) {
                                                        message.edit(storyResultEmbed(navigators.next(), navigators.getArrow(), navigators.getMaximum()));
                                                    }
                                                } else if (e.getEmoji().equalsEmoji("⬅") && navigators.getMaximum() > 1) {
                                                    if (navigators.getArrow() > 0) {
                                                        message.edit(storyResultEmbed(navigators.backwards(), navigators.getArrow(), navigators.getMaximum()));
                                                    }
                                                } else if (e.getEmoji().equalsEmoji("👍")) {
                                                    navigators.current().transformToStory().thenAccept(story -> {
                                                        // Give a fresh read on the RSS, retrieve the first entry then save the published date.
                                                        ReadRSS.getLatest(story.getRSS()).ifPresentOrElse(syndEntry -> {
                                                            FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(), story.getSID(), story.getRSS(), channel.getId(), user.getId(), story.getTitle(), syndEntry.getPublishedDate(), new ArrayList<>()));
                                                            message.delete();
                                                            event.getMessage().reply("The bot will now send updates for the story on the channel, " + channel.getMentionTag());
                                                        }, () -> event.getMessage().reply("An error occurred while retrieving RSS feed, please try again."));
                                                    });
                                                } else if (e.getEmoji().equalsEmoji("👎")) {
                                                    message.delete("End of purpose.");
                                                    event.getMessage().delete();
                                                }
                                            }
                                            if (e.getUserId() != event.getApi().getYourself().getId()) {
                                                e.removeReaction();
                                            }
                                        }).removeAfter(5, TimeUnit.MINUTES).addRemoveHandler(message::removeAllReactions);
                                    });
                                } else {
                                    event.getMessage().reply("Error: No results found.");
                                }
                            });
                        } else {
                            // User version.
                            amatsuki.searchUser(content).thenAccept(userResults -> {
                                if (!userResults.isEmpty()) {
                                    UserNavigators navigators = new UserNavigators(userResults);
                                    event.getMessage().reply(userResultEmbed(navigators.current(), navigators.getArrow(), navigators.getMaximum())).thenAccept(message -> {
                                        if (navigators.getMaximum() > 1) {
                                            message.addReactions("⬅", "👎", "👍", "➡");
                                        } else {
                                            message.addReactions("👎", "👍");
                                        }
                                        message.addReactionAddListener(e -> {
                                            if (e.getUserId() == event.getMessageAuthor().getId()) {
                                                if (e.getEmoji().equalsEmoji("➡") && navigators.getMaximum() > 1) {
                                                    if (navigators.getArrow() < navigators.getMaximum() - 1) {
                                                        message.edit(userResultEmbed(navigators.next(), navigators.getArrow(), navigators.getMaximum()));
                                                    }
                                                } else if (e.getEmoji().equalsEmoji("⬅") && navigators.getMaximum() > 1) {
                                                    if (navigators.getArrow() > 0) {
                                                        message.edit(userResultEmbed(navigators.backwards(), navigators.getArrow(), navigators.getMaximum()));
                                                    }
                                                } else if (e.getEmoji().equalsEmoji("👍")) {
                                                    navigators.current().transformToUser().thenAccept(u -> ReadRSS.getLatest(u.getRSS()).ifPresentOrElse(syndEntry -> {
                                                        FeedDB.addModel(server.getId(), new FeedModel(FeedDB.generateUnique(), u.getUID(), u.getRSS(), channel.getId(), user.getId(), u.getName() + "'s stories", syndEntry.getPublishedDate(), new ArrayList<>()));
                                                        message.delete();
                                                        event.getMessage().reply("The bot will now send updates for the user's stories on the channel, " + channel.getMentionTag());
                                                    }, () -> event.getMessage().reply("An error occurred while retrieving RSS feed, please try again.")));
                                                    message.delete("End of purpose.");
                                                } else if (e.getEmoji().equalsEmoji("👎")) {
                                                    message.delete("End of purpose.");
                                                    event.getMessage().delete();
                                                }
                                            }
                                            if (e.getUserId() != event.getApi().getYourself().getId()) {
                                                e.removeReaction();
                                            }
                                        }).removeAfter(5, TimeUnit.MINUTES).addRemoveHandler(message::removeAllReactions);
                                    });
                                } else {
                                    event.getMessage().reply("Error: No results found, try a deeper query.");
                                }
                            });
                        }
                    } else {
                        event.getMessage().reply("Error: Invalid usage [`register [story/user] [@channel] [query]`]");
                    }
                } else {
                    event.getMessage().reply("Error: The bot cannot write on the channel mentioned!");
                }
            } else {
                event.getMessage().reply("Error: No channel mentioned.");
            }
        } else {
            event.getMessage().reply("Error: Invalid usage [`register [story/user] [@channel] [query]`]");
        }
    }

    private EmbedBuilder storyResultEmbed(StoryResults result, int arrow, int maximum) {
        return new Embed().setTitle(result.getName() + "(" + (arrow + 1) + "/" + maximum + ")").setDescription(StringUtils.stripToLengthWhileDotsEnd(result.getFullSynopsis(), 450))
                .setThumbnail(result.getThumbnail()).build()
                .addInlineField("Statistics", "\nReaders: " + result.getReaders() + "\nRating: " + result.getRating() + "\nViews: " + result.getViews()
                        + "\nWord Count: " + result.getWordCount())
                .addInlineField("Additional Statistics", "\nChapters: " + result.getChapters() + "\nFavorites: " + result.getFavorites() + "\nReviews: " + result.getReviews() + "\nChapter/week: " + result.getChapterPerWeek())
                .addInlineField("Additional Information", "\nAuthor: " + result.getCreator() +
                        "\nLast Updated: " + result.getLastUpdated())
                .addInlineField("Genres", result.getGenres().toString().replaceAll("[\\[\\](){}]", "")).setAuthor("Press here to read", result.getUrl(), "https://cdn.discordapp.com/attachments/778550757204426793/779630090769793074/L8g8JndJ.png");
    }

    private EmbedBuilder userResultEmbed(UserResults result, int arrow, int maximum) {
        return new Embed().setTitle(result.getName() + "(" + (arrow + 1) + "/" + maximum + ")").setDescription("[Click here to redirect](" + result.getUrl() + ")")
                .attachImage(result.getAvatar()).build();
    }

    public boolean isStory(String arg) {
        return arg.equalsIgnoreCase("story");
    }
}
