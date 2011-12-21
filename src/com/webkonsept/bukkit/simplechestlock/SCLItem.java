package com.webkonsept.bukkit.simplechestlock;

import java.text.ParseException;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SCLItem {

	private Location location;
	private String deferredLocation;
	public String worldName;
	private boolean isLocationDeferred = true;
	private String owner;
	private String split = ",";
	private boolean comboLocked = false;
	private DyeColor combo[] = {DyeColor.WHITE,DyeColor.WHITE,DyeColor.WHITE};
	
	private int correctLength = 9;
	
	SCLItem (SCL plugin,String line) throws ParseException {
		String[] elements = line.split(split,9);
		String playerName = "???";
		try {
			playerName = elements[0];
		}
		catch(ArrayIndexOutOfBoundsException e){
			throw new ParseException("Completely misshaped chestfile line: "+line,0);
		}
		
		Double X = null;
		Double Y = null;
		Double Z = null;
		World world = null;
		if (elements.length == correctLength){ // That is, if it's the new format with the combo;
			this.owner = playerName;
			this.worldName = elements[1];
			
			world = Bukkit.getServer().getWorld(elements[1]);
			try {
				X = Double.parseDouble(elements[2]);
				Y = Double.parseDouble(elements[3]);
				Z = Double.parseDouble(elements[4]);
			}
			catch(NumberFormatException e){
				throw new ParseException("I got an unparsable number from the chest file: "+e.getMessage(),0);
			}
			comboLocked = Boolean.valueOf(elements[5]);
			DyeColor tumbler1 = DyeColor.valueOf(elements[6]);
			DyeColor tumbler2 = DyeColor.valueOf(elements[7]);
			DyeColor tumbler3 = DyeColor.valueOf(elements[8]);
			if (tumbler1 == null){
				throw new ParseException("First DyeColor, "+elements[6]+", does not make sense.",0);
			}
			else if (tumbler2 == null){
				throw new ParseException("Second DyeColor, "+elements[7]+", does not make sense.",0);
			}
			else if (tumbler3 == null){
				throw new ParseException("Third DyeColor, "+elements[8]+", does not make sense.",0);
			}
			else {
				combo[0] = tumbler1;
				combo[1] = tumbler2;
				combo[2] = tumbler3;
			}
		}
		else {
			throw new ParseException("Invalid number of fields in Chestfile line: "+elements.length+" elements in "+line, 0);
		}
		
		if (world != null && X != null && Y != null && Z != null){
			Location location = new Location(world,X,Y,Z);
			Material type = location.getBlock().getType();
			if(plugin.lockable.containsKey(type)){
				plugin.babble("Added location to protection list: Player("+playerName+") World("+world+") X("+X+") Y("+Y+") Z("+Z+")");
				isLocationDeferred = false;
				this.location = location;
				
			}
			else {
				throw new ParseException(type.toString()+" not a lockable block at World("+world+") X("+X+") Y("+Y+") Z("+Z+")! "+playerName+"'s block was moved, or severe configuration change?",0);
			}
		}
		else if (world == null && X != null && Y != null && Z != null){
			isLocationDeferred = true;
			plugin.babble("World '"+elements[1]+"' isn't loaded yet.  Will defer loading to later.");
			deferredLocation = elements[1]+split+elements[2]+split+elements[3]+split+elements[4];
		}
		else {
			throw new ParseException("Unknown error in chestfile:  Player("+playerName+") World("+world+") X("+X+") Y("+Y+") Z("+Z+")",0);
		}
	}
	SCLItem (Player player, Block block){
		isLocationDeferred = false;
		location = block.getLocation();
		owner = player.getName();
	}
	SCLItem (String playerName, Block block){
		isLocationDeferred = false;
		location = block.getLocation();
		owner = playerName;
	}
	SCLItem (String playerName,Block block,DyeColor[] comboArray){
		isLocationDeferred = false;
		location = block.getLocation();
		owner = playerName;
		if (comboArray.length == combo.length){
			combo = comboArray;
			comboLocked = true;
		}
	}
	SCLItem (Player player, Block block, DyeColor[] comboArray){
		isLocationDeferred = false;
		location = block.getLocation();
		owner = player.getName();
		if (comboArray.length == combo.length){
			combo = comboArray;
			comboLocked = true;
		}
	}
	public boolean retryLocation(SCL plugin) throws ParseException {
		if (!isLocationDeferred){
			return true;
		}
		else {
			String[] locationParts = deferredLocation.split(",",4);
			World world = Bukkit.getServer().getWorld(locationParts[0]);
			Double X = null;
			Double Y = null;
			Double Z = null;
			try {
				X = Double.parseDouble(locationParts[1]);
				Y = Double.parseDouble(locationParts[2]);
				Z = Double.parseDouble(locationParts[3]);
			}
			catch(NumberFormatException e){
				throw new ParseException("I got an unparsable number from the chest file: "+e.getMessage(),0);
			}
			
			if (world == null){
				plugin.babble("Nope, "+locationParts[0]+" is still not loaded.");
				isLocationDeferred = true;
				return false;
			}
			else {
				Location location = new Location(world,X,Y,Z);
				Material type = location.getBlock().getType();
				if(plugin.lockable.containsKey(type)){
					plugin.babble("Added location to protection list: Player("+owner+") World("+world+") X("+X+") Y("+Y+") Z("+Z+")");
					this.location = location;
					plugin.chests.list.put(location,this);
				}
				else {
					throw new ParseException(type.toString()+" not a lockable block at World("+world+") X("+X+") Y("+Y+") Z("+Z+")! "+owner+"'s block was moved, or severe configuration change?",0);
				}
				deferredLocation = null;
				isLocationDeferred = false;
				return true;
			}
		}
		
	}
	public boolean isLocationDeferred() {
		return isLocationDeferred;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(owner);
		sb.append(split);
		if (isLocationDeferred){
			sb.append(deferredLocation);
		}
		else {
			sb.append(location.getWorld().getName());
			sb.append(split);
			sb.append(location.getBlockX());
			sb.append(split);
			sb.append(location.getBlockY());
			sb.append(split);
			sb.append(location.getBlockZ());
		}
		sb.append(split);
		sb.append(comboLocked);
		for (DyeColor tumbler : combo){
			sb.append(split);
			sb.append(tumbler.toString());
		}
		
		return sb.toString();
	}
	public String getOwner(){
		return owner;
	}
	public void setOwner(Player player){
		owner = player.getName();
	}
	public void setOwner(String playerName){
		owner = playerName;
	}
	public boolean isComboLocked(){
		return comboLocked;
	}
	public DyeColor[] getCombo(){
		return combo;
	}
	public String getComboString(){
		return combo[0].toString()+", "+combo[1].toString()+", "+combo[2].toString();
	}
	public void setCombo(DyeColor[] comboArray){
		if(comboArray.length > combo.length){
			for (int i = 0; i < combo.length; i++){
				combo[i] = comboArray[i];
			}
		}
		else if (comboArray.length < combo.length) {
			for (int i = 0; i < comboArray.length; i++){
				combo[i] = comboArray[i];
			}
		}
		else {
			combo = comboArray;
		}
	}
	public boolean correctCombo(DyeColor[] comboArray){
		boolean correct = true;
		
		if (comboLocked){
			if (comboArray.length != combo.length){
				correct = false;
			}
			else {
				for (int i = 0; i < combo.length; i++){
					if (combo[i] != comboArray[i]){
						correct = false;
					}
				}
			}
		}
		
		return correct;
		
	}
	public Location getLocation(){
		return location;
	}
}
