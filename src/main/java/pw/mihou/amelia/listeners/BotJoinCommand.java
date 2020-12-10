package pw.mihou.amelia.listeners;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.server.ServerJoinListener;
import pw.mihou.amelia.commands.base.db.ServerDB;
import pw.mihou.amelia.models.ServerModel;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.amelia.templates.Message;

public class BotJoinCommand implements ServerJoinListener {

    @Override
    public void onServerJoin(ServerJoinEvent event) {
        event.getServer().getSystemChannel().ifPresent(tc -> {
            Message.msg(embed().setThumbnail(event.getApi().getYourself().getAvatar())).send(tc);
        });
        ServerDB.addServer(new ServerModel(event.getServer().getId(), "a.", true, 0L));
    }

    private EmbedBuilder embed(){
        return new Embed().setTitle("Hello! I am Amelia-chan!")
                .setDescription("Good day, y'all! I am Amelia-chan, here to support you! *powh*")
                .build().addInlineField("What can I do?", "Give you updates to your favorite stories!")
                .addInlineField("How can I do that?", "First, check my help command using `a.help`!");
    }
}
