package pw.mihou.amelia.models;

import pw.mihou.amelia.commands.db.FeedDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class FeedModel {

    private int id;
    private long unique;
    private String feedURL;
    private long channel;
    private String name;
    private long user;
    private Date date;
    private ArrayList<Long> mentions = new ArrayList<>();

    public FeedModel(long unique, int id, String feedURL, long channel, long user, String name, Date date, ArrayList<Long> mentions) {
        this.unique = unique;
        this.id = id;
        this.feedURL = feedURL;
        this.channel = channel;
        this.user = user;
        this.name = name;
        this.date = date;
        this.mentions.addAll(mentions);
    }

    public ArrayList<Long> getMentions(){
        return mentions;
    }

    public FeedModel subscribeRole(long id){
        mentions.add(id);

        return this;
    }

    public FeedModel unsubscribeRole(long id){
        mentions.remove(id);

        return this;
    }

    public FeedModel setPublishedDate(Date date){
        this.date = date;

        return this;
    }

    public CompletableFuture<Void> update(long server){
        return CompletableFuture.runAsync(() -> FeedDB.addModel(server, this));
    }

    public long getUnique(){
        return unique;
    }

    public Date getDate(){
        return date;
    }

    public String getName(){
        return name;
    }

    public long getUser(){
        return user;
    }

    public long getChannel(){
        return channel;
    }

    public int getId() {
        return id;
    }

    public String getFeedURL() {
        return feedURL;
    }
}
