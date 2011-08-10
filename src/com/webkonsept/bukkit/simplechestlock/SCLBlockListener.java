package com.webkonsept.bukkit.simplechestlock;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SCLBlockListener extends BlockListener {
	SCL plugin;
	
	public SCLBlockListener(SCL instance) {
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		
		if (! plugin.isEnabled() ) return;
		if ( event.isCancelled() ) return;
		
		Block block = event.getBlock();
		if (plugin.chests.isLocked(block)){
			Player player = event.getPlayer();
			String owner = plugin.chests.getOwner(block);
			if (owner != player.getName() && ! plugin.permit(player,"simplechestlock.ignoreowner")){
				player.sendMessage(ChatColor.RED+"You can't break "+owner+"'s block!");
				event.setCancelled(true);
			}
			else {
				plugin.chests.unlock(block);
				if (player.getName() != owner){
					Player ownerPlayer = plugin.getServer().getPlayer(owner);
					if (ownerPlayer != null){
						ownerPlayer.sendMessage(ChatColor.RED+player.getName()+" broke one of your locked blocks.");
						player.sendMessage(ChatColor.RED+"Owner informed that you broke the locked block!");
					}
					else {
						player.sendMessage(ChatColor.RED+"Locked block broken, but owner is offline and not informed.");
					}
					
				}
				else {
					player.sendMessage(ChatColor.GREEN+"Locked block broken!");
				}
			}
		}
	}
	public void onBlockPlace(BlockPlaceEvent event){
		if (!plugin.isEnabled()) return;
		if (event.isCancelled()) return;
		
		Block block = event.getBlock();
		
		if (plugin.lockable.containsKey(block.getType()) && plugin.lockable.get(block.getType())){
			Player player = event.getPlayer();
			HashSet<Block> checkForLocks = plugin.chests.getNeighbours(block);
			boolean hostileBlock = false;
			boolean foundLocked = false;
			for (Block checkBlock : checkForLocks){
				if (plugin.chests.isLocked(checkBlock) && checkBlock.getType().equals(block.getType())){
					if (plugin.chests.getOwner(checkBlock).equalsIgnoreCase(player.getName())){
						foundLocked = true;
					}
					else {
						hostileBlock = true;
					}
				}
			}
			String type = block.getType().toString().toLowerCase().replace("_"," ");
			if (hostileBlock){
				player.sendMessage(ChatColor.RED+"Sorry, someone else owns a locked "+type+" right next to this slot.  Can't place your "+type+" here, or there'd be a conflict.");
				event.setCancelled(true);
			}
			else if (foundLocked){
				plugin.chests.lock(player, block);
				player.sendMessage(ChatColor.YELLOW+"You have a locked "+type+" right next to this one, so I've locked the "+type+" you just placed.");
			}
		}
	}

}
