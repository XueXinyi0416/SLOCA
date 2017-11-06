/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import dao.HeatMapDAO;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.*;
import net.minidev.json.*;
import java.io.OutputStream;
import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDateTime;
import util.Token;


@WebServlet(name = "HeatMapServlet", urlPatterns = {"/HeatMapServlet"})

/**
 * This Heatmap servlet contains the logic to support the Heatmap function of SLOCA
 * @author Terminal 4
 * 
 */
public class HeatMapServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setContentType("application/json");

        JSONObject json = new JSONObject();
        JSONArray errorArr = new JSONArray();
        JSONArray resultArr = new JSONArray();
        PrintWriter out = response.getWriter();
        String token = "";

        
        
        try {
            String sharedSecret = Token.getSharedSecret();
            String tsEnd = request.getParameter("date");
            String floor = request.getParameter("floor");
            token = request.getParameter("token");
            int floorNo = 3;
            String isDirectQuery = request.getParameter("hidden");
            boolean validPara = true;
            
            // If user is using JSON
            if (isDirectQuery == null) {
                // TOKEN VALIDATION START
                if (token == null) {
                    errorArr.add("missing token");
                    validPara = false;
                } else if (token.equals("")) {
                    errorArr.add("blank token");
                    validPara = false;
                } else {
                    try {
                        String username = JWTUtility.verify(token, sharedSecret);
                    } catch (JWTException e) {
                        errorArr.add("invalid token");
                        validPara = false;
                    }
                } // TOKEN VALIDATION END
                // DATE VALIDATION START
                if (tsEnd == null) {
                    validPara = false;
                    errorArr.add("missing date");
                } else if (tsEnd.equals("")) {
                    validPara = false;
                    errorArr.add("blank date");
                } else if (tsEnd != null) {
                    // Timestamp tweaking for JSON request
                    try {
                        tsEnd = tsEnd.replace("T", " ");
                        Timestamp tsCheck = Timestamp.valueOf(tsEnd);
                    } catch (IllegalArgumentException e) {
                        validPara = false;
                        errorArr.add("invalid date");
                    }
                } // DATE VALIDATION END
            } else {
                // Timestamp tweaking for Web App request
                // Handling NullPointerException
                if (tsEnd != null) {
                    tsEnd = tsEnd.replace("T", " ") + ":00";
                }
            }
            
            // FLOOR VALIDATION START
            if (floor == null) {
                validPara = false;
                errorArr.add("missing floor");
            } else if (floor.equals("")) {
                validPara = false;
                errorArr.add("blank floor");
            } else {
                String regex = "[0-5]{1}";
                // if floor contains numbers of only 0 to 5, and only 1 character
                if (floor.matches(regex)) {
                    floorNo = Integer.parseInt(floor);
                } else if (isDirectQuery != null && floor.equals("B1")) {
                    // this 'else if' statement is to check if user is accessing from web app
                    // and is selecting B1
                    floorNo = 0;
                } else {
                    validPara = false;
                    errorArr.add("invalid floor");
                }
            } // FLOOR VALIDATION END
            
            if (validPara == false) {
                json.put("status", "error");
                json.put("messages", errorArr);
            } else {
                // if floor does not contain "B" or "0", then insert "L" into floor
                if (floor.indexOf("B") == -1 && floor.indexOf("0") == -1) {
                    floor = "L" + floor;
                } else if(floor.indexOf("0") != -1){
                    floor = "B1";
                }

                java.sql.Timestamp startTimeStamp = java.sql.Timestamp.valueOf(tsEnd);
                startTimeStamp.setTime(startTimeStamp.getTime() - 15 * 60 * 1000);
                String tsStart = startTimeStamp.toString();

                HeatMapDAO hmDAO = new HeatMapDAO(tsStart, tsEnd, floor);
                LinkedHashMap<String, Integer> result = hmDAO.getResult();
                
                System.out.println(result);
                
                
                Set set = result.keySet();
                ArrayList<String> keyArray = new ArrayList<>(set);
                int i = 0;

                if (result.size() != 0) {
                    json.put("status", "success");
                    
                    for(String s : keyArray){    
                    JSONObject queryResults = new JSONObject();
                                        
                    int noVisits = (int)result.get(s);
                    
                    

                    
                    queryResults.put("semantic-place", s);
                    queryResults.put("num-people", noVisits);
                    if (noVisits== 0) {
                            queryResults.put("crowd-density", 0);
                        } else if (noVisits <= 2 && noVisits >= 1) {
                            queryResults.put("crowd-density", 1);
                        } else if (noVisits <= 5 && noVisits >= 3) {
                            queryResults.put("crowd-density", 2);
                        } else if (noVisits <= 10 && noVisits >= 6) {
                            queryResults.put("crowd-density", 3);
                        } else if (noVisits <= 20 && noVisits >= 11) {
                            queryResults.put("crowd-density", 4);
                        } else if (noVisits <= 30 && noVisits >= 21) {
                            queryResults.put("crowd-density", 5);
                        } else if (noVisits >= 31) {
                            queryResults.put("crowd-density", 6);
                        }
                    
                    resultArr.add(queryResults);
                }
                
                    
                    json.put("heatmap", resultArr);
                }
            }
        } catch (Exception e) {
            json.put("status", "error");
        } finally{
           out.write(json.toJSONString());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
