package pw.mihou.amelia.models;

import pw.mihou.amelia.io.AmatsukiWrapper;
import tk.mihou.amatsuki.entities.user.User;

import java.util.concurrent.CompletableFuture;

public class SHUser {

    private final int unique;
    private final String url;
    private final String name;

    public SHUser(String url, int unique, String name){
        this.unique = unique;
        this.url = url;
        this.name = name;
    }

    public int getUnique(){
        return unique;
    }

    public String getUrl(){
        return url;
    }

    public String getName(){
        return name;
    }

    public CompletableFuture<User> asUser(){
        return AmatsukiWrapper.getConnector().getUserFromUrl(url);
    }

}
