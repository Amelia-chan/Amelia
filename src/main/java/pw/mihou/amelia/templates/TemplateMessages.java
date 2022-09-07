package pw.mihou.amelia.templates;

public class TemplateMessages {

    public static final String NEUTRAL_LOADING = "<a:manaWinterLoading:880162110947094628> Please wait...";
    public static final String ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE = "❌ Amelia was unable to fetch the RSS feed from ScribbleHub, this can happen because of either the user doesn't have a story published yet or ScribbleHub is down.";
    public static final String ERROR_CHANNEL_NOT_FOUND = "❌ Amelia was unable to find the text channel, are you sure that I can **see**, **write** and **read** on the channel?";
    public static final String ERROR_FEED_NOT_FOUND = "❌ Amelia was unable to find the feed, are you sure it exists?";

    public static final String ERROR_FAILED_TO_PERFORM_ACTION = "❌ An error occurred while trying to perform this action, please create an issue in https://github.com/Amelia-chan/Amelia/issues/.";

    public static final String ERROR_INVALID_READING_LIST_LINK =
            "❌ The link provided doesn't meet the specifications for reading list feed, please read https://github.com/Amelia-chan/Amelia/discussions/19 for more information.";

    public static final String ERROR_DATE_NOT_FOUND = "❌ Amelia was unable to fetch the date of the feed, please try contacting our support team if it still doesn't work at **amelia@mihou.pw**";
    public static final String ERROR_DATABASE_FAILED = "❌ Amelia was unable to complete this action because of some database issue, please try contacting our support team if it still doesn't work at **amelia@mihou.pw**";
    public static final String ERROR_MISSING_PERMISSIONS = "❌ You do not have permission to use this command, required permission: **Manage Server** or **Manage Channels**.";
}
