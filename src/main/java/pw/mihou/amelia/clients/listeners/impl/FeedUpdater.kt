package pw.mihou.amelia.clients.listeners.impl

import pw.mihou.amelia.Amelia
import pw.mihou.amelia.io.rome.ItemWrapper
import pw.mihou.amelia.logger
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.nexus
import pw.mihou.amelia.session.AmeliaSession

object FeedUpdater {

    fun onEvent(item: ItemWrapper, feed: FeedModel) {
        nexus.shardManager.getShardOf(feed.server).ifPresent { shard ->
            shard.getServerById(feed.server).flatMap { server -> server.getTextChannelById(feed.channel) }.ifPresent serverCheck@{ channel ->
                if (item.date == null) {
                    logger.error("A feed was received that doesn't contain a date. [feed=${feed.feedUrl}]")
                    return@serverCheck
                }

                channel.sendMessage(Amelia.format(item, feed, channel.server)).thenAccept {
                    AmeliaSession.feedsUpdated.incrementAndGet()
                    logger.info("I have sent a feed update to a server with success. [feed=${feed.feedUrl}, server=${channel.server.id}]")
                }.exceptionally { exception ->
                    logger.error("Failed to send update for a feed to a server. [feed=${feed.feedUrl}, server=${channel.server.id}]", exception)
                    return@exceptionally null
                }
            }
        }
    }

}