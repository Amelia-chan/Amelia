package pw.mihou.amelia.clients.listeners.impl;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.clients.listeners.interfaces.TrendingListener;
import pw.mihou.amelia.io.Scheduler;
import pw.mihou.amelia.payloads.AmeliaTrendingPayload;
import pw.mihou.amelia.templates.Embed;
import tk.mihou.amatsuki.entities.story.lower.StoryResults;

import java.util.concurrent.TimeUnit;

public class TrendingNotification implements TrendingListener {

    @Override
    public void onEvent(AmeliaTrendingPayload payload) {
        send(payload);
    }

    public void send(AmeliaTrendingPayload payload){
        if (!Amelia.shards.containsKey(0)) {
            Scheduler.schedule(() -> send(payload), 2, TimeUnit.SECONDS);
        } else {
            Amelia.shards.get(0).getUserById(payload.user).thenAccept(user -> user.sendMessage(notificationEmbed(payload.story)));
        }
    }

    private static EmbedBuilder notificationEmbed(StoryResults results) {
        Amelia.log.info("{}'s story: [{}] has reached trending, the user has been notified!", results.getCreator(), results.getName());
        return new Embed().setTitle("[Trending Notification]")
                .setDescription(String.format("`\uD83C\uDF89` Congratulations %s! Your story **[%s]** has trended on the frontpage (one of the 9 stories on the frontpage) of ScribbleHub! `\uD83C\uDF89`",
                        results.getCreator(), results.getName()))
                .setThumbnail(results.getThumbnail()).setFooter("Created by Shindou Mihou @ patreon.com/mihou").build();
    }

    @Override
    public String type() {
        return "trending";
    }

}
