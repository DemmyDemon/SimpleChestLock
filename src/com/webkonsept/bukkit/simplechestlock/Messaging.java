package com.webkonsept.bukkit.simplechestlock;

import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.entity.Player;

public class Messaging {
    SCL plugin;
    int mtd = 3000;
    Calendar cal = Calendar.getInstance();
    
    HashMap<String,Long> playerTime = new HashMap<String,Long>();
    
    public Messaging (SCL instance,Integer minimumTimeDifference) {
        plugin = instance;
        mtd = minimumTimeDifference;
    }
    
    public void throttledMessage(Player player,String message){
        String playerName = player.getName();
        if (playerTime.containsKey(playerName)){
            if (playerTime.get(playerName) + mtd < now() ){
                player.sendMessage(message);
                playerTime.put(playerName,now());
            }
            else {
                plugin.babble("Throttling suppressed message '"+message+"' for "+playerName);
            }
        }
        else {
            player.sendMessage(message);
            playerTime.put(playerName,now());
        }
    }
    
    public Long now() {
        return cal.getTimeInMillis();
    }

}
