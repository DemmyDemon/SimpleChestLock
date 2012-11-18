package com.webkonsept.bukkit.simplechestlock;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;

public class Settings {
    SCL plugin;


    private boolean lockpair = true;
    private ItemStack key;
    private ItemStack comboKey;
    private boolean useKeyData = false;
    private boolean consumeKey = false;
    private boolean openMessage = true;
    private boolean usePermissionsWhitelist = false;
    private boolean whitelistMessage = true;
    private boolean lockedChestsSuck = false;
    private boolean protectiveAura = false;
    private int suckRange = 3;

    private boolean useLimits = false;
    private int suckInterval = 100;
    private boolean suckEffect = true;

    private boolean useWorldGuard = false;
    private WorldGuardPlugin worldGuard = null;

    private boolean preventExposions = true;

    public Settings (SCL instance){
        plugin = instance;
        load();
    }

    public boolean lockpair(){return lockpair;}
    public ItemStack key(){return key;}
    public ItemStack comboKey(){return comboKey;}
    // public boolean useKeyData(){return useKeyData;} // Never actually used outside this class
    public boolean consumeKey(){return consumeKey;}
    public boolean openMessage(){return openMessage;}
    public boolean usePermissionsWhitelist(){return usePermissionsWhitelist;}
    public boolean whitelistMessage(){return whitelistMessage;}
    public boolean lockedChestsSuck(){return lockedChestsSuck;}
    public int suckRange(){return suckRange;}
    public boolean useLimits(){return useLimits;}
    public int suckInterval(){return suckInterval;}
    public boolean suckEffect(){return suckEffect;}
    public boolean useWorldGuard(){return useWorldGuard;}
    public WorldGuardPlugin worldGuard(){return worldGuard;}
    public boolean preventExplosions(){return preventExposions;}
    public boolean protectiveAura(){return protectiveAura;}

    public void report(CommandSender target){
        target.sendMessage(ChatColor.GOLD+"SimpleChestLock settings");
        indentedMessages(target,
                "Verbose mode: " + boolColor(SCL.verbose),
                "Lock pairs: " + boolColor(lockpair),
                "Access message: " + boolColor(openMessage),
                "Limit number of locked blocks: " + boolColor(useLimits),
                "WorldGuard support enabled: " + boolColor(useWorldGuard),
                "Prevent explosions near locks: " + boolColor(preventExposions),
                "Some blocks have protective auras: " + boolColor(protectiveAura),
                "",
                "Key item: " + key.getType().toString() + ":" + key.getDurability(),
                "Combo key item; " + comboKey.getType().toString() + ":" + comboKey.getDurability(),
                "Check key data: " + boolColor(useKeyData),
                "Consume key: " + boolColor(consumeKey),
                "",
                "Permissions whitelist: " + boolColor(usePermissionsWhitelist),
                "Whitelist message: " + boolColor(whitelistMessage),
                "",
                "Locked containers suck up items: " + boolColor(lockedChestsSuck),
                "Item suck range: " + suckRange + " blocks",
                "Item suck interval: " + suckInterval + " ticks",
                "Click sound when sucking: " + boolColor(suckEffect)
        );
    }
    private void indentedMessages(CommandSender target, String... messages){
        for (String message : messages){
            target.sendMessage("    "+message);
        }
    }

    public String boolColor(boolean what){
        /** Returns a green ON if TRUE and RED OFF if FALSE. */
        return what ? ChatColor.GREEN+"ON" : ChatColor.RED+"OFF";
    }

    public void load() {
        File configFile = new File(plugin.getDataFolder(),"config.yml");
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        config.addDefaults(new HashMap<String,Object>(){
            {
                put("verbose",false);
                put("useLimits",false);
                put("key",280);
                put("keyDurability",0);
                put("comboKey",352);
                put("comboKeyDurability",0);
                put("useKeyDurability",false);
                put("consumeKey",false);

                put("useWorldGuard",false);
                put("preventExplosions",true);
                put("useProtectiveAuras",false);

                put("lockpair",true);
                put("usePermissionsWhitelist",false);
                put("whitelistMessage",true);
                put("openMessage",true);

                put("lockedChestsSuck",false);
                put("suckRange",3);
                put("suckInterval",100);
                put("suckEffect",true);

            }
        });

        SCL.verbose = config.getBoolean("verbose", false);
        lockpair = config.getBoolean("lockpair", true);
        usePermissionsWhitelist = config.getBoolean("usePermissionsWhitelist",false);
        whitelistMessage = config.getBoolean("whitelistMessage",true);
        openMessage = config.getBoolean("openMessage", true);

        useLimits = config.getBoolean("useLimits",false);

        Integer keyInt = config.getInt("key",280); // Stick
        Integer keyDurability = config.getInt("keyDurability",0);
        Integer comboKeyInt = config.getInt("comboKey",352); // Bone
        Integer comboKeyDurability = config.getInt("comboKeyDurability",0);
        useKeyData = config.getBoolean("useKeyDurability",false);

        consumeKey = config.getBoolean("consumeKey",false);

        useWorldGuard = config.getBoolean("useWorldGuard",false);
        if (useWorldGuard){
            Plugin candidate = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
            if (candidate == null || !(candidate instanceof WorldGuardPlugin)){
                useWorldGuard = false;
                SCL.crap("useWorldGuard is TRUE, but WorldGuard was not found!");
            }
            else {
                worldGuard = (WorldGuardPlugin) candidate;
                SCL.verbose("WorldGuard support enabled.");
            }
        }

        preventExposions = config.getBoolean("preventExplosions",true);
        protectiveAura = config.getBoolean("useProtectiveAuras",false);

        lockedChestsSuck = config.getBoolean("lockedChestsSuck",false);
        suckRange = config.getInt("suckRange",3);
        suckInterval = config.getInt("suckInterval",100);
        suckEffect = config.getBoolean("suckEffect",true);

        Material keyMaterial = Material.getMaterial(keyInt);
        Material comboKeyMaterial = Material.getMaterial(comboKeyInt);
        if (keyMaterial == null){
            keyMaterial = Material.STICK;
            useKeyData = false;
            SCL.crap("OY!  Material ID " + keyInt + " is not a real material.  Falling back to using STICK (ID 280) for the key.");
        }
        if (comboKeyMaterial == null){
            comboKeyMaterial = Material.BONE;
            useKeyData = false;
            SCL.crap("OY!  Materail ID " + comboKeyInt + " is not a real material. Falling back to using BONE (ID 352) for the combo key.");
        }

        key = new ItemStack(keyMaterial);
        key.setAmount(1);
        comboKey = new ItemStack(comboKeyMaterial);
        comboKey.setAmount(1);

        if (useKeyData){
            key.setDurability((short)(int)keyDurability);
            comboKey.setDurability((short)(int)comboKeyDurability);
        }

        if (!configFile.exists()){
            plugin.saveConfig();
        }
        if (plugin.trustHandler != null){
            plugin.trustHandler.loadFromConfig();
        }
        if (plugin.limitHandler != null){
            plugin.limitHandler.loadFromConfig();
        }
    }
}
