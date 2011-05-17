package com.webkonsept.bukkit.simplechestlock;

import java.util.Iterator;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class SimpleChestLockEntityListener extends EntityListener {
	SimpleChestLock plugin;
	
	public SimpleChestLockEntityListener(SimpleChestLock instance) {
		plugin = instance;
	}
	public void onEntityExplode (EntityExplodeEvent event){
		List<Block> blocks = event.blockList();
		Iterator<Block> iterator = blocks.iterator();
		while (iterator.hasNext()){
			Block block = iterator.next();
			if (plugin.chests.isLocked(block)){
				event.setCancelled(true);
				break;
			}
		}
	}
}
