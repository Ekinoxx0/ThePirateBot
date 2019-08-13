package to.thepiratebay.users;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import discord4j.core.DiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import to.thepiratebay.ThePirateBay;
import to.thepiratebay.stc.StaticID;

@SuppressWarnings("unused")
public class UsersDB {

	public static final File saveFolder = new File("." + File.separator + "users" + File.separator);
	private Logger log = Loggers.getLogger("UsersDB");
	private ArrayList<PlatformUser> users = null;
	private final DiscordClient client;
	private final ThePirateBay bay;

	public UsersDB(DiscordClient client, ThePirateBay bay)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		this.client = client;
		this.bay = bay;
		if (!saveFolder.exists()) {
			saveFolder.mkdirs();
		}

		this.load();
		this.log.info("Finished loading " + this.users.size() + " users file into UsersDB..");
	}

	private void load() throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		this.users = new ArrayList<PlatformUser>();
		for (File userFile : saveFolder.listFiles()) {
			try {
				PlatformUser user = PlatformUser.loadFromFile(userFile);
				if (user != null) {
					users.add(user);
				} else {
					log.error("User " + userFile.getName() + " is null, failed to load.");
				}
			} catch (Throwable th) {
				th.printStackTrace();
				log.error("Failed to load user file : " + userFile.getName());
			}
		}
	}

	private void saveAll() {
		try {
			for (PlatformUser user : users) {
				user.save();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/*
	 * Others
	 */
	
	public Mono<PlatformUser> getFromMemberMono(Member member){
		return Mono.from(t -> this.getFromMember(member));
	}

	public PlatformUser getFromMember(Member member) {
		return this.getFromSnowflake(member.getId());
	}

	public PlatformUser getFromSnowflake(Snowflake sf) {
		return this.getFromID(sf.asLong());
	}

	private PlatformUser getFromID(long id) {
		for (PlatformUser puser : users) {
			if (puser.getId() == id) {
				return puser;
			}
		}

		throw new NullPointerException();
	}

	public boolean exist(Member member) {
		try {
			this.getFromMember(member);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public PlatformUser createUser(Member member) {
		String id = bay.getIdDB().notTakenRandom();
		Guild g = bay.getClient().getGuildById(StaticID.GUILD).block();

		Role r = g.createRole(spec -> {
			spec.setColor(Color.GRAY);
			spec.setName(member.getId().asString());
			spec.setPermissions(PermissionSet.of(Permission.VIEW_CHANNEL));
		}).block();
		
		member.addRole(r.getId());

		TextChannel tc = g.createTextChannel(spec -> {
			spec.setParentId(StaticID.PRIVATE_CAT);
			spec.setName(id);
			spec.setPermissionOverwrites(new HashSet<>(Arrays.asList(
					PermissionOverwrite.forRole(g.getEveryoneRole().block().getId(), PermissionSet.none(),
							PermissionSet.of(Permission.VIEW_CHANNEL)),
					PermissionOverwrite.forRole(r.getId(), PermissionSet.of(Permission.VIEW_CHANNEL),
							PermissionSet.none())
					)));
		}).block();
		
		PlatformUser user = new PlatformUser(member, r, tc);
		this.users.add(user);

		bay.getLive().newUser(member, id);
		bay.getIdDB().inputId(id, member);
		
		tc.createMessage(spec -> {
			spec.setEmbed(embed -> {
				embed.setTitle("**__Bienvenue sur le réseau ``" + id + "``__**");
				embed.setDescription("\n"
						+ "**Hey, vous venez d'intégrer le réseau ThePirateBay !**\n"
						+ "\n"
						+ "Votre identifiant unique est ``" + id + "``, "
						+ "partagez le avec les personnes de confiance afin qu'ils puisse communiquer avec vous ici.\n"
						+ "\n"
						+ "**Voici quelques commandes de base :**\n"
						+ "     !msg <ID> <MESSAGE>\n"
						+ "     !services\n"
						+ "     !report <MSG>\n"
						+ "     !help\n"
						+ "     !profil\n");
			});
		}).block();
		return user;
	}
	
	public void cleanupUser(long id) {
		PlatformUser user = this.getFromID(id);
		this.bay.getClient().getRoleById(StaticID.GUILD, user.getRoleId()).flatMap(Role::delete).block();
		this.bay.getClient().getChannelById(user.getChannelId()).flatMap(Channel::delete).block();
		user.getSaveFile().delete();
		this.users.remove(user);
		this.bay.getIdDB().removeId(this.bay.getIdDB().getIdFromUser(user));
		log.info("Cleaned up " + id + " without errors");
	}

}
