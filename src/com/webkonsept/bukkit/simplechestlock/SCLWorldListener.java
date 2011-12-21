package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

public class SCLWorldListener extends WorldListener {
	SCL plugin;
	
	public SCLWorldListener(SCL instance) {
		plugin = instance;
	}
	
	public void onWorldLoad(WorldLoadEvent event){
		plugin.chests.retryDeferred(event.getWorld().getName());
	}
}
