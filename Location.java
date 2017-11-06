/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 * This class stores a location id
 * 
 * @author Terminal 4
 */
public class Location {
    private int location_id;
    
    /**
     * Constructs a Location object
     * @param location_id location_id stored within this object
     */
    public Location(int location_id){
        this.location_id = location_id;
    }
    
    /** 
     * Returns the location id stored within Location
     * @return int location_id
     */
    public int getLocation_id(){
        return location_id;
    }
    
}
