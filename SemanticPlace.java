/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.*;

/**
 * This class stores a Semantic Place. A semantic place has a specific name and 
 * can contain multiple locationIDs 
 * 
 * @author Terminal 4
 */
public class SemanticPlace {    
    private ArrayList<Location> locationList = new ArrayList<>();
    private String semanticName;
    
    /**
     * Constructs a SemanticPlace object
     * 
     * @param semanticName Semantic Place's name
     * @param locationList an ArrayList of locations
     */
    public SemanticPlace(String semanticName, ArrayList<Location> locationList){
        this.semanticName = semanticName;
        this.locationList = locationList;
    }
    
    /** 
     * This method returns the Semantic Place's name
     * 
     * @return String name of semantic place
     */
    public String getName(){
        return semanticName;
    }
    
    /** 
     * Changes SemanticPlace's locationList to the ArrayList specified in locations
     * @param locations ArrayList of Location objects
     */
    public void updateArrayLocation(ArrayList<Location> locations){
        this.locationList = locations;
    }
    
    /**
     * Returns locationList, which is an ArrayList of Location objects that is stored within this SemanticPlace
     * @return an ArrayList of Location objects
     */
    public ArrayList<Location> getLocations(){
        return locationList;
    }
    
}
