package com.webkonsept.minecraft.boilerplate;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class KonseptPlugin extends JavaPlugin {
    private static String pluginName = "Unknown plugin";
    private static String pluginVersion = "0.0.0";
    private static boolean verbose = false;
    private static boolean checkUpdate = false;
    private static Logger log;

    @Override
    public void onLoad(){
        pluginName = getDescription().getFullName();
        pluginVersion = getDescription().getVersion();
        log = getLogger();
    }
    public boolean allow(Player player,String permission){
        boolean allow = player.hasPermission(permission);
        verboseYes(player.getName()+"->"+permission,allow);
        return allow;
    }
    public static void setVerbose(Boolean setting){
        verbose = setting;
    }
    public static void setCheckUpdate(Boolean setting){
        checkUpdate = setting;
    }
    public static void log(String message){
        log.info(String.format("[%s v%s] %s",pluginName,pluginVersion,message));
    }
    public static void verbose(String message){
        if (verbose){
            log.info(String.format("[%s v%s] [VERBOSE] %s",pluginName,pluginVersion,message));
        }
    }
    public static void verboseYes(String message, boolean isTrue){
        if (!verbose) return;
        String enabled = isTrue ? ": yes" : ": no";
        verbose(message+enabled);
    }
    public static void warning(String message){
        log.warning(String.format("[%s v%s] %s",pluginName,pluginVersion,message));
    }
    public static void checkUpdate(){
        if (checkUpdate){
            verbose("Checking for update...");
            log(KonseptUpdate.check(pluginName, pluginVersion));
        }
        else {
            verbose("Update checking is disabled.");
        }
    }
    public FileConfiguration refreshConfig(){
        if (! new File(getDataFolder(),"config.yml").exists()){
            saveDefaultConfig();
        }
        else {
            reloadConfig();
        }

        FileConfiguration config = getConfig();

        verbose = config.getBoolean("verbose",false);
        verbose("Verbosity activated.");

        checkUpdate = config.getBoolean("checkUpdate",true);
        checkUpdate();

        return config;
    }
}
