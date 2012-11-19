package com.webkonsept.bukkit.simplechestlock;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class Messaging {
    int mtd = 3000;
    
    private final HashMap<String,Long> playerTime = new HashMap<String,Long>();
    
    public Messaging (Integer minimumTimeDifference) {
        mtd = minimumTimeDifference;
    }
    
    public void throttledMessage(Player player,String message){
        String playerName = player.getName();
        if (playerTime.containsKey(playerName)){
            if ( (playerTime.get(playerName) + mtd) < System.currentTimeMillis() ){
                player.sendMessage(message);
                playerTime.put(playerName,System.currentTimeMillis());
            }
            else {
                SCL.verbose("Suppressed message '" + message + "' for " + playerName);
            }
        }
        else {
            player.sendMessage(message);
            playerTime.put(playerName,System.currentTimeMillis());
        }
    }
}
