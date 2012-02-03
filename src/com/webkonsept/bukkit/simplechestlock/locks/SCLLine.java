package com.webkonsept.bukkit.simplechestlock.locks;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SCLLine {

    private final static String splitter = ",";
    
    // Raw form
    private ArrayList<String> items = new ArrayList<String>();

    // Digested form
    public String owner;
    public String worldName = null;
    public String X = null;
    public String Y = null;
    public String Z = null;
    public String comboLocked = null;
    public String combo[] = {"WHITE","WHITE","WHITE"};
    
    public String trustLocked = null;
    public HashSet<String> trusted = new HashSet<String>(); 

    public SCLLine(String line) throws ParseException,IndexOutOfBoundsException {
        items.addAll(Arrays.asList(line.split(splitter)));
        //items = (ArrayList<String>) Arrays.asList(line.split(splitter));
        if (items.size() < 9){
            throw new ParseException("Completely misshaped chestfile line: "+line,0);
        }
        owner = shift();
        worldName = shift();
        X = shift();
        Y = shift();
        Z = shift();
        comboLocked = shift();
        combo[0] = shift();
        combo[1] = shift();
        combo[2] = shift();
        
        if (items.size() > 0){
            for (String user : items){
                trusted.add(user);
            }
        }
        items.clear();
        
    }
    
    private String shift(){
            return items.remove(0);
    }
    
}
