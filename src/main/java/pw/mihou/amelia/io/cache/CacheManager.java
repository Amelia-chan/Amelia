package pw.mihou.amelia.io.cache;

import pw.mihou.amelia.io.cache.entities.StoryCache;
import tk.mihou.amatsuki.entities.story.Story;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class CacheManager {

    private static final Map<Integer, StoryCache> nodeStory = new TreeMap<>();


    public static Optional<StoryCache> getStory(int id){
        return Optional.ofNullable(nodeStory.get(id));
    }

    public static void addStory(int id, Story story){ nodeStory.put(id, new StoryCache(story)); }

}
