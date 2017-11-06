/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Location;
import entity.SemanticPlace;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author XUE XINYI
 */
public class HeatMapDAO {
    
    LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
    

    public HeatMapDAO(String startTime, String endTime, String floor) {
        
        loadSemanticPlace(floor);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("SELECT `semantic-place`, count(DISTINCT lt.`mac-address`) as num FROM  \n"
                    + "(select max(timestamp) as latesttime, `mac-address` from location where\n"
                    + "timestamp>= ? and timeStamp< ? \n"
                    + "group by `mac-address`) as lt inner join location l on \n"
                    + "lt.latesttime=l.timestamp and lt.`mac-address`=l.`mac-address` inner join `location-lookup`\n"
                    + "lo on lo.`location-id`=l.`location-id` where `semantic-place` like ? \n"
                    + "group by `semantic-place`\n"
                    + "order by num desc");

            
            stmt.setString(1, startTime);
            stmt.setString(2, endTime);
            stmt.setString(3,"%" + floor + "%");
            
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                String semantic_place = rs.getString("semantic-place");
                int no = rs.getInt("num");
                
                result.replace(semantic_place,no);
                
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            
            ConnectionManager.close(conn,stmt,rs);
        }
    }
    
    /**
     *
     * The method loadSemanticPlace loads the Semantic place from the specific floor
     *
     * @param floor String variable to be used in the method
     */
    public void loadSemanticPlace(String floor){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select distinct `semantic-place` from `location-lookup` where `semantic-place` like ? order by `semantic-place`");
            stmt.setString(1,"%" + floor + "%");
          
            
            
            rs = stmt.executeQuery();
            
            while(rs.next()){
                String semantic_place = rs.getString("semantic-place");
                
                result.put(semantic_place, 0);
                
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            ConnectionManager.close(conn,stmt,rs);
        }
    }
    
    public LinkedHashMap<String, Integer> getResult(){
        return result;
    }

}
