package com.webkonsept.bukkit.simplechestlock;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class SimpleChestLock extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private PermissionHandler Permissions;
	private boolean usePermissions;
	public boolean verbose = false;
	public boolean lockpair = true;
	public Material key = Material.STICK;
	public Server server = null;
	
	// Intended to hold the material in question and a boolean of weather or not it's double-lockable (like a double chest)
	public HashMap<Material,Boolean> lockable = new HashMap<Material,Boolean>();
	
	private SimpleChestLockPlayerListener 	playerListener 	= new SimpleChestLockPlayerListener(this);
	private SimpleChestLockBlockListener 	blockListener 	= new SimpleChestLockBlockListener(this);
	private SimpleChestLockEntityListener 	entityListener 	= new SimpleChestLockEntityListener(this);
	public  SimpleChestLockList				chests			= new SimpleChestLockList(this);
	
	private HashMap<String,Boolean> fallbackPermissions = new HashMap<String,Boolean>();

	@Override
	public void onDisable() {
		chests.save("Chests.txt");
		this.out("Disabled!");
	}

	@Override
	public void onEnable() {
		setupLockables();
		loadConfig();
		if(!setupPermissions()){
			fallbackPermissions.put("simplechestlock.reload",false);
			fallbackPermissions.put("simplechestlock.save",false);
			fallbackPermissions.put("simplechestlock.ignoreowner",false);
			fallbackPermissions.put("simplechestlock.lock", true);
		}
		server = getServer();
		chests.load("Chests.txt");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK,blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE,entityListener,Priority.Normal,this);
		this.out("Enabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		
		if ( ! this.isEnabled() ) return false;
		
		boolean success = true;
		boolean player = false;
		if (sender instanceof Player){
			player = true;
		}

		if (command.getName().equalsIgnoreCase("sclreload")){
			if ( !player  || this.permit((Player)sender, "simplechestlock.reload")){
				this.loadConfig();
				chests.load("Chests.txt");
				sender.sendMessage("Successfully reloaded configuration and chest list");
			}
			else {
				sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
				success = false;
			}
		}
		else if (command.getName().equalsIgnoreCase("sclsave")){
			if (!player || this.permit((Player)sender, "simplechestlock.save")){
				chests.save("Chests.txt");
				sender.sendMessage("Successfully saved the chests file");
			}
		}
		return success;
	}
	public boolean permit(Player player,String permission){ 
		
		boolean allow = false; // Default to GTFO
		if ( usePermissions ){
			allow = Permissions.has(player,permission);
		}
		else if (player.isOp()){
			allow = true;
		}
		else {
			if (fallbackPermissions.get(permission) || false){
				allow = true;
			}
		}
		this.babble(player.getName()+" asked permission to "+permission+": "+allow);
		return allow;
	}
	private boolean setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		if (this.Permissions == null){
			if (test != null){
				this.Permissions = ((Permissions)test).getHandler();
				this.usePermissions = true;
				return true;
			}
			else {
				this.out("Permissions plugin not found, defaulting to OPS CHECK mode");
				return false;
			}
		}
		else {
			this.out("Urr, this is odd...  Permissions are already set up!");
			return true;
		}
	}
	public void out(String message) {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void crap(String message){
		PluginDescriptionFile pdfFile = this.getDescription();
		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void babble(String message){
		if (!this.verbose){ return; }
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
	}
	public String plural(int number) {
		if (number == 1){
			return "";
		}
		else {
			return "s";
		}
	}
	private void setupLockables() {
		lockable.clear();
		lockable.put(Material.CHEST,true);
		//NOTE:  If double locking is enabled for furnaces, remember that a furnace and a burning furnace are NOT the same material!
		// That means that double locking, which tests the neighboring blocks for .equals() on the material, won't work if one is burning. 
		lockable.put(Material.FURNACE,false);
		lockable.put(Material.BURNING_FURNACE,false);

		lockable.put(Material.DISPENSER,false);
	}
	public boolean canLock (Block block){
		if (block == null) return false;
		Material material = block.getType();
		return lockable.containsKey(material);
	}
	public boolean canDoubleLock (Block block){
		if (block == null) return false;
		Material material = block.getType();
		if (lockable.containsKey(material)){
			return lockable.get(material);
		}
		else {
			return false;
		}
	}
	public void loadConfig() {
		File configFile = new File(this.getDataFolder().toString()+"/settings.yml");
		File configDir = this.getDataFolder();
		Configuration config = new Configuration(configFile);
		
		config.load();
		verbose = config.getBoolean("verbose", false);
		Integer keyInt = config.getInt("key",280); // Stick
		lockpair = config.getBoolean("lockpair", true);
		
		key = Material.getMaterial(keyInt);
		if (key == null){
			key = Material.STICK;
			this.crap("OY!  Material ID "+keyInt+" is not a real material.  Falling back to STICK (ID 280)");
		}
		if (!configFile.exists()){
			if (!configDir.exists()){
				configDir.mkdir();
			}
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				this.crap("IOError while creating config file: "+e.getMessage());
			}
			config.save();
		}
	}

}
