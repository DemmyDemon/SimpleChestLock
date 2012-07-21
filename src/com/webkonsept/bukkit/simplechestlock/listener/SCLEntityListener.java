package com.webkonsept.bukkit.simplechestlock.listener;

import com.webkonsept.bukkit.simplechestlock.locks.SCLItem;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.webkonsept.bukkit.simplechestlock.SCL;

public class SCLEntityListener implements Listener {
	final SCL plugin;
	
	public SCLEntityListener(SCL instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onEntityExplode (EntityExplodeEvent event){
	    if (!plugin.isEnabled()) return;
	    if (event.isCancelled()) return;
		for (Block block : event.blockList()){
			if (plugin.chests.isLocked(block)){
                if (plugin.cfg.preventExplosions()){
				    event.setCancelled(true);
                    SCL.verbose(plugin.chests.getOwner(block)+"'s "+block.getType().toString()+ " was saved from explosion.  Boom: "+String.valueOf(event.getEntity()).replace("^Craft","")); // Thanks SonarBerserk
                    break;
                }
			}
		}
	}
    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorEntityExplode (EntityExplodeEvent event){
        if (!plugin.isEnabled()) return;
        if (event.isCancelled()) return;
        for (Block block : event.blockList()){
            if (plugin.chests.isLocked(block)){
                SCL.verbose(plugin.chests.getOwner(block)+"'s "+block.getType().toString()+ " was destroyed by an exploding "+String.valueOf(event.getEntity()).replace("^Craft","")); // Thanks SonarBerserk
                plugin.chests.unlock(block);
            }
        }
    }
}
