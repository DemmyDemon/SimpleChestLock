package com.webkonsept.bukkit.simplechestlock.listener;

import com.webkonsept.bukkit.simplechestlock.SCL;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;

public class SCLBlockListener implements Listener {
	final SCL plugin;

    private final HashSet<BlockFace> aura = new HashSet<BlockFace>(){{
        add(BlockFace.NORTH);
        add(BlockFace.SOUTH);
        add(BlockFace.WEST);
        add(BlockFace.EAST);
        add(BlockFace.UP);
        add(BlockFace.DOWN);
    }};
	
	public SCLBlockListener(SCL instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event){
		
		if (! plugin.isEnabled() ) return;
		if ( event.isCancelled() ) return;
		
		Block block = event.getBlock();
        if (!plugin.canLock(block)) return; // Thanks SonarBerserk

		if (plugin.chests.isLocked(block)){
			Player player = event.getPlayer();
			String owner = plugin.chests.getOwner(block);
			if (owner.equalsIgnoreCase(player.getName()) || SCL.permit(player,"simplechestlock.ignoreowner")){
                plugin.chests.unlock(block);
                if (!player.getName().equalsIgnoreCase(owner)){
                    Player ownerPlayer = plugin.getServer().getPlayer(owner);
                    if (ownerPlayer != null){
                        ownerPlayer.sendMessage(ChatColor.RED+player.getName()+" broke one of your locked blocks.");
                        player.sendMessage(ChatColor.RED+"Owner informed that you broke the locked block!");
                    }
                    else {
                        player.sendMessage(ChatColor.RED+"Locked block broken, but owner is offline and not informed.");
                    }

                }
                else {
                    player.sendMessage(ChatColor.GREEN+"Locked block broken!");
                }
			}
			else {
                plugin.messaging.throttledMessage(player,ChatColor.RED+"You can't break "+owner+"'s block!");
                event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event){
		if (!plugin.isEnabled()) return;
		if (event.isCancelled()) return;
		
		Block block = event.getBlock();
        if (!plugin.canLock(block) && !plugin.cfg.protectiveAura()) return; // Thanks SonarBerserk

        if (plugin.cfg.protectiveAura()){
            Player player = event.getPlayer();
            for (BlockFace face : aura){
                Block inAura = block.getRelative(face);
                if (plugin.chests.isLocked(inAura) && plugin.hasAura(inAura) && !plugin.chests.getOwner(inAura).equals(player.getName())){
                    player.sendMessage(ChatColor.GOLD+"You can't place that here:  Too close to a locked block."+ChatColor.RESET);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Now that we've established there are no locked blocks in the aura, it's no sense continuing if the block in question isn't even lockable.
        if (!plugin.canLock(block)) return;

        if (plugin.canDoubleLock(block)){
			Player player = event.getPlayer();
			HashSet<Block> checkForLocks = plugin.chests.getNeighbours(block);
			boolean hostileBlock = false;
			boolean foundLocked = false;
			for (Block checkBlock : checkForLocks){
				if (plugin.chests.isLocked(checkBlock) && checkBlock.getType().equals(block.getType())){
					if (plugin.chests.getOwner(checkBlock).equalsIgnoreCase(player.getName())){
						foundLocked = true;
					}
					else {
						hostileBlock = true;
					}
				}
			}
			String type = block.getType().toString().toLowerCase().replace("_"," ");
			if (hostileBlock){
				player.sendMessage(ChatColor.RED+"Sorry, someone else owns a locked "+type+" right next to this slot.  Can't place your "+type+" here, or there'd be a conflict.");
				event.setCancelled(true);
			}
			else if (foundLocked){
				plugin.chests.lock(player, block);
				player.sendMessage(ChatColor.YELLOW+"You have a locked "+type+" right next to this one, so I've locked the "+type+" you just placed.");
			}
		}
	}

}
