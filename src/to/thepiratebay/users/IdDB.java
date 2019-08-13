package to.thepiratebay.users;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.util.Logger;
import reactor.util.Loggers;
import to.thepiratebay.ThePirateBay;
import to.thepiratebay.stc.StaticID;

public class IdDB {

	private static final File saveFile = new File("." + File.separator + "ids.json");
	private Logger log = Loggers.getLogger("IdDB");
	private HashMap<String, ArrayList<Long>> ids = null;
	private final DiscordClient client;
	
	public IdDB(DiscordClient client) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		this.client = client;
		if(!saveFile.exists()) {
			saveFile.getParentFile().mkdirs();
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.ids = new HashMap<String, ArrayList<Long>>();
			this.save();
		}
		
		this.load();
		this.log.info("Finished loading " + this.ids.size() + " ids from Json file into IdDB..");
	}

	private void load() throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		ids = ThePirateBay.GSON.fromJson(new FileReader(saveFile), new TypeToken<HashMap<String, ArrayList<Long>>>(){}.getType());
	}

	private void save() {
		try {
			FileWriter writer = new FileWriter(saveFile);
			writer.write(ThePirateBay.GSON.toJson(ids));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Others
	 */
	
	public String getIdFromUser(PlatformUser user) {
		for(Entry<String, ArrayList<Long>> set : this.ids.entrySet()) {
			for(Long l : set.getValue()) {
				if(l == user.getId()) {
					return set.getKey();
				}
			}
		}
		
		return null;
	}
	
	public void removeId(String id) {
		if(!isValid(id)) throw new IllegalArgumentException();
		this.ids.remove(id);
		this.save();
	}
	
	public boolean isValid(String id) {
		return id.matches("[A-Za-z0-9]{4}") && this.ids.containsKey(id) && this.ids.get(id) != null && this.ids.get(id).size() > 0;
	}
	
	public boolean isGroupId(String id) {
		return this.isValid(id) && this.ids.get(id).size() > 1;
	}
	
	public void inputId(String id, Member member) {
		if(id == null || !id.matches("[A-Za-z0-9]{4}")) throw new IllegalArgumentException("ID Invalid :" + id + "(" + id.matches("[A-Za-z0-9]{4}") + ")");
		if(member == null || member.isBot()) throw new IllegalArgumentException("Null Member");
		
		if(this.ids.containsKey(id)) {
			this.ids.get(id).add(member.getId().asLong());
		} else {
			this.ids.put(id, new ArrayList<Long>(Arrays.asList(member.getId().asLong())));
		}
		
		if(!this.isValid(id)) throw new IllegalStateException("!isValid(" + id + ");");
		this.save();
	}
	
	public List<Long> getMembersId(String id){
		if(!this.isValid(id)) return Collections.emptyList();
		return this.ids.get(id);
	}
	
	public Flux<Member> getMembers(String id){
		if(!this.isValid(id)) return Flux.empty();
		
		if(client == null) {
			System.out.println("null client");
		}
		
		if(StaticID.GUILD == null) {
			System.out.println("null StaticID.GUILD");
		}
		
		if(this.ids.get(id).size() > 0) {
			System.out.println("not empty");

			if(Snowflake.of(this.ids.get(id).get(0)) != null) {
				System.out.println("not null snowflake");
				if(client.getMemberById(StaticID.GUILD, Snowflake.of(this.ids.get(id).get(0))).block() == null) {
					System.out.println("null member");
				}
			}
		}
		
		
		return Flux.fromIterable(this.ids.get(id))
				.filter(k -> k != null)
				.flatMap(k -> client.getMemberById(StaticID.GUILD, Snowflake.of(k)));
	}
	
	public List<Member> getMembersList(String id){
		return this.getMembers(id).collectList().block();
	}
	
	public String notTakenRandom() {
		String random;
		while(!this.isValid(random = random())) {}
		return random;
	}

	private String random() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 4);
	}
	
}
