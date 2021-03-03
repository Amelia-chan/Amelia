package pw.mihou.amelia.io;

import pw.mihou.amelia.io.cache.CacheManager;
import tk.mihou.amatsuki.api.Amatsuki;
import tk.mihou.amatsuki.entities.story.Story;

public class AmatsukiWrapper {

    private static final Amatsuki connector = new Amatsuki();
    private static final String base_story = "https://scribblehub.com/series/%s/";

    public static Story getStoryById(int id){
        if(CacheManager.getStory(id).isPresent()){
            if(!CacheManager.getStory(id).get().isValid()){
                // We want it to be blocking.
                Story story = connector.getStoryFromUrl(String.format(base_story, id)).join();
                CacheManager.addStory(id, story);

                return story;
            } else {
                return CacheManager.getStory(id).get().get();
            }
        } else {
            // We want it to be blocking.
            Story story = connector.getStoryFromUrl(String.format(base_story, id)).join();
            CacheManager.addStory(id, story);

            return story;
        }
    }


}
