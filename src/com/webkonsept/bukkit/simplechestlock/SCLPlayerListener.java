package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SCLPlayerListener implements Listener {
	SCL plugin;
	
	public SCLPlayerListener(SCL instance) {
		plugin = instance;
	}
	private String ucfirst(String string){
		return string.substring(0,1).toUpperCase() + string.substring(1);
	}
	
	// TODO: Break up this monster method, plx!
	@EventHandler
	public void onPlayerInteract (final PlayerInteractEvent event){
		if (! plugin.isEnabled() ) return;
		if ( event.isCancelled() ) return;
		Block block = event.getClickedBlock();
		
		
		Material toolUsed = Material.AIR;
		if (event.getItem() != null){
			toolUsed = event.getItem().getType();
		}
		Player player = event.getPlayer();
		
		if (block == null) return;  // We don't care about non-block (air) interactions.
		if (plugin.canLock(block)){
			String typeName = block.getType().toString().replaceAll("_", " ").toLowerCase();
			if(
					event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
					|| ( 
						event.getAction().equals(Action.LEFT_CLICK_BLOCK)
						&& plugin.leftLocked.contains(block.getType())
						&& !(toolUsed.equals(plugin.key) || toolUsed.equals(plugin.comboKey))
					)
					|| event.getAction().equals(Action.PHYSICAL)
			){
				if (plugin.chests.isLocked(block)){
					String owner = plugin.chests.getOwner(block);
					plugin.babble(player.getName()+" wants to use "+owner+"'s "+typeName);
					boolean ignoreOwner = plugin.permit(player, "simplechestlock.ignoreowner");
					boolean comboLocked = plugin.chests.isComboLocked(block);
					if (comboLocked){
						plugin.babble("This block is locked with a combination lock!");
					}
					else {
						plugin.babble("This block is locked with a normal key");
					}
					
					if ( comboLocked && ! owner.equalsIgnoreCase(player.getName()) && ! ignoreOwner){
						Inventory inv = player.getInventory();
						if (
								inv.getItem(0).getType().equals(Material.WOOL)
								&& inv.getItem(1).getType().equals(Material.WOOL)
								&& inv.getItem(2).getType().equals(Material.WOOL)
						){
							DyeColor tumbler1 = DyeColor.getByData(inv.getItem(0).getData().getData());
							DyeColor tumbler2 = DyeColor.getByData(inv.getItem(1).getData().getData());
							DyeColor tumbler3 = DyeColor.getByData(inv.getItem(2).getData().getData());
							DyeColor[] combo = {tumbler1,tumbler2,tumbler3};
							SCLItem item = plugin.chests.list.get(block.getLocation());
							if (!item.correctCombo(combo)){
								plugin.babble(player.getName()+" provided the wrong combo for "+owner+"'s "+typeName);
								player.sendMessage(ChatColor.RED+owner+"'s "+typeName+" has a different combination...");
								event.setCancelled(true);
							}
						}
						else {
							plugin.babble(player.getName()+" provided no combo for "+owner+"'s "+typeName);
							player.sendMessage(ChatColor.RED+owner+"'s "+typeName+" is locked with a combination lock.");
							event.setCancelled(true);
						}
					}
					else if (! owner.equalsIgnoreCase(player.getName()) && ! ignoreOwner){
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED+"Access denied to "+owner+"'s "+typeName);
					}

					else if (! owner.equalsIgnoreCase(player.getName()) && ignoreOwner){
						plugin.babble(player.getName()+" was let into "+owner+"'s "+typeName+", ignoring owner.");
						if (plugin.openMessage){
							player.sendMessage(ChatColor.GREEN+"Access granted to "+owner+"'s "+typeName);
						}
					}
					else {
						plugin.babble(player.getName()+" was let into the "+typeName);
						if (plugin.openMessage){
							if (comboLocked){
								String comboString = plugin.chests.getComboString(block);
								//player.sendMessage(ChatColor.GREEN+"Access granted to "+typeName);
								player.sendMessage(ChatColor.GREEN+"Lock combination is "+comboString);
							}
							else {
								player.sendMessage(ChatColor.GREEN+"Access granted to "+typeName);
							}
						}
					}
				}
				else if (plugin.permit(player, "simplechestlock.limited")){
				    plugin.babble("Player "+player.getName()+" is limited, and access to un-owned "+typeName+" was denied.");
				    event.setCancelled(true);
				    player.sendMessage(ChatColor.RED+"You can only open thigs that belong to you.");
				}
				else {
				    plugin.babble("Access granted to unlocked "+typeName);
				}
			}
			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
				ItemStack tool = event.getItem();
				if (tool == null) return;
				if (tool.getType().equals(plugin.key) || tool.getType().equals(plugin.comboKey)){
					event.setCancelled(true);
					if (plugin.permit(player,"simplechestlock.lock")){
						if (plugin.chests.isLocked(block)){
							String owner = plugin.chests.getOwner(block);
							if (owner.equalsIgnoreCase(player.getName())){
								Integer unlockedChests = plugin.chests.unlock(block);
								if (unlockedChests == 1){
									player.sendMessage(ChatColor.GREEN+ucfirst(typeName)+" unlocked");
								}
								else if (unlockedChests > 1){
									player.sendMessage(ChatColor.GREEN+unlockedChests.toString()+" "+typeName+"s unlocked");
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking your "+typeName);
								}
							}
							else if (plugin.permit(player, "simplechestlock.ignoreowner")){
								Integer unlockedChests = plugin.chests.unlock(block);
								Player ownerObject = plugin.server.getPlayer(owner);
								if (unlockedChests == 1){
									if (ownerObject != null){
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+typeName+", and taddle-taled on you for it.");
										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked your "+typeName+" using mystic powers!");
									}
									else {
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+typeName+", but that user is offline, and was not notified.");
									}
								}
								else if (unlockedChests > 1){
									if (ownerObject != null){
										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+unlockedChests.toString()+" "+typeName+"s, and taddle-taled on you for it.");
										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked "+unlockedChests.toString()+" of your "+typeName+"s using mystic powers!");
									}
								}
								else {
									player.sendMessage(ChatColor.RED+"Error while unlocking "+owner+"'s "+typeName);
								}
								
							}
							else {
								player.sendMessage(ChatColor.RED+"Locked by "+owner+":  You can't use it!");
							}
							
						}
						else {
							if (
								!(plugin.usePermissionsWhitelist)
								|| ( 
										plugin.usePermissionsWhitelist 
										&& plugin.permit(player, new String[]{"simplechestlock.locktype."+block.getType().toString().toLowerCase(),"simplechestlock.locktype.*"})
									)
							){
								boolean lockForSomeone = false;
								String locksFor = "???";
								if (plugin.locksAs.containsKey(player.getName())){
									locksFor = plugin.locksAs.get(player.getName());
									lockForSomeone = true;
								}
								if (tool.getType().equals(plugin.comboKey)){
									if (plugin.permit(player, "simplechestlock.usecombo")){
										Inventory inv = player.getInventory();
										if (
												inv.getItem(0).getType().equals(Material.WOOL)
												&& inv.getItem(1).getType().equals(Material.WOOL)
												&& inv.getItem(2).getType().equals(Material.WOOL)
										){
											DyeColor tumbler1 = DyeColor.getByData(inv.getItem(0).getData().getData());
											DyeColor tumbler2 = DyeColor.getByData(inv.getItem(1).getData().getData());
											DyeColor tumbler3 = DyeColor.getByData(inv.getItem(2).getData().getData());
											DyeColor[] combo = {tumbler1,tumbler2,tumbler3};
											String comboString = tumbler1.toString()+","+tumbler2.toString()+","+tumbler3.toString();
											Integer itemsLocked = plugin.chests.lock(player,block,combo);
											if (itemsLocked == 1){
												if (lockForSomeone){
													player.sendMessage(ChatColor.GREEN+ucfirst(typeName)+" locked for "+locksFor+"!  Combo is "+comboString);
												}
												else {
													player.sendMessage(ChatColor.GREEN+ucfirst(typeName)+" locked!  Combo is "+comboString);
												}
											}
											else if (itemsLocked > 1){
												if (lockForSomeone){
													player.sendMessage(ChatColor.GREEN+itemsLocked.toString()+" "+typeName+"s locked for "+locksFor+"!  Combo is "+comboString);
												}
												else {
													player.sendMessage(ChatColor.GREEN+itemsLocked.toString()+" "+typeName+"s locked!  Combo is "+comboString);
												}
											}
											else{
												player.sendMessage(ChatColor.RED+"Error encountered while locking this "+typeName);
											}
											
										}
										else {
											player.sendMessage(ChatColor.RED+"First three hotbar slots must be wool for the combo!");
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"Sorry, permission denied for combination locking!");
									}
								}
								else {
									Integer chestsLocked = plugin.chests.lock(player, block);
									if (chestsLocked == 1){
										if (lockForSomeone){
											player.sendMessage(ChatColor.GREEN+ucfirst(typeName)+" locked for "+locksFor+"!");
										}
										else {
											player.sendMessage(ChatColor.GREEN+ucfirst(typeName)+" locked!");
										}
									}
									else if (chestsLocked > 1){
										if (lockForSomeone){
											player.sendMessage(ChatColor.GREEN+chestsLocked.toString()+" "+typeName+"s locked for "+locksFor+"!");
										}
										else {
											player.sendMessage(ChatColor.GREEN+chestsLocked.toString()+" "+typeName+"s locked!");
										}
									}
									else{
										player.sendMessage(ChatColor.RED+"Error encountered while locking this "+typeName);
									}
								}
							}
							else if (plugin.usePermissionsWhitelist){
								player.sendMessage(ChatColor.RED+"Sorry, you are not allowed to lock "+block.getType().toString());
							}
						}
					}
					else {
						player.sendMessage(ChatColor.RED+"You can't lock or unlock blocks!  Permission denied!");
					}
				}
			}
		}
	}
}
