/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.SemanticPlace;
import java.sql.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;

/**
 * This Class handles the logic 
 * @author Terminal 4
 */
public class LocationDAO {

    private ArrayList<String> temp;
    private SemanticPlaceDAO spDAO = new SemanticPlaceDAO();

    public LocationDAO() {

    }

    /**
         *
         * The method getStartingTime returns the time that is of 15 minutes ago
         *
         * @param ts Timestamp variable to be used in the method
         * 
         * @return The time that is 15 minutes ago
         */
    public String getStartingTime(Timestamp ts) {
        String tsString = ts.toString();
        StringBuilder sb = new StringBuilder(tsString);
        sb.deleteCharAt(19);
        sb.deleteCharAt(19);
        tsString = sb.toString();

        Calendar cal = Calendar.getInstance();
        String startingTime = "";
        java.util.Date date;
        try {
            //converting string to timestamp
            java.sql.Timestamp ts_new = java.sql.Timestamp.valueOf(tsString);
            //setting the calendar time for calculation
            cal.setTime(ts_new);

            cal.add(Calendar.MINUTE, -15);
            startingTime = cal.toString();

            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startingTime = format1.format(cal.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return startingTime;

    }
    
    /**
         *
         * The method getLocationsVistsbyTime returns the number of visitors per semantic place
         *
         * @param ts Timestamp variable to be used in the method
         * 
         * @return The number of visitors per semantic place
         */
    public LinkedHashMap<String, Integer> getLocationsVisitsbyTime(Timestamp ts) {
        LinkedHashMap<String, Integer> data = new LinkedHashMap<>();

        String endTime = ts.toString();
        StringBuilder sb = new StringBuilder(endTime);
        sb.deleteCharAt(19);
        sb.deleteCharAt(19);
        endTime = sb.toString();

        String startTime = getStartingTime(ts);

        // go databsae
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            

            stmt = conn.prepareStatement("select `location-id` from location l inner join (select `mac-address` as mac, max(timestamp) as timeTs from location where timestamp \n"
                    + ">= ? and timestamp < ? group by `mac-address`) A where l.`mac-address` = A.mac and l.timestamp = A.timeTs order by `location-id`");
            stmt.setString(1, startTime);
            stmt.setString(2, endTime);

            rs = stmt.executeQuery();
            // rs can be null  
            if (rs.next() == false) {
                return null;
            } else {
                int location_id = rs.getInt("location-id");
                String semanticName = spDAO.getSpByLocationId(location_id);

                // if the location_id is invalid
                boolean exist = data.containsKey(semanticName);
                if (exist == true) {

                    int noVisits = data.get(semanticName);
                    noVisits += 1;
                    data.replace(semanticName, noVisits);
                } else {
                    data.put(semanticName, 1);
                }
            }

            // time, id, location
            while (rs.next()) {
                int location_id = rs.getInt("location-id");
                String semanticName = spDAO.getSpByLocationId(location_id);

                // if the location_id is invalid
                boolean exist = data.containsKey(semanticName);
                if (exist == true) {

                    int noVisits = data.get(semanticName);
                    noVisits += 1;
                    data.replace(semanticName, noVisits);
                } else {
                    data.put(semanticName, 1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
            return data;
        }

    }

    /**
         *
         * The method getNoPreviousSp returns the number of people in the selected 
         * semantic place for the previous window
         *
         * @param ts Timestamp variable to be used in the method
         * @param spName String variable to be used in the method
         * 
         * @return The number of poeple in the selected semantic place for the previous window
         */
    public int getNoPreviousSp(Timestamp ts, String spName) {
        int number = 0;

        ArrayList<String> temp = new ArrayList<>();

        String endTime = ts.toString();
        StringBuilder sb = new StringBuilder(endTime);
        sb.deleteCharAt(19);
        sb.deleteCharAt(19);
        endTime = sb.toString();

        String startTime = getStartingTime(ts);

        // go databsae
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();

            stmt = conn.prepareStatement("select `mac-address`,lu.`location-id`, `semantic-place` from location l \n"
                    + "inner join (select `mac-address` as mac, max(timestamp) as timeTs from location where timestamp \n"
                    + " >= ? and timestamp < ? group by `mac-address`) A inner join `location-lookup`lu \n"
                    + "on lu.`location-id` = l.`location-id` and l.`mac-address` = A.mac and l.timestamp = A.timeTs where `semantic-place` like ?");

            stmt.setString(1, startTime);
            stmt.setString(2, endTime);
            stmt.setString(3, "%" + spName + "%");

            rs = stmt.executeQuery();
            // rs can be null  

            if (rs.next() == false) {

                number = 0;
            } else {
                number = 1;
                String mac_address = rs.getString("mac-address");

                temp.add(mac_address);

                while (rs.next()) {
                    number += 1;

                    mac_address = rs.getString("mac-address");

                    temp.add(mac_address);
                }
            }

            this.temp = temp;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
            return number;
        }

    }

    /**
         *
         * The method getFutureTime returns the time 
         *
         * @param ts Timestamp variable to be used in the method
         * 
         * @return The time
         */
    public String getFutureTime(Timestamp ts) {
        String tsString = ts.toString();
        StringBuilder sb = new StringBuilder(tsString);
        sb.deleteCharAt(19);
        sb.deleteCharAt(19);
        tsString = sb.toString();

        Calendar cal = Calendar.getInstance();
        String futureTime = "";
        java.util.Date date;
        try {
            //converting string to timestamp
            java.sql.Timestamp ts_new = java.sql.Timestamp.valueOf(tsString);
            //setting the calendar time for calculation
            cal.setTime(ts_new);

            cal.add(Calendar.MINUTE, +15);
            futureTime = cal.toString();

            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            futureTime = format1.format(cal.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return futureTime;

    }

    /**
         *
         * The method getUsermap returns users' locations within the time
         *
         * @param ts Timestamp variable to be used in the method
         * @param spName String variable to be used in the method
         * 
         * @return The users' locations within the time
         */
    // ArrayList<ArrayList<Mac-address, timestamp, location-id>>
    public ArrayList<ArrayList<String>> getUserMap(Timestamp ts, String spName) {

        String endTime = ts.toString();
        StringBuilder sb = new StringBuilder(endTime);
        sb.deleteCharAt(19);
        sb.deleteCharAt(19);
        endTime = sb.toString();

        String futureTime = getFutureTime(ts);

        SemanticPlaceDAO spDAO = new SemanticPlaceDAO();
        LocationDAO locatDAO = new LocationDAO();

        // initialising the userMap to store user with their time <mac_address, <semanticName, tsString>>
        ArrayList<ArrayList<String>> userMap = new ArrayList<>();
        // go databsae
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("select * from location where timestamp >= ? and timestamp < ? order by `mac-address`, timestamp desc,`location-id` desc");
            stmt.setString(1,endTime);
            stmt.setString(2,futureTime);
            
            rs = stmt.executeQuery();

            //Store the timestamps for users
            while (rs.next()) {

                String mac_address = rs.getString("mac-address");

                //only create those in the previous window
                if (temp.indexOf(mac_address) != -1) {
                    int location_id = rs.getInt("location-id");
                    String sepName = spDAO.getSpByLocationId(location_id);
                    String tsString = rs.getString("timestamp");
                    ArrayList<String> tempList = new ArrayList<>();
                    tempList.add(mac_address);
                    tempList.add(sepName);
                    tempList.add(tsString);
                    userMap.add(tempList);

                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
            return userMap;
        }

    }
    
    /**
         *
         * The method nextPlaceWithNoVisits returns the semantic places with the number of visits
         *
         * @param ts Timestamp variable to be used in the method
         * @param spName String variable to be used in the method
         * 
         * @return The semantic places with the number of visits
         */
    // mac_address, ArrayList<spName, ts>
    public HashMap<String, ArrayList<ArrayList<String>>> nextPlaceWithNoVisits(Timestamp ts, String spName) {

        HashMap<String, ArrayList<ArrayList<String>>> result = new HashMap<>();

        ArrayList<ArrayList<String>> userMap = getUserMap(ts, spName);

        for (ArrayList<String> user : userMap) {
            String mac_address = user.get(0);
            String spname = user.get(1);
            String tsString = user.get(2);

            // check if the user alrdy in the result
            if (result.containsKey(mac_address)) {
                ArrayList<ArrayList<String>> spTsArray = result.get(mac_address);
                ArrayList<String> t = new ArrayList<>();
                t.add(spname);
                t.add(tsString);
                spTsArray.add(t);
                result.replace(mac_address, spTsArray);
            } else {

                ArrayList<ArrayList<String>> tempArray = new ArrayList<>();
                ArrayList<String> t1 = new ArrayList<>();
                t1.add(spname);
                t1.add(tsString);
                boolean add = tempArray.add(t1);
                result.put(mac_address, tempArray);
            }

        }

        return result;

    }
    
    /**
             *
             * The method getTopKNextPLace returns the semantic places with the number of visits
             *
             * @param ts Timestamp variable to be used in the method
             * @param spName String variable to be used in the method
             * 
             * @return The semantic places with the number of visits
             */
    public HashMap<String, Integer> getTopKNextPlace(Timestamp ts, String spName) {
        HashMap<String, Integer> result = new HashMap<>();

        //initialising the userMap <mac, ArrayList<spName, ts>>
        HashMap<String, ArrayList<ArrayList<String>>> userMap = nextPlaceWithNoVisits(ts, spName);

        //temper array to track the postition of the user <spName, ArrayList<ts>>
        HashMap<String, ArrayList<String>> trackLocation = new HashMap<>();

        for (int x = 0; x < temp.size(); x++) {
            String mac_address = temp.get(x);
            ArrayList<ArrayList<String>> locationArray = userMap.get(mac_address);
            
           

            if (locationArray != null) {
                //create a hashmap to store <spName, ArrayList<ts>>
                LinkedHashMap<String, ArrayList<String>> currentLocation = new LinkedHashMap<>();

                ArrayList<String> temparray = locationArray.get(0);
                String firstSpname = temparray.get(0);
                String firsttsString = temparray.get(1);
                ArrayList<String> firsttsArray = new ArrayList<>();
                String futureTime = getFutureTime(ts);
                firsttsArray.add(futureTime);
                firsttsArray.add(firsttsString);

                // add firstSpname into the hashmap
                currentLocation.put(firstSpname, firsttsArray);

                for (int j = 1; j < locationArray.size(); j++) {
                    ArrayList<String> array = locationArray.get(j);
                    String spname = array.get(0);
                    String tsString = array.get(1);

                    

                    if (spname.equals(firstSpname) && j != locationArray.size()-1) {

                        if (currentLocation.containsKey(spname)) {
                            ArrayList<String> tsStamps = currentLocation.get(spname);
                            tsStamps.add(tsString);
                            currentLocation.replace(spname, tsStamps);
                        }
                    } else {
                        
                        
                        ArrayList<String> tsArray = currentLocation.get(firstSpname);
                        String endTime = tsArray.get(0);
                        Timestamp checkFiveMinUpdate = Timestamp.valueOf(endTime);
                        Timestamp fiveMinBeforeEnd = Timestamp.valueOf(endTime);
                        
                        
                        String secLastTime = tsArray.get(1);
                        Timestamp secLastTimeTs = Timestamp.valueOf(secLastTime);
                        
                        // check the 5 min difference in Mac address record update
                        checkFiveMinUpdate.setTime(checkFiveMinUpdate.getTime()-5*60*1000);
                        if(secLastTimeTs.before(checkFiveMinUpdate)){
                            secLastTimeTs.setTime(secLastTimeTs.getTime() + 5 * 60 * 1000);
                            fiveMinBeforeEnd = secLastTimeTs;
                        }
                        
                        
                        String startTime = tsArray.get(tsArray.size() - 1);

                        fiveMinBeforeEnd.setTime(fiveMinBeforeEnd.getTime() - 5 * 60 * 1000);
                        Timestamp startTs = Timestamp.valueOf(startTime);

                        

                        if (startTs.before(fiveMinBeforeEnd)) {

                            if (result.containsKey(firstSpname)) {
                                int noVisits = result.get(firstSpname);
                                noVisits += 1;
                                result.replace(firstSpname, noVisits);
                                j = locationArray.size();
                                
                                
                            } else {
                                result.put(firstSpname, 1);
                                
                            }

                        }

                        // clear the currentLocation    
                        currentLocation.clear();

                        //create the arrayList for the tsStamps for the new location
                        ArrayList<String> tsStamps = new ArrayList<>();
                        tsStamps.add(startTime);
                        tsStamps.add(tsString);

                        currentLocation.put(spname, tsStamps);

                        firstSpname = spname;

                        
                    }
                    
                }
                
            }
            
        }
        
        return result;
    }

}
