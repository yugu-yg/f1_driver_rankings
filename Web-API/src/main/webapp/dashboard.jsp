<%@ page import="java.util.ArrayList" %>
<%@ page import="WebService.Log" %>
<%--
Author:Yu Gu AndrewID: ygu3
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Dashboard</title>
</head>
<body>
<h1>Operation Analytics</h1>
<br>The Most Searched Year:<br>
<% String yearResult = (String) request.getAttribute("yearResult");%>
<%= yearResult %><br>
<br>The Most Searched Position:<br>
<% String positionResult = (String) request.getAttribute("positionResult");%>
<%= positionResult %><br>
<br>The Highest Searched Points<br>
<% String pointResult = (String) request.getAttribute("pointResult");%>
<%= pointResult %><br>
<h1>Logs</h1>
<% ArrayList<Log> logs = (ArrayList<Log>) request.getAttribute("logs");%>
<% for (int i = 1; i <= logs.size(); i++) { %>
<%= i + logs.get(i - 1).toString() %><br>
<% } %>
<br>
</body>
</html>
