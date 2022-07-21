package pw.mihou.amelia.clients.listeners

import org.json.JSONObject
import pw.mihou.amelia.Amelia
import pw.mihou.amelia.clients.listeners.impl.FeedUpdater
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.io.rome.ItemWrapper
import java.util.concurrent.CompletableFuture

object ListenerManager {

    @JvmStatic
    fun dispatch(message: JSONObject) {
        if (message.getString("payload_type").equals("feed", ignoreCase = true)) {
            val payload = JSONObject(message.getString("payload"))
            val feed = FeedDatabase.get(payload.getJSONObject("model").getLong("unique"))!!
            val wrapper = payload.getJSONObject("wrapper")
            val item = ItemWrapper(
                title = wrapper.getString("title"),
                date = Amelia.websocketFormatter.parse(wrapper.getString("date")),
                author = wrapper.getString("author"),
                link = wrapper.getString("link"),
                description = wrapper.getString("description")
            )

            CompletableFuture.runAsync { FeedUpdater.onEvent(item, feed) }
        }
    }

}