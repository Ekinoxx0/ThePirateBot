package to.thepiratebay.users;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import to.thepiratebay.ThePirateBay;

public class PlatformUser {
	
	public static PlatformUser loadFromFile(File file) throws JsonSyntaxException, JsonIOException, FileNotFoundException, IllegalStateException {
		if(file == null || !file.exists() || !file.canRead()) throw new IllegalStateException("Unable to load from " + file.getName());
		return ThePirateBay.GSON.fromJson(new FileReader(file), PlatformUser.class);
	}

	/*
	 * 
	 */
	
	private final long id;
	private final String username;
	private final long roleId;
	private final long channelId;
	private String rpName = null;

	public PlatformUser(Member member, Role r, TextChannel tc) throws IllegalStateException {
		this.id = member.getId().asLong();
		this.username = member.getUsername();
		this.roleId = r.getId().asLong();
		this.channelId = tc.getId().asLong();
		this.save();
	}
	
	public void save() {
		try {
			FileWriter writer = new FileWriter(getSaveFile());
			writer.write(ThePirateBay.GSON.toJson(this));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public File getSaveFile() {
		return new File(UsersDB.saveFolder, this.id + ".json");
	}

	public long getId() {
		return this.id;
	}

	public String getUsername() {
		return this.username;
	}
	
	public String getRPName() {
		return rpName == null ? "Inconnu" : rpName;
	}
	
	public Snowflake getRoleId() {
		return Snowflake.of(roleId);
	}
	
	public Snowflake getChannelId() {
		return Snowflake.of(channelId);
	}

}
