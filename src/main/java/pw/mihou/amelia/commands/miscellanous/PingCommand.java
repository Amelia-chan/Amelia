package pw.mihou.amelia.commands.miscellanous;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.templates.Embed;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Pings the bot to test if it is alive.", "ping", false);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        long start = Instant.now().toEpochMilli();
        String uptime = uptime();
        event.getApi().measureRestLatency().thenAccept(rest -> {
            long gateway = event.getApi().getLatestGatewayLatency().toMillis();
            event.getMessage().reply(new Embed().setTitle("Ping! Pong!").setThumbnail("https://miro.medium.com/max/256/1*dKSSlnsTw2M-VJMl_ROSdA.png").build()
                    .addField("Statistics", "<:download:778447509684748288> Ping: Average latency is " + (Instant.now().toEpochMilli() - start) + "ms" +
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

    private long getUsedMemory(){
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
}
