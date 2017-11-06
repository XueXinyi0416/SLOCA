<%@page import="net.minidev.json.JSONObject"%>
<%@page import="entity.SemanticPlace"%>
<%@page import="java.util.ArrayList"%>
<%@page import="dao.SemanticPlaceDAO"%>
<%@page import="dao.ConnectionManager"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Connection"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->


<html>
    <head>
        <title>SLOCA</title>
        <meta cha rset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <!--Imports-->
        <%@include file="/WEB-INF/jspf/api.jspf"%>

        <style>

        </style>
    </head>
    <body>

        <nav class="navbar navbar-default">
            <div class="container">
                <div class="navbar-header">
                    <a class="navbar-brand" href="index.jsp">SLOCA</a>
                </div>
                <ul class="nav navbar-nav">
                    <c:if test="${sessionScope.VALIDATED == true}">
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                Basic Location Report<span class="caret"/>
                            </a>
                            <ul class="dropdown-menu">
                                <li><a href="#" onClick="breakdownByDemographic()">Breakdown By Year & Gender</a></li>
                                <li><a href="#" onClick="topKPopularPlaces()">Top-K Popular Places</a></li>
                                <li><a href="#" onClick="topKCompanion()">Top-K Companions</a></li>
                                <li><a href="#" onClick = "topKNextPopularPlaces()">Top-K Next Places</a></li>
                            </ul>
                        </li>
                        <li><a href="#" onClick = "doHeatMap()">Heatmap</a></li>
                        <li><a href="#" onClick = "doAutomaticGroup()">Automatic Group Detection</a><li>
                            <!-- If user is admin, then show Bootstrap function -->
                            <c:if test="${sessionScope.isAdmin == true}">
                            <li><a href="#" onClick="doBootstrap()">Bootstrap</a></li>
                            </c:if>
                    </c:if>
                </ul>
                <div class="navbar-right">
                    <c:choose>
                        <c:when test="${sessionScope.VALIDATED == true}">
                            <button class="btn btn-default navbar-btn" onClick="logout()">Log Out</button>
                        </c:when>
                        <c:otherwise>
                            <form id="loginForm" action="LoginServlet"  method="post" class="navbar-form">
                                <input type="username" class="form-control" name="username" placeholder="Username">
                                <input type="password" class="form-control" name="password" placeholder="Password">
                                <input type="hidden" name="isWEB" value="true"/>
                                <button  type="submit" class="btn btn-default">Log In</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>
        </nav>

        <div id="main" class="container" style="background-color:#cccccc">
            <div class="page-header">
                <h1>SMU Location Analytics Service</h1>
            </div>

            <c:if test="${sessionScope.VALIDATED == null}">
                <h4>Please Log In First!</h4>
            </c:if>

        </div>



        <!--breakdown by year, school, Gender page-->
        <div id="breakdownByDemographic" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Breakdown By Year, Gender and School</h1>
                </div>
            </div>

            <div class="container">
                <div class="page-header">
                    <h4> Please select the type of breakdown</h4>
                </div>
                <form id = "breakdownForm" method = "post" action="basicLocationReportServlet">
                    Select Date and Time:<br>
                    <input type="datetime-local" name="date" ><br><br>
                    First Parameter: <br>
                    <input list="first" name="order" value="year">
                    <datalist id="first">
                        <option value="school">school</option>
                        <option value="year">Year</option>
                        <option value="gender">Gender</option>
                    </datalist> <br><br>
                    Second Parameter: <br>
                    <input list="second" name="order" value="school">
                    <datalist id="second">
                        <option value="">nil</option>
                        <option value="school">school</option>
                        <option value="year">Year</option>
                        <option value="gender">Gender</option>
                    </datalist> <br><br>
                    Third Parameter: <br>
                    <input list="third" name="order" value="gender">
                    <datalist id="third">
                        <option value="">nil</option>
                        <option value="school">school</option>
                        <option value="year">Year</option>
                        <option value="gender">Gender</option>
                    </datalist> <br><br>
                    <input type="hidden" name="hidden" value="123">
                    <input type="submit" value="Submit">
                </form> 
            </div>
        </div>

        <!--Top K Companion-->
        <div id="topKCompanion" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Top K Companions</h1>
                </div>
            </div>

            <div class="container">
                <form id = "kCompanionForm" method = "post" action="topKCompanionServlet">
                    <div class="page-header">
                        <h4> Please select the Top K Companions</h4>
                    </div>
                    Select ranking: <br>   
                    <select name="k">
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option selected value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                        <option value="6">6</option>
                        <option value="7">7</option>
                        <option value="8">8</option>
                        <option value="9">9</option>
                        <option value="10">10</option>
                    </select><br><br>
                    Please enter:<br>  
                    <input type="text" name="mac-address" placeholder="mac-address" value="022c764d5a43955db30ce9b314e135eb12f5eeec"><br><br>   
                    Please select the time:<br>  
                    <input type="datetime-local" name="date" ><br>
                    <input type="hidden" name="hidden" value="notJSON">
                    <br>
                    <input type="submit" value="Submit">
                </form>  
            </div>
        </div>

        <!-- breakdown By Year and Gender result-->
        <div id="breakdownByYearGender" style="display:none" class="container" >
            <span class="order"></span>
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Breakdown Result</h1><br/>

                </div>
            </div>

            <div class="container">
                <div class = "container">
                    <table class =" table table-boardered table-striped"
                           id = "breakdown_table">
                        <br>
                        <tr>

                        </tr>
                    </table>
                </div>


            </div>
        </div>

        <!-- TOP K COMPANION RESULT -->
        <div id="topKCompanionResult" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Top K Companions Results</h1>
                </div>
            </div>

            <div class="container">
                <div class="page-header">
                    <table class =" table table-boardered table-striped"
                           id = "topKCompanion_table">               
                        <tr>
                            <th>Rank</th>
                            <th>Companion</th>
                            <th>Mac-Address</th>
                            <th>Time together</th>
                        </tr>
                    </table>
                </div>
            </div>
        </div>

        <!--topKPopularPlaces-->
        <div id="topKPopularPlaces" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Top K Popular Places</h1>
                </div>
            </div>

            <div class="container">
                <form id = "kplaceForm" method = "post" action="topKPlaceServlet">
                    <div class="page-header">
                        <h4> Please select the Top K Popular Places </h4>
                    </div>
                    Please select the time
                    <input type="datetime-local" name="date" ><br><br>
                    Select ranking: 
                    <select name="k">
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option selected value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                        <option value="6">6</option>
                        <option value="7">7</option>
                        <option value="8">8</option>
                        <option value="9">9</option>
                        <option value="10">10</option>

                    </select>
                    <input type="hidden" name="hidden" value="called">
                    <br>
                    <br>
                    <input type="submit" value="Submit">
                </form>  
            </div>
        </div>

        <!-- topKPlaceResult-->
        <div id="topKPlaceResult" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Top K Popular result</h1><br/>
                </div>
            </div>

            <div class="container">
                <div class = "container">
                    <table class =" table table-boardered table-striped"
                           id = "topKPlace_table">
                        <br>
                        <tr>
                            <th>Rank</th>
                            <th>Semantic Place</th>
                            <th>Count</th>
                        </tr>
                    </table>
                </div>

            </div>
        </div>

        <!--topKNextPopularPlaces-->
        <div id="topKNextPopularPlaces" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Top K Next Popular Places</h1>
                </div>
            </div>

            <div class="container">
                <form id = "knextplaceForm" method = "post" action="topKNextPlaceServlet">
                    <div class="page-header">
                        <h4> Please select the Top K Next Popular Places </h4>
                    </div>
                    Please select the date:
                    <input type="datetime-local" name="date" ><br><br>
                    Select Origin: 
                    <select name="origin">
                        <jsp:useBean id="placeBean"  class="dao.SemanticPlaceDAO">
                            <c:forEach var="x" items="${placeBean.semanticName}">
                                <option value =" ${x}"> ${x}</option>
                            </c:forEach>    

                        </jsp:useBean>

                        <!--insert semantic place -->


                    </select>
                    <br>
                    <br>
                    Select K:
                    <select name="k">
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option selected value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                        <option value="6">6</option>
                        <option value="7">7</option>
                        <option value="8">8</option>
                        <option value="9">9</option>
                        <option value="10">10</option>

                    </select>
                    <input type="hidden" name="hidden" value="notJSON">
                    <br>
                    <br>
                    <input type="submit" value="Submit">
                </form>  
            </div>
        </div>

        <!-- topKNextPlaceResult-->
        <div id="topKNextPlaceResult" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Top K Next Popular result</h1><br/>
                </div>
            </div>

            <div class="container">
                <div class = "container">
                    <table class =" table table-boardered table-striped"
                           id = "topKNextPlace_table">
                        <br>
                        <tr>
                            <th>Rank</th>
                            <th>Semantic Place</th>
                            <th>Count</th>
                        </tr>
                    </table>

                    <table class =" table table-boardered table-striped"
                           id = "overall_table">
                        <br>
                        <tr>
                            <th>Total User</th>
                            <th>Total Next Place User</th>
                        </tr>
                    </table>

                </div>

            </div>
        </div>

        <!--doHeatMap-->
        <div id="doHeatMap" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Heatmap</h1>
                </div>
            </div>

            <div class="container">
                <form id = "heatMapForm" method = "post" action="HeatMapServlet">
                    <div class="page-header">
                        <h4> Please select the time and place for the Heatmap </h4>
                    </div>
                    Please select the time
                    <input type="datetime-local" name="date" ><br>
                    <input type="hidden" name="hidden" value="notJSON">
                    <br>
                    Select floor: 
                    <select name="floor">
                        <option value="B1">B1</option>
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option selected value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                    </select>
                    <input type="hidden" name="hidden" value="notJSON">
                    <br>
                    <br>
                    <input type="submit" value="Submit">
                </form>  
            </div>
        </div>   

        <!-- HeatMapResult-->
        <div id="heatmapResult" style="display:none" class="container" >
            <div style="background-color:#cccccc" class="container">
                <div class="page-header">
                    <h1>Heatmap result</h1><br/>
                </div>
            </div>

            <div class="container">
                <div class = "container">
                    <table class =" table table-boardered table-striped"
                           id = "heatmap_table">
                        <br>
                        <tr>
                            <th>Semantic Place</th>
                            <th>Count</th>
                            <th>Crowd Density</th>
                        </tr>
                    </table>
                </div>

            </div>
        </div>

        <!--Automatic Group Detection-->
        <%@include file="/WEB-INF/jspf/automaticGroupDetection.jspf" %>
        <%@include file="/WEB-INF/jspf/automaticGroupDetectionResult.jspf" %>
        <%@include file="/WEB-INF/jspf/bootstrap.jspf" %>
    </body>
</html>

<script>
    var currentPage = $("#main");
    //AJAXIFIED LOGIN
    $("#loginForm").on("submit", function (event) {
        $.post("LoginServlet", $("#loginForm").serialize(), function (response) {
            if (response.status == "failure") {
                alert("Invalid Username/Password");
            } else if (response.status == "success") {
                window.location = "index.jsp"
            }
        });
        event.preventDefault();
    });

    //display breakdown by gender and year result
    $("#breakdownForm").on("submit", function (event) {
        $.post("basicLocationReportServlet", $("#breakdownForm").serialize(), function (response) {

            currentPage.hide();
            $("#breakdownByYearGender").show();
            currentPage = $("#breakdownByYearGender")

            $('#breakdown_table td').remove();
            $('#breakdown_table tr').remove();


            // check how many levels are there
            var noLevel = 1;
            if (response["breakdown"][0]["breakdown"] != null) {
                noLevel++;
                if (response["breakdown"][0]["breakdown"][0]["breakdown"] != null) {
                    noLevel++;
                }
            }

            if (noLevel == 3) {

                var name1 = Object.keys(response["breakdown"][0])[0];
                var name2 = Object.keys(response["breakdown"][0]["breakdown"][0])[0];
                var name3 = Object.keys(response["breakdown"][0]["breakdown"][0]["breakdown"][0])[0];

                $('#breakdown_table').append("<tr><th>" + name1 + "</th><th>" + name2 + "</th><th>" + name3 + "</th></tr>");


                for (var i = 0; i < response["breakdown"].length; i++) {
                    for (var x = 0; x < response["breakdown"][i]["breakdown"].length; x++) {
                        for (var y = 0; y < response["breakdown"][i]["breakdown"][x]["breakdown"].length; y++) {
                            if (x == 0 && y == 0) {
                                $('#breakdown_table').append("<tr><td>" + response["breakdown"][i][name1] + "<br/>Count: " + response["breakdown"][i]["count"] + "</td>\n\
                                    <td>" + response["breakdown"][i]["breakdown"][x][name2] + "<br/>Count: " + response["breakdown"][i]["breakdown"][x]["count"] + "</td>\n\
                                    <td>" + response["breakdown"][i]["breakdown"][x]["breakdown"][y][name3] + "<br/>Count: " + response["breakdown"][i]["breakdown"][x]["breakdown"][y]["count"] + "</td></tr>");
                            } else if (y == 0) {
                                $('#breakdown_table').append("<tr><td></td>\n\
                                    <td>" + response["breakdown"][i]["breakdown"][x][name2] + "<br/>Count: " + response["breakdown"][i]["breakdown"][x]["count"] + "</td>\n\
                                    <td>" + response["breakdown"][i]["breakdown"][x]["breakdown"][y][name3] + "<br/>Count: " + response["breakdown"][i]["breakdown"][x]["breakdown"][y]["count"] + "</td></tr>");
                            } else {
                                $('#breakdown_table').append("<tr><td></td>\n\
                                    <td></td>\n\
                                    <td>" + response["breakdown"][i]["breakdown"][x]["breakdown"][y][name3] + "<br/>Count: " + response["breakdown"][i]["breakdown"][x]["breakdown"][y]["count"] + "</td></tr>");
                            }
                        }
                    }
                }
            }


            if (noLevel == 2) {
                var name1 = Object.keys(response["breakdown"][0])[0];
                var name2 = Object.keys(response["breakdown"][0]["breakdown"][0])[0];

                $('#breakdown_table').append("<tr><th>" + name1 + "</th><th>" + name2 + "</th></tr>");

                for (var i = 0; i < response["breakdown"].length; i++) {
                    for (var x = 0; x < response["breakdown"][i]["breakdown"].length; x++) {
                        if (x == 0) {
                            $('#breakdown_table').append("<tr><td>" + response["breakdown"][i][name1] + "<br/>Count: " + response["breakdown"][i]["count"] + "</td>\n\
                                <td>" + response["breakdown"][i]["breakdown"][x][name2] + "<br/>Count: " + response["breakdown"][i]["breakdown"][x]["count"] + "</td></tr>");
                        } else {
                            $('#breakdown_table').append("<tr><td>" + "</td>\n\
                                <td>" + response["breakdown"][i]["breakdown"][x][name2] + "<br/>Count: " + response["breakdown"][i]["breakdown"][x]["count"] + "</td></tr>");
                        }

                    }
                }
            }


            if (noLevel == 1) {
                var name1 = Object.keys(response["breakdown"][0])[0];

                $('#breakdown_table').append("<tr><th>" + name1 + "</th></tr>");

                for (var i = 0; i < response["breakdown"].length; i++) {
                    $('#breakdown_table').append("<tr><td>" + response["breakdown"][i][name1] + "<br/>Count: " + response["breakdown"][i]["count"] + "</td></tr>");
                }



            }






        });
        event.preventDefault();
    });





    //display top k place result
    $("#kplaceForm").on("submit", function (event) {
        $.post("topKPlaceServlet", $("#kplaceForm").serialize(), function (response) {

            currentPage.hide();
            $("#topKPlaceResult").show();
            currentPage = $("#topKPlaceResult")

            $('#topKPlace_table td').remove();

            var rank = 0;


            for (var i = 0; i < response["results"].length; i++) {

                if (response["results"][i]["rank"] !== rank) {
                    $('#topKPlace_table').append("<tr>\n\
                                              <td>" + response["results"][i]["rank"] + "</td>\n\
                                              <td>" + response["results"][i]["semantic-place"] + "</td>\n\
                                              <td>" + response["results"][i]["count"] + "</td>\n\
                                              </tr>");
                    rank++;
                } else {

                    $('#topKPlace_table').append("<tr>\n\
                                              <td>" + " " + "</td>\n\
                                              <td>" + response["results"][i]["semantic-place"] + "</td>\n\
                                              <td>" + response["results"][i]["count"] + "</td>\n\
                                              </tr>");
                }

            }
        });
        event.preventDefault();
    });

    //display top k next place result
    $("#knextplaceForm").on("submit", function (event) {
        $.post("topKNextPlaceServlet", $("#knextplaceForm").serialize(), function (response) {

            currentPage.hide();
            $("#topKNextPlaceResult").show();
            currentPage = $("#topKNextPlaceResult")

            $('#topKNextPlace_table td').remove();
            $('#overall_table td').remove();

            var rank = 0;

            for (var i = 0; i < response["results"].length; i++) {
                if (response["results"][i]["rank"] !== rank) {
                    $('#topKNextPlace_table').append("<tr>\n\
                                              <td>" + response["results"][i]["rank"] + "</td>\n\
                                              <td>" + response["results"][i]["semantic-place"] + "</td>\n\
                                              <td>" + response["results"][i]["count"] + " (" + parseInt(response["results"][i]["count"] / response["total-users"] * 100) + "%) " + "</td>\n\
                                              </tr>");
                    rank++;
                } else {

                    $('#topKNextPlace_table').append("<tr>\n\
                                              <td>" + " " + "</td>\n\
                                              <td>" + response["results"][i]["semantic-place"] + "</td>\n\
                                              <td>" + response["results"][i]["count"] + " (" + parseInt(response["results"][i]["count"] / response["total-users"] * 100) + "%) " + "</td>\n\
                                              </tr>");
                }

            }



            $('#overall_table').append("<tr>\n\
                                              <td>" + response["total-users"] + "</td>\n\
                                              <td>" + response["total-next-place-users"] + "</td>\n\
                                              </tr>");

        });
        event.preventDefault();
    });

    // display heatmap result
    $("#heatMapForm").on("submit", function (event) {
        $.post("HeatMapServlet", $("#heatMapForm").serialize(), function (response) {

            currentPage.hide();
            $("#heatmapResult").show();
            currentPage = $("#heatmapResult")

            $('#heatmap_table td').remove();

            var str = "";

            for (var i = 0; i < response["heatmap"].length; i++) {
                str += "<tr>\n\
                        <td>" + response["heatmap"][i]["semantic-place"] + "</td>\n\
                        <td>" + response["heatmap"][i]["num-people"] + "</td>";

                if (response["heatmap"][i]["crowd-density"] == 0) {
                    str += "<td style='background-color:#09FAD9'>" + response["heatmap"][i]["crowd-density"] + "</td></tr>";
                } else if (response["heatmap"][i]["crowd-density"] == 1) {
                    str += "<td style='background-color:#A5FA09'>" + response["heatmap"][i]["crowd-density"] + "</td></tr>";
                } else if (response["heatmap"][i]["crowd-density"] == 2) {
                    str += "<td style='background-color:#DCFA09'>" + response["heatmap"][i]["crowd-density"] + "</td></tr>";
                } else if (response["heatmap"][i]["crowd-density"] == 3) {
                    str += "<td style='background-color:#FAF609'>" + response["heatmap"][i]["crowd-density"] + "</td></tr>";
                } else if (response["heatmap"][i]["crowd-density"] == 4) {
                    str += "<td style='background-color:#FABF09'>" + response["heatmap"][i]["crowd-density"] + "</td></tr>";
                } else if (response["heatmap"][i]["crowd-density"] == 5) {
                    str += "<td style='background-color:#FA8709'>" + response["heatmap"][i]["crowd-density"] + "</td></tr>";
                } else if (response["heatmap"][i]["crowd-density"] == 6) {
                    str += "<td style='background-color:#FA3409'>" + response["heatmap"][i]["crowd-density"] + "</td></tr>";
                }


            }

            $('#heatmap_table').append(str);
        });
        event.preventDefault();
    });

    // display companion result
    $("#kCompanionForm").on("submit", function (event) {
        $.post("topKCompanionServlet", $("#kCompanionForm").serialize(), function (response) {

            currentPage.hide();
            $("#topKCompanionResult").show();
            currentPage = $("#topKCompanionResult")

            $('#topKCompanion_table td').remove();


            var rank = 0;

            for (var i = 0; i < response["results"].length; i++) {
                if (response["results"][i]["rank"] !== rank) {
                    $('#topKCompanion_table').append("<tr>\n\
                                              <td>" + response["results"][i]["rank"] + "</td>\n\
                                              <td>" + response["results"][i]["companion"] + "</td>\n\
                                              <td>" + response["results"][i]["mac-address"] + "</td>\n\
                                              <td>" + response["results"][i]["time-together"] + "</td>\n\
                                              </tr>");
                    rank++;
                } else {

                    $('#topKCompanion_table').append("<tr>\n\
                                              <td>" + "" + "</td>\n\
                                              <td>" + response["results"][i]["companion"] + "</td>\n\
                                              <td>" + response["results"][i]["mac-address"] + "</td>\n\
                                              <td>" + response["results"][i]["time-together"] + "</td>\n\
                                              </tr>");
                }

            }

        });
        event.preventDefault();
    });

    $("#automaticGroupForm").on("submit", function (event) {
        event.preventDefault();
        $.ajax({
            type: "post",
            data: $("#automaticGroupForm").serialize(),
            url: "AutomaticGroupServlet",
            timeout: 60000,
            success: function (response) {
                currentPage.hide();
                currentPage = $("#automaticGroupDetectionResult");
                currentPage.show();
                $("#agd_size").load(location.href + " #agd_size");
            }
        });
    });
    
    



    //AJAXIFIED BOOTSTRAP FILE SUBMIT

    //LOGOUT BUTTON
    function logout() {
        window.location.href = "logout.jsp";
    }

    //BOOTSTRAP
    function doBootstrap() {
        currentPage.hide();
        $("#bootstrap").show();
        currentPage = $("#bootstrap")
    }
    function breakdownByDemographic() {
        currentPage.hide();
        $("#breakdownByDemographic").show();
        currentPage = $("#breakdownByDemographic")
    }

    // TOP K COMPANION
    function topKCompanion() {
        currentPage.hide();
        $("#topKCompanion").show();
        currentPage = $("#topKCompanion")
    }

    function topKPopularPlaces() {
        currentPage.hide();
        $("#topKPopularPlaces").show();
        currentPage = $("#topKPopularPlaces")
    }

    function topKNextPopularPlaces() {
        currentPage.hide();
        $("#topKNextPopularPlaces").show();
        currentPage = $("#topKNextPopularPlaces")
    }

    function doHeatMap() {
        currentPage.hide();
        $("#doHeatMap").show();
        currentPage = $("#doHeatMap")
    }

    function doAutomaticGroup() {
        currentPage.hide();
        $("#automaticGroup").show();
        currentPage = $("#automaticGroup")
    }
</script>
