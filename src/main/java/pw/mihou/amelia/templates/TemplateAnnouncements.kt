package pw.mihou.amelia.templates

import org.javacord.api.entity.message.embed.EmbedBuilder
import java.awt.Color

object TemplateAnnouncements {

    val UNSUPPORTED_STORY_FEEDS = EmbedBuilder().setColor(Color.RED)
        .setTitle("Story feeds are now unsupported!")
        .setDescription(
            "As the title mentions, all story feeds are now disabled "
                    + "due to a recent change in ScribbleHub "
                    + "([statement from Tony](https://forum.scribblehub.com/threads/cloudflare-blocking-rss-feeds.11117/post-243945)) "
                    + "that blocks all access to individual (series) feeds. "
                    + "\n\n" +
                    "To resolve this issue, Amelia-chan will support reading list feeds within either today or the next couple of days. " +
                    "If you want to keep watch for that change, feel free to watch on [Source Code](https://github.com/Amelia-chan/Amelia) or " +
                    "[Announcements](https://github.com/Amelia-chan/Amelia/discussions/categories/announcements)." +
                    "\n\n" +
                    "*This channel is receiving this message because it has story feeds registered onto it. To migrate onto author feeds, please " +
                    "follow this guide: https://github.com/Amelia-chan/Amelia/discussions/18*" +
                    "\n\n" +
                    "*In September 10, 2022, all story feeds stored in Amelia's database will be removed and a backup will be available for people who " +
                    "wants to retrieve the feeds for their servers, more information will be available in " +
                    "[Announcements](https://github.com/Amelia-chan/Amelia/discussions/categories/announcements) by then. " +
                    "For now, all your feeds are accessible under the `/feeds` command.*"
        )
        .setFooter("Shindou Mihou", "https://cdn.discordapp.com/avatars/584322030934032393/991d5b2c105bb5dd82634decb3fdbafc.png?size=2048")

}