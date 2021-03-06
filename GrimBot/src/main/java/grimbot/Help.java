package grimbot;

import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Help {
	private static String prefix = "";
	private static Message menu;
	private static HashMap<String, Message> embeds = new HashMap<String, Message>();
	
	public Help (String p, List<Plugin> plugins) {
		prefix = p;
		buildEmbeds(plugins);
	}
	
	public static void handle(MessageReceivedEvent event, String msg) {
		if (msg.split(" ").length == 1) {
			System.out.println("Attempting to send Message");
			sendMessage(event, menu);
		} else { 
			boolean found = false;
			for (Plugin p: Bot.plugins) {
				if ((p.pattern).matcher(msg.split(" ")[1]).matches()) {
					handlePluginHelp(event, p.getPrimaryAlias());
					found = true;
				}
			}
			if (!found) {
				sendString(event, "[Bot command was malformed. Type `"+prefix+"help` for more information.]");
			}
		}
	}
	
	public static void handlePluginHelp(MessageReceivedEvent event, String alias) {
		if (embeds.containsKey(alias)) {
			sendMessage(event, embeds.get(alias));
		} else {
			sendString(event, "[There is no plugin with the primary alias `"+alias+"`.]");
		}
	}
	
	private String buildMenuHelp(String alias, String usage) {
		String text = "";
		if (alias != null) {
			text +="\n`" + prefix + alias;
			if (usage != null) {
				text +="` - "+ usage;
			}
		}
		return text;
	}
	
	private String buildPluginHelp(Plugin p) {
		String name = p.getPrimaryAlias();
		String[] aliases = p.getOtherAliases();
		String[] params = p.getParameters();
		String desc = p.getDescription();
		String[] ex = p.getExamples();
		String text = "";
		
		if (name != null) {
			text +="\n**Command:** `" + prefix + name;
		}
		if (params != null) {
			for (int i=0; i<params.length; i++) text += " <" + params[i] + ">";
		}
		text += "`";
		if (aliases != null) {
			text += "\n\n**Aliases:**";
			for (int i=0; i<aliases.length; i++) text += " `" + prefix + aliases[i] + "`";
		}
		if (desc != null) {
			text +="\n\n**Description:** " + desc;
		}
		if (ex != null) {
			text += "\n\n**Examples:** ";
			for (int i=0; i<ex.length; i++) text += "\n`" + prefix + ex[i] + "`";
		}
		return text;
	}
	
	private Message buildMessage(String msg) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(java.awt.Color.BLACK);
        eb.setDescription(msg);
		//eb.setFooter("Bot built by Marzipanic#4639", event.getJDA().getUserById("140901708493619200").getEffectiveAvatarUrl());
        
		// Send direct message
		MessageEmbed e = eb.build();
        Message m = new MessageBuilder().setEmbed(e).build();
        return m;
	}
	
	private void buildEmbeds(List<Plugin> plugins) {
		String menuText = "__**Available Commands**__\n`" + prefix + "help <command name>` Displays command help.";
		String pluginText = "";
		for(Plugin p: plugins) {
			menuText += buildMenuHelp(p.getPrimaryAlias(), p.getUsage());
			pluginText = buildPluginHelp(p);
			embeds.put(p.getPrimaryAlias(), buildMessage(pluginText));
		}
		menu = buildMessage(menuText);
		System.out.println("Help Menu build successfully!");
	}
	
	private static void sendMessage(MessageReceivedEvent event, Message msg) {
		if (!event.isFromType(ChannelType.PRIVATE)) {
			event.getChannel().sendMessage(event.getAuthor().getAsMention() 
					+ " I sent you a direct message.").queue();
		}
		event.getAuthor().getPrivateChannel().sendMessage(msg).queue();
	}
	
	private static void sendString(MessageReceivedEvent event, String msg) {
		if (!event.isFromType(ChannelType.PRIVATE)) {
			event.getChannel().sendMessage(event.getAuthor().getAsMention() 
					+ " I sent you a direct message.").queue();
		}
		event.getAuthor().getPrivateChannel().sendMessage(msg).queue();
	}
}
