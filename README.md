SimpleChestLock - Simplified chest locking
==========================================

Veeery simplified chest locking, sort of like [LockChest](http://forums.bukkit.org/threads/lockchest.16317/) and others, but very simplified.  
Simply slap a chest with a stick, and it's locked. Only you can access it.  
Slap it with a stick again, and it'll be open for everyone.  

**NOTE:** May only lock the chest actually slapped with the stick, depending on settings.  
This means that two people can share a double chest, but it also means that if you forget to lock one half you might as well not have bothered.  
Make sure the settings are configured to do what you want!

**VERY IMPORTANT:**
If you are upgrading from 0.1, be sure to actually delete the 0.1 JAR file, as it has a different name! ChestLock.jar became SimpleChestLock.jar due to plugin name change!

**Features:**

* Lock chests slapstick-style
* Lock furnaces and dispensers, too (honest)!
* Configure a different key if you don't like sticks
* Optional Permissions support
* Share a chest with someone by locking half each on a double-chest (optional)
* Protects locked chests against destruction
* Keep those pesky griefers out of your stuff!
* No pesky iConomy support, so don't even suggest it!

**Accepted commands**

*/sclreload* - Reloads the chests file and settings -- WILL DISCARD ANY CHEST LOCKS SINCE LAST LOAD, so /sclsave first if you don't want that! 
*/sclsave* - Save the chests file right now. 

**Permission nodes**

*simplechestlock.reload* - Access to the /clreload command 
*simplechestlock.save* - Access to the /clsave command 
*simplechestlock.ignoreowner* - Open and unlock other people's chests 
*simplechestlock.lock* - Permission to lock chests 

All but chestlock.lock are OP only when Permissions are unavailable.

**Configuration**

Upon first run, the plugin will create plugins/SimpleChestLock/settings.yml 
Default values:  
    verbose: false
    key: 280
    lockpair: true

Leave *verbose* off unless you want the plugin to tell you eeeeeeverything about itself. 
The "key" is the ID of the preferred key object. 280 is a stick, Minecraft Data Values has all the IDs if you want to change this. 
Remember to /sclsave before you /sclreload unless you want recently locked chests unlocked... 
If lockpair is true, the plugin will lock the chest next to the one you slap as well, enabling automagic double chest locking. 
Turn this off if you want to share a chest with someone, but keep in mind that the setting is server global (all worlds!)