package to.thepiratebay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import reactor.util.Logger;
import reactor.util.Loggers;
import to.thepiratebay.feeds.AdminFeed;
import to.thepiratebay.feeds.LiveFeed;
import to.thepiratebay.feeds.PoliceFeed;
import to.thepiratebay.feeds.UsersFeed;
import to.thepiratebay.stc.StaticFunction;
import to.thepiratebay.stc.StaticID;
import to.thepiratebay.users.IdDB;
import to.thepiratebay.users.UsersDB;

public class ThePirateBay {
	
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private DiscordClient client;
	private Logger log;
	
	private AdminFeed admin;
	private LiveFeed live;
	private PoliceFeed police;
	private UsersFeed usersFeed;
	
	private UsersDB users;
	private IdDB ids;
	
	public ThePirateBay() {
		this.log = Loggers.getLogger("TPB");
		
		try {
			this.users = new UsersDB(this.client, this);
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Unable to proceed to UsersDB creation...");
			return;
		}
		
		try {
			this.ids = new IdDB(this.client);
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Unable to proceed to IdDB creation...");
			return;
		}
		
		this.admin = new AdminFeed(this);
		this.live = new LiveFeed(this);
		this.police = new PoliceFeed(this);
		this.usersFeed = new UsersFeed(this);
		
		this.client = new DiscordClientBuilder("NjEwMjYyMzU5OTc3NjIzNTcx.XVFrcQ.km-7iAyVrgL6HgXq9_k1zQjsSa4").build();

		EventDispatcher ed = this.client.getEventDispatcher();
		
		ed.on(ReadyEvent.class).subscribe(this::onReady);
		ed.on(MessageCreateEvent.class)
			.filter(msg -> msg.getMember().isPresent() && !msg.getMember().get().isBot() && msg.getMessage().getChannelId().equals(StaticID.ADMIN))
			.subscribe(event -> this.admin.onMessage(event));
		ed.on(MessageCreateEvent.class)
			.filter(msg -> msg.getMember().isPresent() && !msg.getMember().get().isBot())
			.subscribe(this::onMessage);
		ed.on(MemberJoinEvent.class).subscribe(this::onJoin);
		ed.on(ReactionAddEvent.class).filter(r -> r.getGuildId().isPresent()).subscribe(this::onReaction);
		
		this.client.login().block();
	}
	
	private void onReady(ReadyEvent ready) {
		log.info("ThePirateBay is now logged.");
        client.updatePresence(Presence.online(Activity.watching("le réseau crypté"))).block();
        //StaticFunction.updateInformation(client);
        StaticFunction.anonymeAllMembers(client);
	}
	
	private void onReaction(ReactionAddEvent react) {
		Member member = (Member) react.getUser().block().asMember(StaticID.GUILD).block();
		
		if(react.getChannelId().equals(StaticID.INFO)) {
			if(!this.getUsersDB().exist(member)) {
				log.info(member.getUsername() + " has reacted to #information message, creating new user.");
				this.getUsersDB().createUser(member);
			}
		}
	}
	
	private void onMessage(MessageCreateEvent msg) {
		Category cat = (Category) this.client.getChannelById(StaticID.PRIVATE_CAT).block();
		if(cat.getChannels().any(channel -> channel.equals((GuildChannel) msg.getMessage().getChannel().block())).block()){
			this.usersFeed.onMessage(msg);
		}
	}
	
	private void onJoin(MemberJoinEvent join) {
		join.getMember().edit(spec -> spec.setNickname("Anonyme")).block();
	}
	
	/*
	 * GETTER
	 */

	public UsersDB getUsersDB() {
		return this.users;
	}
	
	public IdDB getIdDB() {
		return this.ids;
	}

	public LiveFeed getLive() {
		return this.live;
	}

	public DiscordClient getClient() {
		return this.client;
	}
	
}
