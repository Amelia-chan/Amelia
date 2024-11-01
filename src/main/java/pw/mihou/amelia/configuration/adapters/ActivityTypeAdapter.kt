package pw.mihou.amelia.configuration.adapters

import org.javacord.api.entity.activity.ActivityType
import pw.mihou.envi.adapters.standard.EnviFieldAdapter

object ActivityTypeAdapter : EnviFieldAdapter<ActivityType> {
    override fun adapt(contents: String): ActivityType =
        when (contents.lowercase()) {
            "watching" -> ActivityType.WATCHING
            "listening" -> ActivityType.LISTENING
            "playing" -> ActivityType.PLAYING
            "streaming" -> ActivityType.STREAMING
            "custom" -> ActivityType.CUSTOM
            "competing" -> ActivityType.COMPETING
            else -> throw IllegalArgumentException("Invalid activity type: $contents")
        }
}
