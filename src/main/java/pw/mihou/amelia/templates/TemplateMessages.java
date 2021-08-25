package pw.mihou.amelia.templates;

public class TemplateMessages {

    public static final String SUCCESS_ACCOUNT_REMOVE = "✔ The account with the unique id: %d was removed.";
    public static final String NEUTRAL_LOADING = "<a:manaWinterLoading:880162110947094628> Please wait...";
    public static final String ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE = "❌ Amelia was unable to fetch the RSS feed from ScribbleHub, this can happen because of either the user doesn't have a story published yet or ScribbleHub is down.";
    public static final String ERROR_CHANNEL_NOT_FOUND = "❌ Amelia was unable to find the text channel, are you sure that I can **see**, **write** and **read** on the channel?";
    public static final String ERROR_FEED_NOT_FOUND = "❌ Amelia was unable to find the feed, are you sure it exists?";
    public static final String ERROR_DATE_NOT_FOUND = "❌ Amelia was unable to fetch the date of the feed, please try contacting our support team if it still doesn't work at https://manabot.fun/support";
    public static final String ERROR_INT_BELOW_ZERO = "❌ The ID provided was below numerical possibility, please try again with a proper number!";
    public static final String ERROR_INT_ABOVE_LIMIT = "❌ The feed number provided is not valid, please use `feeds` command to find the correct feed!";
    public static final String ERROR_MISSING_PERMISSIONS = "❌ You do not have permission to use this command, required permission: Manage Server, or lacking the required role to modify feeds.";
    public static final String ERROR_OPTION_TEXT_CHANNEL_REQUIRED = "❌ Please provide a **text channel** for the channel option!";
    public static final String ERROR_OPTION_CHANNEL_NOT_FOUND = "❌ There are no channels mentioned or the bot cannot write on the channel mentioned!";
    public static final String ERROR_NO_ACCOUNTS_ASSOCIATED = "❌ You do not have an account associated with the unique ID provided, please check `author me` for all accounts associated with your Discord account.";

}
