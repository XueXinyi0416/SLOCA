/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import entity.*;
import dao.*;
import is203.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import util.Token;

/**
 *
 * @author Terminal 4 This servlet contains the logic to support the function of
 * the Top K Next Place function of SLOCA
 *
 */
@WebServlet(name = "topKPlaceServlet", urlPatterns = {"/topKPlaceServlet"})
public class topKPlaceServlet extends HttpServlet {

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
        // TOKEN: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb290IiwiZXhwIjoxNTA4NDAwODQyLCJpYXQiOjE1MDgzOTcyNDJ9.3lau2JPpemSPpC3ZKvRMyyyzvsPg2zL46xM5UmtehoA

        // Creating variables for JSON
        JSONObject json = new JSONObject();
        JSONArray errorArr = new JSONArray();
        JSONArray resultArr = new JSONArray();
        int k = 3;
        Timestamp ts = null;
        boolean validPara = true;

        PrintWriter out = response.getWriter();
        try {
            String tsString = request.getParameter("date");
            tsString = tsString.replace("T", " ");
            if(tsString.length() < 19){
                tsString = tsString + ":00";
            }
            // tsString format : yyyy-mm-ddThh:mm => yyyy-mm-dd hh:mm:ss
            String isDirectQuery = request.getParameter("hidden");
            String kString = request.getParameter("k");
            String token = request.getParameter("token");
            String sharedSecret = Token.getSharedSecret();

            // If user is using JSON
            
            try {
                if (kString != null) {
                    if (kString.equals("")) {
                        validPara = false;
                        errorArr.add("blank k");
                    } else {
                        k = Integer.parseInt(kString);
                        // k must be a value between 1 and 10
                        if (k < 1 || k > 10) {
                            validPara = false;
                            errorArr.add("invalid k");
                        }
                    }
                } else {
                    k=3;
                }
            } catch (NumberFormatException e) {
                validPara = false;
                errorArr.add("invalid k");
            }
            
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
                        e.printStackTrace();
                        errorArr.add("invalid token");
                        validPara = false;
                    }
                }
                // TOKEN VALIDATION END
                

                // DATE VALIDATION START
                if (tsString == null) {
                    validPara = false;
                    errorArr.add("missing date");
                } else if (tsString.equals("")) {
                    validPara = false;
                    errorArr.add("blank date");
                } else if (tsString != null) {
                    // Timestamp tweaking for JSON request
                    try {
                        
                        Timestamp tsCheck = Timestamp.valueOf(tsString);
                    } catch (IllegalArgumentException e) {
                        validPara = false;
                        errorArr.add("invalid date");
                    }
                } // DATE VALIDATION END
            } 
            
            
                System.out.println( "hiii");
            
            // If errorArr has things in it, that means json request has errors
            if (validPara == false) {
                json.put("messages", errorArr);
                json.put("status","fail");
            } else {

                LocationDAO locatDAO = new LocationDAO();
                SemanticPlaceDAO sDAO = new SemanticPlaceDAO();
                
                System.out.println("HI" + tsString);
                
                ts = Timestamp.valueOf(tsString);

                HashMap<String, Integer> map = locatDAO.getLocationsVisitsbyTime(ts);

                // creating a List object of map. These following codes are to sort map based on values
                List list1 = new LinkedList(map.entrySet());
                // Defined Custom Comparator here
                Collections.sort(list1, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return ((Comparable) ((Map.Entry) (o2)).getValue())
                                .compareTo(((Map.Entry) (o1)).getValue());
                    }
                });

                // Here I am copying the sorted list in HashMap
                // using LinkedHashMap to preserve the insertion order
                LinkedHashMap sortedHashMap = new LinkedHashMap();
                for (Iterator it = list1.iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    sortedHashMap.put(entry.getKey(), entry.getValue());
                }
                // test code end

                int toComapreNoVisits = 0;
                int index = 0;
                if (sortedHashMap.size() != 0) {
                    json.put("status", "success");

                    Set set1 = sortedHashMap.keySet();
                    ArrayList<String> semanticArray = new ArrayList<>(set1);

                    for (int i = 1; i <= k; i++) {
                        JSONObject queryResults = new JSONObject();

                        String semanticString = semanticArray.get(index);

                        int noVisits = (int) sortedHashMap.get(semanticString);
                        // Removing '[' and ']' at start and end of semantic
                        semanticString = semanticString.replace("[", "");
                        semanticString = semanticString.replace("]", "");

                        if (noVisits == toComapreNoVisits) {
                            queryResults.put("rank", --i);
                        } else {
                            queryResults.put("rank", i);
                        }

                        toComapreNoVisits = noVisits;

                        queryResults.put("semantic-place", semanticString);
                        queryResults.put("count", noVisits);
                        resultArr.add(queryResults);
                        index++;
                    }

                    json.put("results", resultArr);

                    //out.print("</table>");
                } else {
                    json.put("results", resultArr);
                    json.put("status", "success");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            json.put("status", "error");
            json.put("messages", errorArr);
            //out.write(json.toString());
        } finally {

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
