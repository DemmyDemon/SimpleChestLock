package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class SCLEntityListener extends EntityListener {
	SCL plugin;
	
	public SCLEntityListener(SCL instance) {
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
