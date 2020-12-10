package pw.mihou.amelia.io.rome;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ReadRSS {

    public static Optional<SyndEntry> getLatest(String url) {
        try (CloseableHttpClient client = HttpClients.createMinimal()) {
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
