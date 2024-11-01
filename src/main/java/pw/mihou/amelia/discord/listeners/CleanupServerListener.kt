package pw.mihou.amelia.discord.listeners

import com.mongodb.client.model.Filters
import org.javacord.api.event.server.ServerLeaveEvent
import org.javacord.api.listener.server.ServerLeaveListener
import pw.mihou.amelia.db.MongoDB

object CleanupServerListener : ServerLeaveListener {
    override fun onServerLeave(event: ServerLeaveEvent) {
        MongoDB.client.getDatabase("amelia").getCollection("feeds").deleteMany(
            Filters.eq("server", event.server.id),
        )
    }
}
