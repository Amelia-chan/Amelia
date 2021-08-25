package pw.mihou.amelia.clients.listeners.impl;

import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.clients.listeners.interfaces.FeedListener;
import pw.mihou.amelia.payloads.AmeliaPayload;
import pw.mihou.amelia.session.AmeliaSession;

public class FeedUpdater implements FeedListener {

    @Override
    public void onEvent(AmeliaPayload payload) {
        Amelia.shards.forEach((integer, api) -> api.getServerTextChannelById(payload.model.getChannel())
                .ifPresent(textChannel -> payload.wrapper.getPubDate()
                        .ifPresent(date -> textChannel.sendMessage(Amelia.format(payload.wrapper, payload.model, textChannel.getServer()))
                                .whenComplete((message, throwable) -> {
                                    AmeliaSession.feedsUpdated.incrementAndGet();

                                    if (throwable != null) {
                                        Amelia.log.error("We were unable to send feed update [{}] to server {}!", payload.model.getUnique(),
                                                textChannel.getServer().getName());
                                    } else {
                                        Amelia.log.info("Amelia has finished (or has attempted) sending feed [{}] to {}!", payload.model.getUnique(),
                                                textChannel.getServer().getName());
                                    }
                                }))));
    }

    @Override
    public String type() {
        return "feed";
    }
}
