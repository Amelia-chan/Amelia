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
            val item = Amelia.moshi.adapter(ItemWrapper::class.java).fromJson(payload.getString("wrapper"))!!

            CompletableFuture.runAsync { FeedUpdater.onEvent(item, feed) }
        }
    }

}