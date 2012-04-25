package com.webkonsept.bukkit.simplechestlock.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import com.webkonsept.bukkit.simplechestlock.SCL;

public class SCLWorldListener implements Listener {
	final SCL plugin;
	
	public SCLWorldListener(SCL instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onWorldLoad(final WorldLoadEvent event){
		plugin.chests.retryDeferred(event.getWorld().getName());
	}
}
