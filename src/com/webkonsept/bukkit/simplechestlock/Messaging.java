package com.webkonsept.bukkit.simplechestlock;

import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.entity.Player;

public class Messaging {
    int mtd = 3000;
    final Calendar cal = Calendar.getInstance();
    
    final HashMap<String,Long> playerTime = new HashMap<String,Long>();
    
    public Messaging (Integer minimumTimeDifference) {
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
                SCL.verbose("Throttling suppressed message '" + message + "' for " + playerName);
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
