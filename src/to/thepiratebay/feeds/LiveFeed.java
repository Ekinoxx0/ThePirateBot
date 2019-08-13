package to.thepiratebay.feeds;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import to.thepiratebay.ThePirateBay;
import to.thepiratebay.stc.StaticID;

public class LiveFeed {
	
	private final ThePirateBay bay;
	
	public LiveFeed(ThePirateBay bay) {
		this.bay = bay;
	}

	public void newUser(Member member, String id) {
		bay.getClient().getChannelById(StaticID.LIVE)
			.cast(TextChannel.class)
			.flatMap(r -> r.createMessage("**Nouvel utilisateur ->** " + member.getUsername() + " : `" + id + "`"))
			.block();
	}

}
