package pw.mihou.amelia.commands.miscellanous;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Ping implements VelenEvent, VelenSlashEvent {

    @Override
    public void onEvent(MessageCreateEvent event, Message msg, User user, String[] args) {
        long start = Instant.now().toEpochMilli();
        String uptime = uptime();
        event.getApi().measureRestLatency().thenAccept(rest -> {
            long gateway = event.getApi().getLatestGatewayLatency().toMillis();
            msg.reply(new Embed().setTitle("Ping! Pong!")
                    .setThumbnail("https://miro.medium.com/max/256/1*dKSSlnsTw2M-VJMl_ROSdA.png")
                    .build()
                    .addField("Statistics", "<:download:778447509684748288> Ping: Average latency is " +
                            (Instant.now().toEpochMilli() - start) + "ms" +
                            "\n<:upload:778550310347997194> Gateway Latency: " + gateway + "ms" +
                            "\n<:latency:778551868301246485> Rest Latency: " + rest.toMillis() + "ms" +
                            "\n<:shards:778551235551690752> Total Shards: " + event.getApi().getTotalShards() + " shards" +
                            "\n<:server:778550786518548481> Total Servers: " + event.getApi().getServers().size() + " servers" +
                            "\n<:uptime:778552145406459924> Uptime: " + uptime +
                            "\n<:memory:778552648029831208> RAM: " + getUsedMemory() + " MB").setAuthor(user))
                    .thenAccept(message -> message.edit(new Embed().setTitle("Ping! Pong!").setThumbnail("https://miro.medium.com/max/256/1*dKSSlnsTw2M-VJMl_ROSdA.png").build()
                            .addField("Statistics", "<:download:778447509684748288> Ping: Average latency is " + (Instant.now().toEpochMilli() - start) + "ms" +
                                    "\n<:upload:778550310347997194> Gateway Latency: " + gateway + "ms" +
                                    "\n<:latency:778551868301246485> Rest Latency: " + rest.toMillis() + "ms" +
                                    "\n<:shards:778551235551690752> Total Shards: " + event.getApi().getTotalShards() + " shards" +
                                    "\n<:server:778550786518548481> Total Servers: " + event.getApi().getServers().size() + " servers" +
                                    "\n<:uptime:778552145406459924> Uptime: " + uptime +
                                    "\n<:memory:778552648029831208> RAM: " + getUsedMemory() + " MB").setAuthor(user)));
        });
    }

    private long getUsedMemory() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1000 * 1000);
    }

    public String uptime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        return String.format("%d days, %d hours, %d minutes, %d seconds",
                TimeUnit.MILLISECONDS.toDays(uptime),
                TimeUnit.MILLISECONDS.toHours(uptime) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(uptime)),
                TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime)),
                TimeUnit.MILLISECONDS.toSeconds(uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime))
        );
    }


    @Override
    public void onEvent(SlashCommandCreateEvent slashCommandCreateEvent,
                        SlashCommandInteraction event,
                        User user, VelenArguments velenArguments,
                        List<SlashCommandInteractionOption> list,
                        InteractionImmediateResponseBuilder interactionImmediateResponseBuilder) {
        event.respondLater().thenAccept(updater -> {
            long start = Instant.now().toEpochMilli();
            String uptime = uptime();
            event.getApi().measureRestLatency().thenAccept(rest -> {
                long gateway = event.getApi().getLatestGatewayLatency().toMillis();
                updater.addEmbed(new Embed().setTitle("Ping! Pong!")
                                .setThumbnail("https://miro.medium.com/max/256/1*dKSSlnsTw2M-VJMl_ROSdA.png")
                                .build()
                                .addField("Statistics", "<:download:778447509684748288> Ping: Average latency is " +
                                        (Instant.now().toEpochMilli() - start) + "ms" +
                                        "\n<:upload:778550310347997194> Gateway Latency: " + gateway + "ms" +
                                        "\n<:latency:778551868301246485> Rest Latency: " + rest.toMillis() + "ms" +
                                        "\n<:shards:778551235551690752> Total Shards: " + event.getApi().getTotalShards() + " shards" +
                                        "\n<:server:778550786518548481> Total Servers: " + event.getApi().getServers().size() + " servers" +
                                        "\n<:uptime:778552145406459924> Uptime: " + uptime +
                                        "\n<:memory:778552648029831208> RAM: " + getUsedMemory() + " MB").setAuthor(user))
                        .update()
                        .thenAccept(message -> message.edit(new Embed().setTitle("Ping! Pong!").setThumbnail("https://miro.medium.com/max/256/1*dKSSlnsTw2M-VJMl_ROSdA.png").build()
                                .addField("Statistics", "<:download:778447509684748288> Ping: Average latency is " + (Instant.now().toEpochMilli() - start) + "ms" +
                                        "\n<:upload:778550310347997194> Gateway Latency: " + gateway + "ms" +
                                        "\n<:latency:778551868301246485> Rest Latency: " + rest.toMillis() + "ms" +
                                        "\n<:shards:778551235551690752> Total Shards: " + event.getApi().getTotalShards() + " shards" +
                                        "\n<:server:778550786518548481> Total Servers: " + event.getApi().getServers().size() + " servers" +
                                        "\n<:uptime:778552145406459924> Uptime: " + uptime +
                                        "\n<:memory:778552648029831208> RAM: " + getUsedMemory() + " MB").setAuthor(user)));
            });
        });
    }
}
