package pw.mihou.amelia.io;

import tk.mihou.amatsuki.api.Amatsuki;
import tk.mihou.amatsuki.entities.story.Story;

import java.util.concurrent.TimeUnit;

public class AmatsukiWrapper {

    private static final Amatsuki connector = new Amatsuki().setLifespan(24, TimeUnit.HOURS).setCache(true);
    private static final String base_story = "https://scribblehub.com/series/%s/amelia-discord-bot/";

    public static Story getStoryById(int id) {
        // Amatsuki has its own built-in cache manager.
        return connector.getStoryFromUrl(String.format(base_story, id)).join();
    }

    public static Amatsuki getConnector(){
        return connector;
    }


}
