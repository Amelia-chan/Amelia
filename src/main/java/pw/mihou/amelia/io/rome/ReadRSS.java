package pw.mihou.amelia.io.rome;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import pw.mihou.amelia.io.Terminal;
import tk.mihou.amatsuki.impl.cache.CacheManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ReadRSS {

    private static final String format_cache = "AMELIA_FEEDS_%s_CACHE";

    public static Optional<SyndEntry> getLatest(String url){
        if(CacheManager.isCached(String.format(format_cache, url))){
            SyndEntry result = CacheManager.getCache(SyndEntryImpl.class, String.format(format_cache, url));
            if(result != null){
                return Optional.of(result);
            }
        }

        // We only need this cache when sending the same story to the channels that have the same one.
        return requestLatest(url).map(syndEntry -> CacheManager.addCache(syndEntry, String.format(format_cache, url), 2, TimeUnit.MINUTES));
    }

    public static Optional<SyndEntry> requestLatest(String url) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpUriRequest request = new HttpGet(url);
            request.setHeader("User-Agent", "Amelia/1.0r1 (Language=Java/1.8, Developer=Shindou Mihou)");
            try (CloseableHttpResponse response = client.execute(request);
                 InputStream stream = response.getEntity().getContent()) {
                return Optional.of(new SyndFeedInput().build(new XmlReader(stream)).getEntries().get(0));
            } catch (FeedException | IOException e) {
                e.printStackTrace();
                return Optional.empty();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
