package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class SimpleChestLockPlayerListener extends PlayerListener {
	SimpleChestLock plugin;
	
	public SimpleChestLockPlayerListener(SimpleChestLock instance) {
		plugin = instance;
	}
	private String ucfirst(String string){
		return string.substring(0,1).toUpperCase() + string.substring(1);
	}
	public void onPlayerInteract (PlayerInteractEvent event){
		if (! plugin.isEnabled() ) return;
		if ( event.isCancelled() ) return;
		Block block = event.getClickedBlock();
		
		
		Material toolUsed = Material.AIR;
		if (event.getItem() != null){
			toolUsed = event.getItem().getType();
		}
		
		if (block == null) return;  // We don't care about non-block (air) interactions.
		if (plugin.canLock(block)){
			String typename = block.getType().toString().replaceAll("_", " ").toLowerCase();
			if(
					event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
					|| ( 
						event.getAction().equals(Action.LEFT_CLICK_BLOCK)
						&& plugin.leftLocked.contains(block.getType())
						&& !(toolUsed.equals(plugin.key))
					)
					|| event.getAction().equals(Action.PHYSICAL)
			){
				if (plugin.chests.isLocked(block)){
					Player player = event.getPlayer();
					String owner = plugin.chests.getOwner(block);
					plugin.babble(player.getName()+" wants to use "+owner+"'s block");
					boolean ignoreOwner = plugin.permit(player, "simplechestlock.ignoreowner");
					if (! owner.equalsIgnoreCase(player.getName()) && ! ignoreOwner){
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED+"Access denied to "+owner+"'s "+typename);
					}
					else if (! owner.equalsIgnoreCase(player.getName()) && ignoreOwner){
						if (plugin.openMessage){
							player.sendMessage(ChatColor.GREEN+"Access granted to "+owner+"'s "+typename);
						}
					}
					else {
						if (plugin.openMessage){
							player.sendMessage(ChatColor.GREEN+"Access granted to "+typename);
						}
					}
				}
			}
			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
				ItemStack tool = event.getItem();
				if (tool == null) return;
				if (tool.getType().equals(plugin.key)){
					event.setCancelled(true);
					Player player = event.getPlayer();
					if (plugin.permit(player,"simplechestlock.lock")){
						if (plugin.chests.isLocked(block)){
							String owner = plugin.chests.getOwner(block);
							if (owner.equalsIgnoreCase(player.getName())){
								Integer unlockedChests = plugin.chests.unlock(block);
								if (unlockedChests == 1){
									player.sendMessage(ChatColor.GREEN+ucfirst(typename)+" unlocked");
								}
								else if (unlockedChests > 1){
									player.sendMessage(ChatColor.GREEN+unlockedChests.toString()+" "+typename+"s unlocked");
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking your "+typename);
								}
							}
							else if (plugin.permit(player, "simplechestlock.ignoreowner")){
								Integer unlockedChests = plugin.chests.unlock(block);
								Player ownerObject = plugin.server.getPlayer(owner);
								if (unlockedChests == 1){
									if (ownerObject != null){
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+typename+", and taddle-taled on you for it.");
										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked your "+typename+" using mystic powers!");
									}
									else {
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+typename+", but that user is offline, and was not notified.");
									}
								}
								else if (unlockedChests > 1){
									if (ownerObject != null){
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+unlockedChests.toString()+" "+typename+"s, and taddle-taled on you for it.");
										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked "+unlockedChests.toString()+" of your "+typename+"s using mystic powers!");
									}
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking "+owner+"'s "+typename);
								}
								
							}
							else {
								player.sendMessage(ChatColor.RED+"Locked by "+owner+":  You can't use it!");
							}
							
						}
						else {
							Integer chestsLocked = plugin.chests.lock(player, block);
							if (chestsLocked == 1){
								player.sendMessage(ChatColor.GREEN+ucfirst(typename)+" locked!");
							}
							else if (chestsLocked > 1){
								player.sendMessage(ChatColor.GREEN+chestsLocked.toString()+" "+typename+"s locked!");
							}
							else{
								player.sendMessage(ChatColor.RED+"Error encountered while locking this "+typename);
							}
						}
					}
					else {
						player.sendMessage(ChatColor.RED+"You can't lock or unlock blocks!  Permission denied!");
					}
				}
			}
		}
	}
}
