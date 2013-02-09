package com.webkonsept.minecraft.boilerplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class KonseptUpdate {

    public static String check(String pluginName, String hasVersion) {
        String version = null;
        String releaseTime = null;
        URLConnection connection = null;
        try {
            connection = new URL("http://minecraft.webkonsept.com/plugins/version/" + pluginName + ".jar").openConnection();
            connection.setConnectTimeout(2000); // Milliseconds, so two second timeout.
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\n");
            version = scanner.next();
            releaseTime = scanner.next();
            if (version != null && releaseTime != null) {
                if (!hasVersion.equals(version)) {
                    return "Current release version of " + pluginName + " is " + version + ", released " + releaseTime + ".  It can be downloaded from http://minecraft.webkonsept.com/plugins/download/" + pluginName + ".jar";
                }
                else {
                    return "You have the current release version of " + pluginName;
                }
            }
            else {
                return "Unable to determine the current release version of " + pluginName;
            }
        }
        catch (NoSuchElementException e) {
            return pluginName + " is not supported for KonseptUpdate yet.  Unreleased plugin?";
        }
        catch (SocketTimeoutException e) {
            return "Unable to reach http://minecraft.webkonsept.com/plugins/ for version check.";
        }
        catch (MalformedURLException e) {
            return e.getMessage();
        }
        catch (IOException e) {
            return e.getMessage();
        }
    }
}
