package pw.mihou.amelia.commands.miscellanous;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.session.AmeliaSession;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.amelia.utility.StringUtils;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Ping implements VelenEvent, VelenSlashEvent {

    private static final com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    @Override
    public void onEvent(MessageCreateEvent event, Message msg, User user, String[] args) {
        event.getApi().measureRestLatency().thenAccept(duration -> {
            long gateway = event.getApi().getLatestGatewayLatency().toMillis();
            msg.reply(
                    new Embed()
                            .setTitle("Pang! Pong!")
                            .setDescription("Here is the current information of Amelia.")
                            .setThumbnail(event.getApi().getYourself().getAvatar().getUrl().toExternalForm())
                            .build()
                            .addField("Latency", StringUtils.createEmbeddedFormat(
                                    "⏰ Gateway: `" + format(gateway) + "ms`",
                                    "🕚 REST: `" + format(duration.toMillis()) + "ms`"
                            ))
                            .addField("Bot Information", StringUtils.createEmbeddedFormat(
                                    "🕰 Uptime: `" + uptime() + "`",
                                    "💻 Servers: `" + event.getApi().getServers().size() + " servers`",
                                    "💿 Memory: `" + format(getUsedMemory()) + " MB / " + format(os.getTotalPhysicalMemorySize() / (1000 * 1000)) + " MB`"
                            ))
                            .addField("Session Information", StringUtils.createEmbeddedFormat(
                                    "☁ Total updates sent: `" + format(AmeliaSession.feedsUpdated.get()) + " chapters notified to servers`",
                                    "🌩 Total trending notifications: `" + format(AmeliaSession.trendingNotified.get()) + " users notified`"
                            ))
                            .addField("Support Amelia", "You can support Amelia through [Patreon](https://patreon.com/manabot)")
            );
        });
    }

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        event.respondLater().thenAccept(updater -> event.getApi().measureRestLatency().thenAccept(duration -> {
            long gateway = event.getApi().getLatestGatewayLatency().toMillis();
            updater.addEmbed(
                    new Embed()
                            .setTitle("Pang! Pong!")
                            .setDescription("Here is the current information of Amelia.")
                            .setThumbnail(event.getApi().getYourself().getAvatar().getUrl().toExternalForm())
                            .build()
                            .addField("Latency", StringUtils.createEmbeddedFormat(
                                    "⏰ Gateway: `" + format(gateway) + "ms`",
                                    "🕚 REST: `" + format(duration.toMillis()) + "ms`"
                            ))
                            .addField("Bot Information", StringUtils.createEmbeddedFormat(
                                    "🕰 Uptime: `" + uptime() + "`",
                                    "💻 Servers: `" + event.getApi().getServers().size() + " servers`",
                                    "💿 Memory: `" + format(getUsedMemory()) + " MB / " + format(os.getTotalPhysicalMemorySize() / (1000 * 1000)) + " MB`"
                            ))
                            .addField("Session Information", StringUtils.createEmbeddedFormat(
                                    "☁ Total updates sent: `" + format(AmeliaSession.feedsUpdated.get()) + " chapters notified to servers`",
                                    "🌩 Total trending notifications: `" + format(AmeliaSession.trendingNotified.get()) + " users notified`"
                            ))
                            .addField("Support Amelia", "You can support Amelia through [Patreon](https://patreon.com/manabot)")
            ).update();
        }));
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

    /**
     * Formats the number into human readable content.
     *
     * @param number The number to format.
     * @return The formatted string.
     */
    private static String format(long number) {
        return NumberFormat.getInstance().format(number);
    }
}
