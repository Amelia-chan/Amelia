package pw.mihou.amelia.discord.listeners

import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.event.connection.ReconnectEvent
import org.javacord.api.event.connection.ResumeEvent
import org.javacord.api.listener.connection.ReconnectListener
import org.javacord.api.listener.connection.ResumeListener
import pw.mihou.amelia.configuration.Configuration

object ActivityListener : ResumeListener, ReconnectListener {
    override fun onResume(event: ResumeEvent) {
        event.api.updateActivity(ActivityType.WATCHING, Configuration.APP_ACTIVITY)
    }

    override fun onReconnect(event: ReconnectEvent) {
        event.api.updateActivity(ActivityType.WATCHING, Configuration.APP_ACTIVITY)
    }
}
