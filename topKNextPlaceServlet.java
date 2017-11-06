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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@WebServlet(name = "topKNextPlaceServlet", urlPatterns = {"/topKNextPlaceServlet"})
public class topKNextPlaceServlet extends HttpServlet {

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

        try {
            /* TODO output your page here. You may use following sample code. */

            LocationDAO locatDAO = new LocationDAO();
            SemanticPlaceDAO sDAO = new SemanticPlaceDAO();

            //get all the parameters
            String tsString = request.getParameter("date");
            String kString = request.getParameter("k");
            String spName = request.getParameter("origin");

            String sharedSecret = Token.getSharedSecret();
            String token = request.getParameter("token");

            String isDirectQuery = request.getParameter("hidden");
            int k = 3;
            boolean validPara = true;
            java.sql.Timestamp ts = null;

            // JSON Validation BEGIN
            // Checking for missing parameters
            // VALIDATION FOR K
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
                    k =3;
                }
            } catch (NumberFormatException e) {
                validPara = false;
                errorArr.add("invalid k");
            }

            // VALIDATION FOR ORIGIN
            // check whether the origin is a valid origin
            if (spName == null) {
                validPara = false;
                errorArr.add("missing origin");
            } else if (spName.trim().length() == 0) {
                validPara = false;
                errorArr.add("blank origin");
            } else {
                try {

                    if (spName.indexOf("[") > -1) {
                        spName = spName.replace("[", "");
                    } else if (spName.indexOf("]") > -1) {
                        spName = spName.replace("]", "");
                    }

                    spName.trim();
                    sDAO.getSemanticPlace(spName.trim()).toString();

                } catch (NullPointerException e) {
                    validPara = false;
                    errorArr.add("invalid origin");
                }

            }

            // VALIDATION FOR TOKEN
            if (isDirectQuery == null) {
                if (token == null) {
                    validPara = false;
                    errorArr.add("missing token");
                } else if (token.equals("")) {
                    validPara = false;
                    errorArr.add("blank token");
                } else {
                    try {
                        String username = JWTUtility.verify(token, sharedSecret);
                    } catch (JWTException e) {
                        validPara = false;
                        errorArr.add("invalid token");
                    }
                }
                // DATE VALIDATION
                try {
                    if (tsString == null) {
                        validPara = false;
                        errorArr.add("missing date");
                    } else if (tsString.equals("")) {
                        validPara = false;
                        errorArr.add("blank date");
                    } else {

                        //to customize the string
                        StringBuilder sb = new StringBuilder(tsString);
                        sb.deleteCharAt(10);
                        sb.insert(10, ' ');
                        tsString = sb.toString();

                        if (tsString.length() < 19) {
                            tsString = sb.toString() + ":00";
                        }

                        ts = Timestamp.valueOf(tsString);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    validPara = false;
                    errorArr.add("invalid date");
                }
            } else {
                // User accessing through web app
                // DATE VALIDATION
                try {
                    if (tsString == null) {
                        validPara = false;
                        errorArr.add("missing date");
                    } else if (tsString.equals("")) {
                        validPara = false;
                        errorArr.add("blank date");
                    } else {
                        //to customize the string
                        StringBuilder sb = new StringBuilder(tsString);
                        sb.deleteCharAt(10);
                        sb.insert(10, ' ');
                        tsString = sb.toString();

                        if (tsString.length() < 19) {
                            tsString = sb.toString() + ":00";
                        }

                        ts = Timestamp.valueOf(tsString);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    validPara = false;
                    errorArr.add("invalid date");
                }
            }

            if (validPara == false) {
                json.put("status", "error");
                json.put("messages", errorArr);
            } else {
                //creating Timestamp object
                ts = java.sql.Timestamp.valueOf(tsString);

                spName = spName.trim();

                if (spName.indexOf("[") != -1) {

                    StringBuilder sb1 = new StringBuilder(spName);
                    sb1.deleteCharAt(spName.indexOf("["));
                    spName = sb1.toString();
                } else if (spName.indexOf("]") != -1) {
                    StringBuilder sb1 = new StringBuilder(spName);
                    sb1.deleteCharAt(spName.indexOf("]"));
                    spName = sb1.toString();
                }

                int noVisitsPreviousSp = locatDAO.getNoPreviousSp(ts, spName);

                HashMap<String, Integer> result = locatDAO.getTopKNextPlace(ts, spName);
                int countNextPlaceUser = 0;

                if (result != null) {
                    json.put("status", "success");

                    // order the result according to the noVisits
                    List list1 = new LinkedList(result.entrySet());
                    // Defined Custom Comparator here
                    Collections.sort(list1, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            return ((Comparable) ((Map.Entry) (o2)).getValue())
                                    .compareTo(((Map.Entry) (o1)).getValue());
                        }
                    });

                    // Here I am copying the sorted list in HashMap
                    // using LinkedHashMap to preserve the insertion order
                    HashMap sortedHashMap = new LinkedHashMap();
                    for (Iterator it = list1.iterator(); it.hasNext();) {
                        Map.Entry entry = (Map.Entry) it.next();
                        sortedHashMap.put(entry.getKey(), entry.getValue());
                    }
                    // end of sorting

                    if (sortedHashMap.size() != 0) {
                        json.put("status", "success");

                        Set set = sortedHashMap.keySet();
                        ArrayList<String> keyArray = new ArrayList<>(set);

                        int ranking = 0;
                        int compareVisits = 0;

                        for (String s : keyArray) {
                            JSONObject queryResults = new JSONObject();

                            int noVisits = (int) sortedHashMap.get(s);

                            if (compareVisits == noVisits) {
                                queryResults.put("rank", ranking);
                            } else {
                                queryResults.put("rank", ++ranking);
                                compareVisits = noVisits;
                            }

                            queryResults.put("semantic-place", s);
                            queryResults.put("count", noVisits);

                            resultArr.add(queryResults);
                        }

                        json.put("results", resultArr);

                    }

                    Set set1 = sortedHashMap.keySet();
                    ArrayList<String> strArr = new ArrayList<>(set1);

                    for (String s : strArr) {
                        int count = (int) sortedHashMap.get(s);
                        countNextPlaceUser += count;
                    }

                    json.put("total-users", noVisitsPreviousSp);
                    json.put("total-next-place-users", countNextPlaceUser);

                    /*if (result_final.size() != 0) {
                        json.put("status", "success");

                        out.print("<table border='1'>");
                        out.print("<tr><th>" + "Position" + "</th>");
                        out.print("<th>" + "Semantic Place" + "</th>");
                        out.print("<th>" + "Number of people" + "</th>");
                        out.print("<th>" + "Percentage" + "</th></tr>");

                        out.write("Total Users: " + noVisitsPreviousSp + "<br>");
                        out.write("Total Next Place Users:  " + countNextPlaceUser);

                        for (int i = 0; i < Integer.parseInt(kString); i++) {

                            int noVisits = noVisitsArray.get(i);
                            out.print("<tr><td>" + (i + 1) + "</td><td>");
                            out.print("" + result_final.get(noVisits).toString());
                            out.print("</td><td>");
                            out.print("" + noVisits);
                            out.print("</td><td>");
                            out.print("" + (double) noVisits / noVisitsPreviousSp * 100 + "%");
                            out.print("</tr>");

                        }
                        out.print("</table>");

                        json.put("status", "success");
                        json.put("total-users", noVisitsPreviousSp);
                        json.put("total-next-place-users", countNextPlaceUser);

                    }*/
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            json.put("status", "error");
        } finally {
            System.out.println(json.toJSONString());
            out.write(json.toJSONString());
        }
    }

    //check 5 mins window
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
