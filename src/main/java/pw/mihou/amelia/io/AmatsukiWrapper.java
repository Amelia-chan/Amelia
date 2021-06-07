package pw.mihou.amelia.io;

import tk.mihou.amatsuki.api.Amatsuki;
import tk.mihou.amatsuki.entities.story.Story;
import tk.mihou.amatsuki.entities.user.User;

import java.util.concurrent.TimeUnit;

public class AmatsukiWrapper {

    private static final Amatsuki connector = new Amatsuki().setLifespan(24, TimeUnit.HOURS).setCache(true);
    private static final String base_story = "https://scribblehub.com/series/%d/amelia-discord-bot/";
    private static final String base_user = "https://scribblehub.com/profile/%d/amelia-discord-bot/";

    public static Story getStoryById(int id) {
        return connector.getStoryFromUrl(String.format(base_story, id)).join();
    }

    public static User getUserById(int id) {
        return connector.getUserFromUrl(String.format(base_user, id)).join();
    }

    public static Amatsuki getConnector(){
        return connector;
    }


}
