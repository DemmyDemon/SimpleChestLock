package com.webkonsept.bukkit.simplechestlock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class SCLList implements Runnable {
	SCL plugin;
	HashMap<Location,SCLItem> list = new HashMap<Location,SCLItem>();
	int key = 0;
	
	public SCLList(SCL instance) {
		plugin = instance;
	}
	public void load (String filename) {
		File chestFile = new File (plugin.getDataFolder(),filename);
		plugin.babble("Reading chests from "+chestFile.getAbsolutePath());
		list.clear();
		if (!chestFile.exists()){
			plugin.getDataFolder().mkdir();
			try {
				plugin.babble("Attempting to create"+chestFile.getAbsolutePath());
				chestFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				plugin.crap("FAILED TO CREATE CHESTFILE ("+filename+"): "+e.getMessage());
			}
		}
		
		int lineNumber = 0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(chestFile));
			String line = "";
			
			while (line != null){
				line = in.readLine();
				lineNumber++;
				if (line != null && !line.matches("^\\s*#")){
					SCLItem item = new SCLItem(plugin,line);
					list.put(item.getLocation(),item);
				}
				else {
					plugin.babble("Done reading protected locations!");
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			plugin.crap("OMG JAVA!  The file I just created DOES NOT EXIST?!  Damn.");
		} catch (IOException e) {
			e.printStackTrace();
			plugin.crap("Okay, crap, IOException while reading "+filename+": "+e.getMessage());
		} catch (ParseException e) {
			plugin.crap("Failed to parse line "+lineNumber+" from chest file: "+e.getMessage());
		}
	}
	public void save(String filename) {
		File chestFile = new File (plugin.getDataFolder(),filename);
		if (!chestFile.exists()){
			plugin.getDataFolder().mkdir();
			plugin.out("Attempting to create "+chestFile.getAbsolutePath());
			try {
				chestFile.createNewFile();
			} catch (IOException e) {
				plugin.crap("FAILED TO CREATE CHESTFILE");
				e.printStackTrace();
			}
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));
			for (SCLItem item : list.values()){
				String line = item.toString();
				plugin.babble("Saved: "+line);
				out.write(line);
				out.newLine();
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			plugin.crap("OMG JAVA!  The file I just created DOES NOT EXIST?!  Damn.");
		} catch (IOException e) {
			e.printStackTrace();
			plugin.crap("Okay, crap, IOExeption while writing "+filename+": "+e.getMessage());
		}
	}
	public String getOwner(Block block){
		if (block == null) return null;
		if (list.containsKey(block.getLocation())){
			return list.get(block.getLocation()).getOwner();
		}
		else {
			return null;
		}
	}
	public boolean isLocked(Block block){
		if (block == null) return false;
		if (list == null) return false;
		if (list.containsKey(block.getLocation())){
			return true;
		}
		else {
			return false;
		}
	}
	public boolean isComboLocked(Block block){
		if (isLocked(block)){
			plugin.babble("> Locked");
			 
			SCLItem item = list.get(block.getLocation());
			if (item != null){
				if (item.isComboLocked()){
					plugin.babble("> Combo");
					return true;
				}
				else {
					plugin.babble("> Not a combo");
					return false;
				}
			}
			else {
				plugin.babble("> Locked item is null?!");
				return false;
			}
		}
		else {
			plugin.babble("> Not locked!");
			return false;
		}
	}
	public Integer lock(Player player,Block block){
		if (player == null || block == null || list == null) return 0;
		if (plugin.canLock(block)){
			int lockedChests = 0;
			if (plugin.lockpair && plugin.canDoubleLock(block)){
				lockedChests = this.addNeighboringChests(block,player);
			}
			else {
				list.put(block.getLocation(),new SCLItem(player,block));
				lockedChests = 1;
			}
			return lockedChests;
		}
		else {
			return 0;
		}
	}
	public Integer lock(Player player,Block block,DyeColor[] combo){
		if (player == null || block == null || list == null || combo.length != 3) return 0;
		if (plugin.canLock(block)){
			int lockedItems = 0;
			if (plugin.lockpair && plugin.canDoubleLock(block)){
				lockedItems = this.addNeighboringChests(block, player,combo);
			}
			else {
				list.put(block.getLocation(),new SCLItem(player,block,combo));
				lockedItems = 1;
			}
			return lockedItems;
		}
		else {
			return 0;
		}
	}
	public String getComboString(Block block) {
		if (list.containsKey(block.getLocation())){
			SCLItem item = list.get(block.getLocation());
			return item.getComboString();
		}
		else {
			return " ..NOT LOCKED.. ";
		}
	}
	public Integer unlock(Block block){
		if (block == null) return 0;
		if (this.isLocked(block)){
			if (list != null){
				
				Integer unlockedChests = 0;
				if (plugin.lockpair && plugin.canDoubleLock(block)){
					unlockedChests = this.removeNeighboringChests(block);
				}
				else {
					list.remove(block.getLocation());
					unlockedChests = 1;
				}
				return unlockedChests;
			}
			else {
				return 0;
			}
		}
		return 0;
	}
	protected HashSet<Block> getNeighbours (Block block) {
		HashSet<Block> neighbours = new HashSet<Block>();
		neighbours.add(block);
		neighbours.add(block.getRelative(BlockFace.NORTH));
		neighbours.add(block.getRelative(BlockFace.SOUTH));
		neighbours.add(block.getRelative(BlockFace.EAST));
		neighbours.add(block.getRelative(BlockFace.WEST));
		// For doors
		neighbours.add(block.getRelative(BlockFace.UP));
		neighbours.add(block.getRelative(BlockFace.DOWN));
		
		
		HashSet<Block> additionalNeighbours = new HashSet<Block>();
		for (Block neighbour : neighbours){
			additionalNeighbours.add(neighbour.getRelative(BlockFace.UP));
			additionalNeighbours.add(neighbour.getRelative(BlockFace.DOWN));
		}
		neighbours.addAll(additionalNeighbours);
		
		
		return neighbours;
	}
	private Integer removeNeighboringChests (Block block) {
		String playerName = getOwner(block);
		Integer additionalChestsUnlocked = 0;
		for (Block currentNeighbour : this.getNeighbours(block)){
			if (currentNeighbour.getType().equals(block.getType())){
				String owner = this.getOwner(currentNeighbour);
				if (owner != null && owner.equals(playerName)){
					list.remove(currentNeighbour.getLocation());
					additionalChestsUnlocked++;
				}
			}
		}
		return additionalChestsUnlocked;
	}
	private Integer addNeighboringChests (Block block,Player owner,DyeColor[] combo) {
		Integer additionalChestsLocked = 0;
		for (Block currentNeighbour : this.getNeighbours(block)){
			if (currentNeighbour.getType().equals(block.getType())){
				list.put(currentNeighbour.getLocation(), new SCLItem(owner,block,combo));
				additionalChestsLocked++;
			}
		}
		return additionalChestsLocked;
	}
	private Integer addNeighboringChests (Block block,Player owner) {
		Integer additionalChestsLocked = 0;
		for (Block currentNeighbour : this.getNeighbours(block)){
			if (currentNeighbour.getType().equals(block.getType())){
				list.put(currentNeighbour.getLocation(), new SCLItem(owner,block));
				additionalChestsLocked++;
			}
		}
		return additionalChestsLocked;
	}
	@Override
	public void run() { // So saving to the default filename is easily scheduled
		this.save("Chests.txt");
	}
}
