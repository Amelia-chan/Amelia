package pw.mihou.amelia.discord.commands.templates

import pw.mihou.amelia.configuration.Configuration

object TemplateMessages {
    val NEUTRAL_LOADING: String = "${Configuration.LOADING_EMOJI} Please wait..."

    val ERROR_NO_USERS_FOUND: String =
        "❌ ${Configuration.APP_NAME} cannot found any users that matches the query, how about trying something else?"

    const val ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE: String =
        "❌ Failed to connect to ScribbleHub. It's possible that the site is down or having issues."

    val ERROR_RSSSCRIBBLEHUB_NOT_ACCESSIBLE: String =
        "❌ ${Configuration.APP_NAME} was unable to fetch the RSS feed from ScribbleHub, " +
            "this can happen because of either the user doesn't have a story published yet " +
            "or ScribbleHub's RSS server is down."

    val ERROR_CHANNEL_NOT_FOUND: String =
        "❌ ${Configuration.APP_NAME} was unable to find the text channel," +
            " are you sure that I can **see**, **write** and **read** on the channel?"

    val ERROR_FEED_NOT_FOUND: String =
        "❌ ${Configuration.APP_NAME} was unable to find the feed, are you sure it exists?"

    val ERROR_FAILED_TO_PERFORM_ACTION: String =
        "❌ An error occurred while trying to perform this action, " +
            "please create an issue in https://github.com/Amelia-chan/Amelia/issues/."

    val ERROR_INVALID_READING_LIST_LINK: String =
        "❌ The link provided doesn't meet the specifications for reading list feed, " +
            "please read https://github.com/Amelia-chan/Amelia/discussions/19 for more information."

    val ERROR_DATE_NOT_FOUND: String =
        "❌ ${Configuration.APP_NAME} was unable to fetch the date of the feed, please try " +
            "contacting our support team if it still doesn't work at **amelia@mihou.pw**"

    val ERROR_DATABASE_FAILED: String =
        "❌ ${Configuration.APP_NAME} was unable to complete this action because of some database " +
            "issue, please try contacting our support team if it still doesn't work at " +
            "**amelia@mihou.pw**"

    val ERROR_MISSING_PERMISSIONS: String =
        "❌ You do not have permission to use this command, required permission: " +
            "**Manage Server** or **Manage Channels**."
}
