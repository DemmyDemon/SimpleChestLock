package com.webkonsept.bukkit.simplechestlock.listener;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.webkonsept.bukkit.simplechestlock.SCL;

public class SCLEntityListener implements Listener {
	SCL plugin;
	
	public SCLEntityListener(SCL instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onEntityExplode (EntityExplodeEvent event){
	    if (!plugin.isEnabled()) return;
	    if (event.isCancelled()) return;
		for (Block block : event.blockList()){
			if (plugin.chests.isLocked(block)){
				event.setCancelled(true);
				break;
			}
		}
	}
}
