/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.*;
import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * This Class handles logic related to Semantic Place 
 * @author Terminal 4
 */
public class SemanticPlaceDAO implements Serializable{
    
    private ArrayList<SemanticPlace> spList = new ArrayList<>();
    private String semanticNames;
    private int spListSize;
    
    public SemanticPlaceDAO(){
        
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        
        
        
        try{
            conn = ConnectionManager.getConnection();
            
            stmt = conn.prepareStatement("select * from `location-lookup` order by `semantic-place`");
            rs = stmt.executeQuery();
            
            while(rs.next()){
                int location_id = rs.getInt("location-id");
                String semantic_place = rs.getString("semantic-place");
                int index = spList.indexOf(semantic_place);
                
                
                // if the semantic place is not found
                if(index == -1){
                    ArrayList<Location> locations = new ArrayList<>();
                    locations.add(new Location(location_id));
                    SemanticPlace sp = new SemanticPlace(semantic_place, locations);
                    spList.add(sp);
                } else {
                
                    // if the semantic place is alrdy created
                    SemanticPlace sp = spList.get(index);
                    ArrayList<Location> locations = sp.getLocations();
                    locations.add(new Location(location_id));
                }
                
            }
            
            
            
        } catch(SQLException ex){
            ex.printStackTrace();
        }finally{
            ConnectionManager.close(conn,stmt,rs);
            generateSemanticNames();
        }
        
        
    }
    
    /**
     *
     * The method getSemanticPlace returns the Semantic Place object
     *
     * @param spName String variable to be used in the method
     * @return The Semantic Place
     */
    public SemanticPlace getSemanticPlace(String spName){
        for(SemanticPlace sp : spList){
            
            spName.trim();
            
            if(sp.getName().toLowerCase().indexOf(spName.toLowerCase()) != -1){
                return sp;
            } else if(sp.getName().equalsIgnoreCase(spName)){
                return sp;
            }
        }
        return null;
    }
    
    /**
     *
     * The method getSpByLocationId returns the Semantic Place name using the ID
     *
     * @param id integer variable to be used in the method
     * @return The Semantic Place String value
     */
    public String getSpByLocationId(int id){
        for(SemanticPlace sp : spList){
            ArrayList<Location> locations = sp.getLocations();
            for(Location l : locations){
                if (l.getLocation_id() == id){
                    return sp.getName();
                }
            }
        }
        return null;
    }
    
    /**
     *
     * The method retrieveAll returns all the Semantic Place
     *
     * @return The Semantic Places
     */
    public ArrayList<SemanticPlace> retrieveAll(){
        return spList;
    }
    
    /**
     *
     * The method getSemanticName returns the Semantic Place name
     *
     * @return The Semantic Place String value
     */
    public String getSemanticName(){
        return semanticNames;
    }
    
 
    /**
     * This method prints out the names of the various Semantic Places within the database
     */
    public void generateSemanticNames(){
        
        ArrayList<String> result = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try{
            conn = ConnectionManager.getConnection();
            
            stmt = conn.prepareStatement("select distinct `semantic-place` from `location-lookup` order by `semantic-place`");
            rs = stmt.executeQuery();
            
            while(rs.next()){
                
                String semantic_place = rs.getString("semantic-place");
                
                result.add(semantic_place);
               
            }
            
            
        } catch(SQLException ex){
            ex.printStackTrace();
        }finally{
            ConnectionManager.close(conn,stmt,rs);
        }
        
        
        semanticNames = result.toString();
        
    }
    
    
}
