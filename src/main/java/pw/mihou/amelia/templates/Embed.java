package pw.mihou.amelia.templates;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import pw.mihou.amelia.utility.StringUtils;

import java.awt.*;
import java.io.InputStream;

public class Embed {

    private EmbedBuilder embed;

    public Embed() {
        embed = new EmbedBuilder().setFooter("Patreon @ patreon.com/mihou").setColor(ColorSpace.defaultColor())
                .setTimestampToNow();
    }

    /**
     * Sets the embed's description.
     *
     * @param description the description for the embed.
     * @return builder.
     */
    public Embed setDescription(String description) {
        // Limits the embed to 2048 characters in order to avoid errors.
        embed.setDescription(StringUtils.stripToLengthWhileDotsEnd(description, 2048));
        return this;
    }

    public Embed setThumbnail(Icon icon) {
        embed.setThumbnail(icon);
        return this;
    }

    public Embed setThumbnail(String url) {
        embed.setThumbnail(url);
        return this;
    }

    /**
     * Sets the embed's title.
     *
     * @param title the title for the embed.
     * @return builder.
     */
    public Embed setTitle(String title) {
        // Limits the title to 256 characters as specified by Discord.
        embed.setTitle(StringUtils.stripToLength(title, 256));
        return this;
    }

    public Embed setFooter(String footer) {
        embed.setFooter(StringUtils.stripToLength(footer, 2048));
        return this;
    }

    public Embed setFooter(String footer, String icon) {
        embed.setFooter(StringUtils.stripToLength(footer, 2048), icon);
        return this;
    }

    public Embed setAuthor(String author) {
        embed.setAuthor(StringUtils.stripToLength(author, 256));
        return this;
    }

    public Embed setAuthor(String author, String icon) {
        embed.setAuthor(StringUtils.stripToLength(author, 256), "https://mihou.pw", icon);
        return this;
    }

    public Embed setAuthor(User user) {
        embed.setAuthor(user);
        return this;
    }

    public Embed setAuthor(String author, String url, String icon) {
        embed.setAuthor(StringUtils.stripToLength(author, 256), url, icon);
        return this;
    }

    public Embed setColor(Color color) {
        embed.setColor(color);
        return this;
    }

    public Embed attachImage(String url) {
        embed.setImage(url);
        return this;
    }

    public Embed attachImage(InputStream stream) {
        embed.setImage(stream);
        return this;
    }

    public EmbedBuilder build() {
        return embed;
    }

}
