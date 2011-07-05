package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class SimpleChestLockEntityListener extends EntityListener {
	SimpleChestLock plugin;
	
	public SimpleChestLockEntityListener(SimpleChestLock instance) {
		plugin = instance;
	}
	public void onEntityExplode (EntityExplodeEvent event){
		for (Block block : event.blockList()){
			if (plugin.chests.isLocked(block)){
				event.setCancelled(true);
				break;
			}
		}
	}
}
