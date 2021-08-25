package pw.mihou.amelia.models;

import org.bson.Document;
import pw.mihou.amelia.commands.db.FeedDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class FeedModel {

    private final int id;
    private final long unique;
    private final String feedURL;
    private final long channel;
    private final String name;
    private final long user;
    private final ArrayList<Long> mentions = new ArrayList<>();
    private Date date;

    public FeedModel(long unique, int id, String feedURL, long channel, long user,
                     String name, Date date, ArrayList<Long> mentions) {
        this.unique = unique;
        this.id = id;
        this.feedURL = feedURL;
        this.channel = channel;
        this.user = user;
        this.name = name;
        this.date = date;
        this.mentions.addAll(mentions);
    }

    /**
     * Gets all the users or roles that will be mentioned during
     * an update.
     *
     * @return The mentions that will be notified.
     */
    public ArrayList<Long> getMentions() {
        return mentions;
    }

    /**
     * Subscribes a role from this feed.
     *
     * @param id The ID of the role to subscribe.
     */
    public void subscribeRole(long id) {
        mentions.add(id);
    }

    public long getUnique() {
        return unique;
    }

    /**
     * Gets the date the feed was created on.
     *
     * @return The date of the feed.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the name of the feed.
     *
     * @return The name of the feed.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the user who created of this feed.
     *
     * @return The user who created of this feed.
     */
    public long getUser() {
        return user;
    }

    /**
     * Gets the channel of this feed.
     *
     * @return The channel of this feed.
     */
    public long getChannel() {
        return channel;
    }

    /**
     * Gets the ID of the feed.
     *
     * @return The ID of the feed.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the Feed URL of this feed.
     *
     * @return The Feed URL.
     */
    public String getFeedURL() {
        return feedURL;
    }

    /**
     * Transforms this document into a MongoDB-understandable BSON document.
     *
     * @param server The server where this document will be bound to.
     * @return A new MongoDB document.
     */
    public Document toDocument(long server) {
        return new Document("id", getId())
                .append("unique", getUnique())
                .append("server", server)
                .append("url", getFeedURL())
                .append("channel", getChannel())
                .append("user", getUser())
                .append("name", getName())
                .append("date", getDate())
                .append("mentions", getMentions());
    }

    /**
     * Transforms a MongoDB BSON Document into a Feed Model which
     * Amelia can understand.
     *
     * @param document The document to transform.
     * @return A new Feed Model instance.
     */
    public static FeedModel from(Document document) {
        return new FeedModel(
                document.getLong("unique"),
                document.getInteger("id"),
                document.getString("url"),
                document.getLong("channel"),
                document.getLong("user"),
                document.getString("name"),
                document.getDate("date"),
                document.get("mentions", new ArrayList<>())
        );
    }

    /**
     * Unsubscribes a role from this feed.
     *
     * @param id The ID of the role to unsubscribe.
     */
    public void unsubscribeRole(long id) {
        mentions.remove(id);
    }

    /**
     * Sets the date of this feed.
     *
     * @param date The date of this feed.
     * @return The date of this feed.
     */
    public FeedModel setPublishedDate(Date date) {
        this.date = date;

        return this;
    }

    /**
     * Updates this feed to the database.
     *
     * @param server The server where this feed will be bound to.
     * @return A future to indicate completion.
     */
    public CompletableFuture<Void> update(long server) {
        return CompletableFuture.runAsync(() -> FeedDB.addModel(server, this));
    }
}
