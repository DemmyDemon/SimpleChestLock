package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class SCLWorldListener implements Listener {
	SCL plugin;
	
	public SCLWorldListener(SCL instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onWorldLoad(final WorldLoadEvent event){
		plugin.chests.retryDeferred(event.getWorld().getName());
	}
}
