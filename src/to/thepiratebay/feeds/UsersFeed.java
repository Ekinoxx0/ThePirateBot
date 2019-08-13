package to.thepiratebay.feeds;

import java.util.Arrays;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import reactor.util.Logger;
import reactor.util.Loggers;
import to.thepiratebay.ThePirateBay;

public class UsersFeed {

	private Logger log = Loggers.getLogger(UsersFeed.class);
	private final ThePirateBay bay;
	public UsersFeed(ThePirateBay bay) {
		this.bay = bay;
	}

	private void msg(Snowflake channelID, String text) {
		bay.getClient().getChannelById(channelID)
			.cast(TextChannel.class)
			.flatMap(channel -> channel.createMessage(text))
			.block();
	}
	
	public void onMessage(MessageCreateEvent event) {
		Message msg = event.getMessage();
		TextChannel channel = msg.getChannel().cast(TextChannel.class).block();
		if(!msg.getContent().isPresent()) return;
		
		String rawText = msg.getContent().get();
		
		if(rawText.startsWith("!") 
				|| rawText.startsWith("*") 
				|| rawText.startsWith("\\") 
				|| rawText.startsWith(".") 
				|| rawText.startsWith("/") 
				|| rawText.startsWith("_") 
				|| rawText.startsWith("#")) {
			rawText = rawText.substring(1);
		} else {
			
			return;
		}
		
		log.info("/" + rawText);
		String[] splitted = rawText.split(" ");
		String cmd = splitted[0].toLowerCase();
		String[] args = Arrays.copyOfRange(splitted, 1, splitted.length);
		
		Snowflake channelId = event.getMessage().getChannelId();

		switch(cmd) {

		case "msg":
		case "m":
		case "t":
		case "tell":
		case "txt":
			if(args.length >= 2) {
				if(!bay.getIdDB().isValid(args[0])){
					msg(channelId, "ID Inconnu...");
					return;
				}
				
				String txt = rawText.replace(cmd + " " + args[0] + " ", "");
				
				this.bay.getIdDB().getMembers(args[0])
					.flatMap(member -> this.bay.getUsersDB().getFromMemberMono(member))
					.flatMap(user -> this.bay.getClient().getChannelById(user.getChannelId()))
					.cast(TextChannel.class)
					.subscribe(tChannel -> tChannel.createEmbed(embed -> {
						embed.setTitle("``" + channel.getName() + "`` -> ``" + args[0] + "``");
						embed.setDescription(txt);
					}));
			} else {
				msg(channelId, "Utilisez **!msg <ID> <MESSAGE>**\n"
							+  "*!msg `abc1` Hey, tu aurai de la ... d'ici demain, on se retrouve devant le parking...*");
			}
			break;
			
		}
	
	}
	
	
	
	
}
