package to.thepiratebay.stc;

import java.awt.Color;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.util.Logger;
import reactor.util.Loggers;

public class StaticFunction {
	
	private static final Logger log = Loggers.getLogger("TPB");
	
	public static void updateInformation(DiscordClient client) {
		TextChannel channel = client.getChannelById(StaticID.INFO).cast(TextChannel.class).block();
		if(channel.getLastMessageId().isPresent()) {
			channel.getMessagesBefore(channel.getLastMessageId().get())
				.doOnError(throwable -> {log.error("Unable to delete old messages from #informations");
				})
				.concatWithValues(channel.getLastMessage().block())
				.filter(msg -> msg != null)
				.subscribe(msg -> msg.delete().block());
		}
		
		Message msg = channel.createMessage(spec -> {
			spec.setEmbed(embed -> {
				embed.setColor(Color.RED);
				embed.setTitle("**__Informations importantes relatives à ThePirateBay__**");
				
				embed.setDescription("\n"
						+ ":desktop: **__Informations HRP__** :desktop:\n"
						+ "Ce discord a pour but de mettre en relation des individus du serveur **BlueVinity**, "
						+ "merci de respecter chacun des utilisateurs et d'utiliser cette plateforme avec du bon sens.\n"
						+ "**A noter que les règles du serveur BlueVinity s'applique également ici !**\n"
						+ "\n"
						+ "A travers ce serveur, nous garantissons l'anonymat et ne profiterons pas de nos accès"
						+ "d'administrateurs afin de perturber vos activitées RP.\n"
						+ "Il est donc impossible de mettre en lien l'identité discord à une discussion ayant place sur ce discord.\n"
						+ "\n"
						+ "Afin de préserver le bon fonctionnement de cet anonymat, vous ne pourrez pas savoir qui a pour charge de s'occuper de cette plateforme"
						+ "vous pouvez tout de même nous contacter à travers le bot en utilisant la commande __!report__\n"
						+ "\n"
						+ ":oncoming_police_car: **->** Possible infiltration / Aucune lecture des MP cryptés\n"
						+ "\n"
						+ ":desktop: **__Informations RP__** :desktop:\n"
						+ "Bienvenue sur le *DARKNET*\n"
						+ "Profitez ici des nombreux services illégals disponible à __Los Santos__ anonymement !\n"
						+ "\n"
						+ "Chacun des utilisateurs de la plateformes est mis en communication à travers nos services, "
						+ "ainsi vous n'avez plus à vous tracasser à propos de votre identité, numéro de téléphone, etc..\n\n"
						+ "Pour vous mettre en communication avec d'autres utilisateurs nous utilisons un identifiant unique"
						+ "de quatres chiffres/lettres **`ab45`** qui vous sert d'identité sur cette plateforme, "
						+ "échanger cette identifiant avec d'autres utilisateurs dans la ville ou par téléphone afin de"
						+ "profiter d'échange crypter et intracable !\n\n"
						+ "\n"
						+ "Profitez de nos services pour voir les offres disponible en ville, ou même proposer vos propres offres illégales\n"
						+ "Utilisez les notes et avis de chaque utilisateur pour savoir s'il est de confiance sur cette plateforme.\n"
						+ "\n"
						+ "**Disclaimer :**\n"
						+ "Cette plateforme virtuelle ne vous empêche pas de vous rencontrer de manière physique à Los Santos mais nous "
						+ "recommandons de ne jamais faire confiance aux individus que vous rencontrerez !\n"
						+ "\n"
						+ "```Markdown\n"
						+ "#Pour accèder à la plateforme, réagissez à ce message en cochant la case de dessous```\n");
			});
		}).block();
		
		msg.addReaction(ReactionEmoji.unicode("✅")).block();
	}
	
	public static void anonymeAllMembers(DiscordClient client) {
		client.getGuilds()
			.subscribe(guild -> guild.getMembers()
									.filter(member -> !member.isBot())
									.filter(member -> !guild.getOwner().block().equals(member))
									.filter(member -> !member.getRoles().any(role -> role.getId().equals(StaticID.STAFF_RANK) || role.getId().equals(StaticID.RESP_RANK)).block())
									.subscribe(member -> member.edit(spec -> spec.setNickname("Anonyme")).doOnError(th -> {
										log.error("Unable to rename " + member.getUsername());
									}).block()));
	}
	
}
