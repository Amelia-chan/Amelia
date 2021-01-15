package pw.mihou.amelia.commands.settings;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.base.db.ServerDB;
import pw.mihou.amelia.commands.db.MessageDB;
import pw.mihou.amelia.models.ServerModel;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.amelia.templates.Message;

public class Settings extends Command {

    public Settings(){
        super("settings", "Modifies the settings for Amelia-chan in the server.", "settings prefix [prefix], settings limit, settings role [@role]", false);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        if(server.canManage(user) || server.canManageRoles(user) || server.canCreateChannels(user) || server.isAdmin(user) || server.isOwner(user)){
            if(args.length > 1){
                if(args[1].equalsIgnoreCase("prefix")){
                    if(args.length > 2) {
                        String prefix = event.getMessageContent().replace(args[0] + " " + args[1] + " ", "");
                        ServerDB.getServer(server.getId()).setPrefix(prefix).update().thenAccept(unused -> Message.msg("The prefix has now been changed to `"+prefix+"`").send(event.getChannel()));
                    } else {
                        Message.msg("Error: Invalid arguments, missing prefix value.").send(event.getChannel());
                    }
                } else if(args[1].equalsIgnoreCase("limit")){
                    if(server.isAdmin(user) || server.isOwner(user)) {
                        ServerModel model = ServerDB.getServer(server.getId());
                        model.setLimit(!model.getLimit()).update().thenAccept(unused -> {
                            if (!ServerDB.getServer(server.getId()).getLimit()) {
                                Message.msg("**__[WARNING]: ANARCHY MODE HAS BEEN ACTIVATED. WE DO NOT RECOMMEND THIS MODE! [WARNING]__**")
                                        .send(event.getChannel()).thenAccept(message -> Message.msg("What is Anarchy Mode?" +
                                                "\nAnarchy mode is as it means, the removal of the role, and permission limitations for all commands (except settings) for the bot." +
                                                "\n- This mode will allow any users to exploit the command to mention any roles, remove feeds, add feeds at their own will." +
                                                "\n- **__WE HIGHLY RECOMMEND YOU DISABLE THIS BY USING THE COMMAND ONCE MORE__**")
                                                .send(event.getChannel()));
                            } else {
                                Message.msg("Your server is now safe from exploitation.").send(event.getChannel());
                            }
                        });
                    } else {
                        Message.msg("Due to the high-risks of this command, we have disabled **ANY** attempt to use this unless you are the server administrator or owner.")
                                .send(event.getChannel());
                    }
                } else if(args[1].equalsIgnoreCase("role")){
                    if(!event.getMessage().getMentionedRoles().isEmpty()){
                        ServerDB.getServer(server.getId()).setRole(event.getMessage().getMentionedRoles().get(0).getId()).update().thenAccept(unused -> Message.msg("We have now updated the configuration to allow " + event.getMessage().getMentionedRoles().get(0).getMentionTag() + " to edit, add, and remove RSS feeds and also subscribe roles to RSS feeds at will." +
                                "\nTo reset this config, please do `settings role reset`.")
                                .setAllowedMentions(new AllowedMentionsBuilder().setMentionUsers(false).setMentionEveryoneAndHere(false).setMentionRoles(false).build())
                                .send(event.getChannel()));
                    } else {
                        if(args.length > 2){
                            if(args[2].equalsIgnoreCase("reset")){
                                ServerDB.getServer(server.getId()).setRole(0L).update().thenAccept(unused -> Message.msg("We have reset the role configuration.").send(event.getChannel()));
                            } else {
                                Message.msg("Error: Invalid arguments.").send(event.getChannel());
                            }
                        } else {
                            Message.msg("Error: Invalid arguments.").send(event.getChannel());
                        }
                    }
                } else if(args[1].equalsIgnoreCase("message")){
                    if(args.length > 2){
                        String message = event.getMessageContent().replaceFirst(args[0] + " " + args[1] + " ", "");
                        MessageDB.setFormat(server.getId(), message);
                        Message.msg("The message format has now been changed.").send(event.getChannel());
                    } else {
                        Message.msg("Error: Please type the message you wish to use for all feeds. \n" +
                                "**Placeholders**" +
                                "\n- **{title}**, the title of the chapter." +
                                "\n- **{author}**, the author of the story." +
                                "\n- **{link}**, the link to the chapter." +
                                "\n- **{subscribed}**, mentions of all subscribed roles.").send(event.getChannel());
                    }
                }
            } else {
              Message.msg(embed(server)).send(event.getChannel());
            }
        } else {
            Message.msg(embed(server)).send(event.getChannel());
        }
    }

    private EmbedBuilder embed(Server server){
        ServerModel model = ServerDB.getServer(server.getId());
        return new Embed().setTitle("Server Settings")
                .setDescription("Here are your current server settings.")
                .build().addInlineField("Limit", (model.getLimit() ? "`Active`" : "`Inactive (WARNING)`"))
                .addInlineField("Prefix", "`"+model.getPrefix()+"`")
                .addInlineField("RSS Manager Role", (model.getRole().isPresent() ? server.getRoleById(model.getRole().get()).map(Role::getMentionTag).orElse("`Disabled`") : "`Disabled`"))
                .addField("Message Format", MessageDB.getFormat(server.getId()));
    }
}
