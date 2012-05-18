package com.webkonsept.bukkit.simplechestlock.locks;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.webkonsept.bukkit.simplechestlock.SCL;

public class SCLItem {

	private Location location;
	private String deferredLocation;
	public String worldName;
	private boolean isLocationDeferred = true;
	private String owner;
	private final String split = ",";
	private boolean comboLocked = false;
	private DyeColor combo[] = {DyeColor.WHITE,DyeColor.WHITE,DyeColor.WHITE};
	private HashSet<String> trusted = new HashSet<String>();
	
	SCLItem (SCL plugin,String line) throws ParseException {
	    SCLLine lockdef = new SCLLine(line);
	    
		String playerName = lockdef.owner;
		
		Double X = null;
		Double Y = null;
		Double Z = null;
		World world = null;
		
		this.owner = playerName;
		this.worldName = lockdef.worldName;
		
		world = Bukkit.getServer().getWorld(lockdef.worldName);
		try {
			X = Double.parseDouble(lockdef.X);
			Y = Double.parseDouble(lockdef.Y);
			Z = Double.parseDouble(lockdef.Z);
		}
		catch(NumberFormatException e){
			throw new ParseException("I got an unparsable number from the chest file: "+e.getMessage(),0);
		}
		comboLocked = Boolean.valueOf(lockdef.comboLocked);
		DyeColor tumbler1 = DyeColor.valueOf(lockdef.combo[0]);
		DyeColor tumbler2 = DyeColor.valueOf(lockdef.combo[1]);
		DyeColor tumbler3 = DyeColor.valueOf(lockdef.combo[2]);
		
		trusted = lockdef.trusted;
		
		if (tumbler1 == null){
			throw new ParseException("First DyeColor, "+lockdef.combo[0]+", does not make sense.",0);
		}
		else if (tumbler2 == null){
			throw new ParseException("Second DyeColor, "+lockdef.combo[1]+", does not make sense.",0);
		}
		else if (tumbler3 == null){
			throw new ParseException("Third DyeColor, "+lockdef.combo[2]+", does not make sense.",0);
		}
		else {
			combo[0] = tumbler1;
			combo[1] = tumbler2;
			combo[2] = tumbler3;
		}
		
		if (world != null && X != null && Y != null && Z != null){
			Location location = new Location(world,X,Y,Z);
            Block block = location.getBlock();
			if(plugin.canLock(block)){
				SCL.verbose("Added location to protection list: Player(" + playerName + ") World(" + world + ") X(" + X + ") Y(" + Y + ") Z(" + Z + ")");
				isLocationDeferred = false;
				this.location = location;
			}
			else {
				throw new ParseException(block.getType().toString()+" not a lockable block at World("+world+") X("+X+") Y("+Y+") Z("+Z+")! "+playerName+"'s block was moved, or severe configuration change?",0);
			}
		}
		else if (world == null && X != null && Y != null && Z != null){
			isLocationDeferred = true;
			SCL.verbose("World '" + lockdef.worldName + "' isn't loaded yet.  Will defer loading to the world load event.");
			deferredLocation = lockdef.worldName+split+lockdef.X+split+lockdef.Y+split+lockdef.Z;
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
	public boolean trusts(Player player){
	    String playerName = player.getName();
	    if (playerName.equals(owner)){
	        return true;
	    }
	    else if (trusted != null && trusted.contains("*")){
	        return true;
	    }
	    else if (trusted != null && trusted.contains(playerName.toLowerCase())){
	        return true;
	    }
	    else {
            for (String trustedEntry : trusted){
                if (trustedEntry.toLowerCase().startsWith("g:")){
                    String groupName = trustedEntry.substring(2);
                    SCL.verbose("Trust group found: "+groupName);
                    if (SCL.permit(player,"group."+groupName)){
                        return true;
                    }
                }
            }
	        return false;
	    }
	}
	public void setTrusted(HashSet<String> trusts){
	    this.trusted = trusts;
	}
	public String trustedNames (){
	   StringBuilder sb = new StringBuilder();
       if (this.trusted != null){
           for (String trustedPlayer : this.trusted){
                sb.append(" ");
                sb.append(trustedPlayer);
           }
           return sb.toString();
       }
       else {
           return null;
       }
	}
	public String shift(ArrayList<String> list){
	    String str = list.get(0);
	    list.remove(0);
	    return str;
	    
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
				SCL.verbose("Nope, " + locationParts[0] + " is still not loaded.");
				isLocationDeferred = true;
				return false;
			}
			else {
				Location location = new Location(world,X,Y,Z);
				//Material type = location.getBlock().getType();
                Block block = location.getBlock();
				if(plugin.canDoubleLock(block)){
					SCL.verbose("Added location to protection list: Player(" + owner + ") World(" + world + ") X(" + X + ") Y(" + Y + ") Z(" + Z + ")");
					this.location = location;
					plugin.chests.list.put(location,this);
				}
				else {
					throw new ParseException(block.getType().toString()+" not a lockable block at World("+world+") X("+X+") Y("+Y+") Z("+Z+")! "+owner+"'s block was moved, or severe configuration change?",0);
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
		
		if (this.trusted != null){
		    for (String trustedPlayer : this.trusted){
		        sb.append(split);
		        sb.append(trustedPlayer);
		    }
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
	public void setComboLocked(boolean locked){
	    this.comboLocked = locked;
	}
	public DyeColor[] getCombo(){
		return combo;
	}
	public String getComboString(){
		return combo[0].toString()+", "+combo[1].toString()+", "+combo[2].toString();
	}
	public void setCombo(DyeColor[] comboArray){
		if(comboArray.length > combo.length){
            System.arraycopy(comboArray, 0, combo, 0, combo.length);
		}
		else if (comboArray.length < combo.length) {
            System.arraycopy(comboArray, 0, combo, 0, comboArray.length);
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
