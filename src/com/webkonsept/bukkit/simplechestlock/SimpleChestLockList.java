package com.webkonsept.bukkit.simplechestlock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class SimpleChestLockList implements Runnable {
	SimpleChestLock plugin;
	HashMap<Location,String> list = new HashMap<Location,String>();
	
	public SimpleChestLockList(SimpleChestLock instance) {
		plugin = instance;
	}
	public void load (String filename) {
		File chestFile = new File (plugin.getDataFolder().toString()+"/"+filename);
		plugin.babble("Reading chests from "+plugin.getDataFolder().getName()+"/"+filename);
		list.clear();
		if (!chestFile.exists()){
			plugin.getDataFolder().mkdir();
			try {
				plugin.babble("Attempting to create "+chestFile.getName());
				chestFile.createNewFile();
				plugin.babble("Attempting to create"+plugin.getDataFolder().getName()+"/"+filename);
			} catch (IOException e) {
				e.printStackTrace();
				plugin.crap("FAILED TO CREATE CHESTFILE ("+filename+"): "+e.getMessage());
			}
		}
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(chestFile));
			String line = "";
			while (line != null){
				line = in.readLine();
				if (line != null){
					String[] elements = line.split(",", 5);
					String playerName = elements[0];
					World world = plugin.server.getWorld(elements[1]);
					Double X = null;
					Double Y = null;
					Double Z = null;
					
					try {
						X = Double.parseDouble(elements[2]);
						Y = Double.parseDouble(elements[3]);
						Z = Double.parseDouble(elements[4]);
					}
					catch(NumberFormatException e){
						e.printStackTrace();
						plugin.crap("I got an unparsable number from the chest file: "+e.getMessage());
					}
					
					if (world != null && X != null && Y != null && Z != null){
						Location location = new Location(world,X,Y,Z);
						if ( ! list.containsKey(location)){
							Material type = location.getBlock().getType();
							if(plugin.lockable.containsKey(type)){
								plugin.babble("Added location to protection list: Player("+playerName+") World("+world+") X("+X+") Y("+Y+") Z("+Z+")");
								list.put(location, playerName);
							}
							else {
								plugin.crap(type.toString()+" not a lockable block at World("+world+") X("+X+") Y("+Y+") Z("+Z+")! "+playerName+"'s block was moved, or severe configuration change?");
							}
						}
					}
					else {
						plugin.crap("Error in chestfile:  Player("+playerName+") World("+world+") X("+X+") Y("+Y+") Z("+Z+")");
					}
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
		}
	}
	public void save(String filename) {
		File chestFile = new File (plugin.getDataFolder().toString()+"/"+filename);
		if (!chestFile.exists()){
			plugin.out("Attempting to create "+chestFile.getName());
			plugin.getDataFolder().mkdir();
			plugin.out("Attempting to create "+plugin.getDataFolder().getName()+"/"+filename);
			try {
				chestFile.createNewFile();
			} catch (IOException e) {
				plugin.crap("FAILED TO CREATE CHESTFILE");
				e.printStackTrace();
			}
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));
			for (Location location : list.keySet()){
				String playerName = list.get(location);
				out.write(playerName+","+location.getWorld().getName()+","+location.getX()+","+location.getY()+","+location.getZ());
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
			return list.get(block.getLocation());
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
	public Integer lock(Player player,Block block){
		if (player == null || block == null || list == null) return 0;
		if (plugin.canLock(block)){
			Integer lockedChests = 0;
			if (plugin.lockpair && plugin.canDoubleLock(block)){
				lockedChests = this.addNeighboringChests(block,player.getName());
			}
			else {
				list.put(block.getLocation(), player.getName());
				lockedChests = 1;
			}
			return lockedChests;
		}
		else {
			return 0;
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
	private Integer addNeighboringChests (Block block,String ownerName) {
		Integer additionalChestsLocked = 0;
		for (Block currentNeighbour : this.getNeighbours(block)){
			if (currentNeighbour.getType().equals(block.getType())){
				list.put(currentNeighbour.getLocation(), ownerName);
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
