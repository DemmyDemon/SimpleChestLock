package com.webkonsept.bukkit.simplechestlock.locks;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.webkonsept.bukkit.simplechestlock.SCL;

public class LimitHandler {
    private SCL plugin;
    private HashMap<String,Integer> limits = new HashMap<String,Integer>();
    
    public LimitHandler(SCL instance){
        plugin = instance;
        loadFromConfig();
    }
    /* Useless
    public void updateConfig(){
        for (String groupname : limits.keySet()){
            plugin.getConfig().set("lockLimits."+groupname,limits.get(groupname));
        }
        plugin.saveConfig();
    }
    */
    
    public void loadFromConfig(){
        plugin.verbose("Loading lockLimits from config");
        ConfigurationSection lockLimits = plugin.getConfig().getConfigurationSection("lockLimits");
        if (lockLimits != null){
            Map<String,Object> cfgGroups = lockLimits.getValues(false);
            if (cfgGroups != null){
                plugin.verbose(cfgGroups.size() + " lockLimits groups found.");
                for (String groupName : cfgGroups.keySet()){
                    Object rawValue = cfgGroups.get(groupName);
                    if (rawValue instanceof Integer){
                        Integer limit = (Integer)rawValue;
                        limits.put(groupName,limit);
                        plugin.verbose(groupName + "-> " + limit);
                    }
                    else {
                        plugin.crap("lockLimits contains an unusable "+rawValue.getClass()+" ("+rawValue+")");
                    }
                }
            }
            else {
                plugin.verbose("Could not use lockLimits secion!  Invalid YAML?");
            }
        }
        else {
            plugin.verbose("lockLimits section does not exist.  Lock limits won't work!");
        }
    }
    
    public int getLimit(Player player){
        Integer foundLimit = 0;
        plugin.verbose("Checking lock limits for " + player.getName());
        for (String groupname : limits.keySet()){
            if (plugin.permit(player,"simplechestlock.locklimit."+groupname)){
                foundLimit += limits.get(groupname);
                plugin.verbose(player.getName() + " is in locklimit group " + groupname + ", limited to " + foundLimit);
            }
        }
        if (foundLimit <= 0){
            plugin.verbose("NOTE:  User " + player.getName() + " can't lock stuff because he/she has a zero-or-less limit!");
        }
        else {
            plugin.verbose(player.getName() + " is limited at " + foundLimit + " locks.");
        }
        return foundLimit;
    }
    public int getLocksLeft(Player player){
        int locksUsed = getLocksUsed(player);
        int limit = getLimit(player);
        plugin.verbose(player.getName() + " has used " + locksUsed + "/" + limit + " locks.");
        return limit - locksUsed;
    }
    public int getLocksUsed(Player player){
        int locksUsed = 0;
        String playerName = player.getName();
        for (SCLItem item : plugin.chests.list.values()){
            if (item.getOwner().equals(playerName)){
                locksUsed++;
            }
        }
        return locksUsed;
    }
    public String usedString(Player player){
        int locksUsed = getLocksUsed(player);
        int limit = getLimit(player);
        if (plugin.useLimits){
            return locksUsed+"/"+limit+" locks used";
        }
        else {
            return "";
        }
    }
    public boolean canLock(Player player, Integer wantToLock){
        if (!plugin.useLimits){
            plugin.verbose("Locking authorized:  Limits disabled");
            return true;
        }
        else if (plugin.permit(player,"simplechestlock.nolocklimit")){
            plugin.verbose("Locking authorized:  User unlimited!");
            return true;
        }
        else if (wantToLock <= getLocksLeft(player)){
            plugin.verbose("Locking authorized:  Player has enough free locks");
            return true;
        }
        else {
            plugin.verbose("Locking NOT authorized:  Not enough free locks!");
            return false;
        }
    }
}
