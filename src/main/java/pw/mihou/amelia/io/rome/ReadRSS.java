package pw.mihou.amelia.io.rome;

import com.apptastic.rssreader.RssReader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import pw.mihou.amelia.AmeliaKt;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReadRSS {

    private static final RssReader reader = new RssReader().setUserAgent("Amelia/1.0r1 (Language=Java/1.8, Developer=Shindou Mihou)");
    public static final LoadingCache<String, List<ItemWrapper>> feeds = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .refreshAfterWrite(9, TimeUnit.MINUTES)
            .build(ReadRSS::request);

    private static List<ItemWrapper> request(String url) {
        try {
            return reader.read(url).map(ItemWrapper::new).toList();
        } catch (IOException exception) {
            AmeliaKt.getLogger().error("Unable to connect to {}: {}", url, exception.getMessage());
            AmeliaKt.getLogger().info("Attempting to reconnect to {} in 0 seconds...", url);
            return retry(url, 0);
        }
    }

    public static List<ItemWrapper> getLatest(String url) {
        return feeds.get(url);
    }

    private static List<ItemWrapper> retry(String url, int i) {
        if (i < 10) {
            int bucket = i * 1000;
            try {
                return reader.read(url).map(ItemWrapper::new).toList();
            } catch (IOException exception) {
                try {
                    AmeliaKt.getLogger().error("Unable to connect to {}: {}", url, exception.getMessage());
                    AmeliaKt.getLogger().info("Attempting to reconnect to {} in {} seconds...", url, bucket);
                    Thread.sleep(bucket);
                    return retry(url, i);
                } catch (InterruptedException e) {
                    AmeliaKt.getLogger().error("Thread was interrupted exception while attempting to retry {} for {} bucket: {}", url, i, e.getMessage());
                    return Collections.emptyList();
                }
            }
        } else {
            AmeliaKt.getLogger().error("Failed to connect to {} after 10 attempts, sending error.", url);
            return Collections.emptyList();
        }
    }

}
