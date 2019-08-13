package to.thepiratebay.feeds;

import java.util.Arrays;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import reactor.util.Logger;
import reactor.util.Loggers;
import to.thepiratebay.ThePirateBay;
import to.thepiratebay.stc.StaticID;

public class AdminFeed {
	
	private Logger log = Loggers.getLogger(AdminFeed.class);
	private final ThePirateBay bay;
	public AdminFeed(ThePirateBay bay) {
		this.bay = bay;
	}

	private void msg(String text) {
		bay.getClient().getChannelById(StaticID.ADMIN)
			.cast(TextChannel.class)
			.flatMap(channel -> channel.createMessage(text))
			.block();
	}
	
	public void onMessage(MessageCreateEvent event) {
		Message msg = event.getMessage();
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

		switch(cmd) {

		case "cleanup":
			if(args.length == 1) {
				try {
					long id = Long.parseLong(args[0]);
					
					try {
						bay.getUsersDB().cleanupUser(id);
						msg("User cleanup... :white_check_mark:");
					} catch(NullPointerException ex) {
						msg("Utilisateur inconnu");
					}
				} catch(NumberFormatException e) {
					msg("Il faut un chiffre comme arg");
				}
			} else {
				msg("Mauvais format");
			}
			break;
			
		}
	
	}
	
}
