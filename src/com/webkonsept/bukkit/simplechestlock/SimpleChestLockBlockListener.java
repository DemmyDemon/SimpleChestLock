package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class SimpleChestLockBlockListener extends BlockListener {
	SimpleChestLock plugin;
	
	public SimpleChestLockBlockListener(SimpleChestLock instance) {
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		
		if (! plugin.isEnabled() ) return;
		if ( event.isCancelled() ) return;
		
		Block block = event.getBlock();
		if (plugin.chests.isLocked(block)){
			Player player = event.getPlayer();
			String owner = plugin.chests.getOwner(block);
			if (owner != player.getName() && ! plugin.permit(player,"chestlock.ignoreowner")){
				player.sendMessage(ChatColor.RED+"You can't break "+owner+"'s chest!");
				event.setCancelled(true);
			}
			else {
				if (player.getName() != owner){
					Player ownerPlayer = plugin.getServer().getPlayer(owner);
					if (ownerPlayer != null){
						ownerPlayer.sendMessage(player.getName()+" broke one of your locked chests.");
						player.sendMessage(ChatColor.RED+"Owner informed that you broke the locked chest!");
					}
					else {
						player.sendMessage(ChatColor.RED+"Locked chest broken, but owner is offline and not informed.");
					}
					
				}
				else {
					player.sendMessage(ChatColor.GREEN+"Locked chest broken!");
				}
			}
		}
	}

}
