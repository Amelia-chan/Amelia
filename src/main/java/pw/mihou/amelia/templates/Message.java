package pw.mihou.amelia.templates;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class Message {

    /**
     * A normal message builder.
     *
     * @param message the message to send.
     * @return a message builder.
     */
    public static MessageBuilder msg(String message) {
        return new MessageBuilder().setContent(message);
    }

    /**
     * A message builder that uses an embed.
     *
     * @param embed the embed to send.
     * @return a message builder.
     */
    public static MessageBuilder msg(EmbedBuilder embed) {
        return new MessageBuilder().setEmbed(embed);
    }

}
