package pw.mihou.amelia.io.cache.entities;

import pw.mihou.amelia.io.cache.CacheSettings;
import tk.mihou.amatsuki.entities.story.Story;

import java.util.concurrent.TimeUnit;

public class StoryCache implements CacheEntity<Story> {

    private final long expiry;
    private final long creation;
    private final Story story;

    public StoryCache(Story story){
        this.story = story;
        this.creation = System.currentTimeMillis();
        this.expiry = creation + TimeUnit.HOURS.toMillis(CacheSettings.MAXIMUM_LIFESPAN);
    }

    @Override
    public Story get() {
        return story;
    }

    @Override
    public long getExpiry() {
        return expiry;
    }

    @Override
    public long getCreation() {
        return creation;
    }

    @Override
    public boolean isValid() {
        return expiry - System.currentTimeMillis() >= 0;
    }
}
