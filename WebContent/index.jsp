<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="gp" uri="com.caibowen.gplume.taglib"%>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Gplume Sample Page</title>
</head>
<body>
<div style="text-align:center; margin-top: 30px;">
<h1>${msg}</h1>
<hr/>
<h3><gp:msg k="authorName" /></h3>
<h3><gp:link nativeName="projectWebSite" url="http://www.caibowen.com/work.html#id_gplume" target="_blank"/></h3>
<h3><gp:link nativeName="githubPage" url="https://github.com/xkommando/Gplume" target="_blank"/></h3>
<hr/>
<br/>
<% String u = "/Gplume/your-birthday/" + new SimpleDateFormat("YYYY-MM-dd").format(new Date()); %>
<gp:link nativeName="today" url="<%=u%>"/> <gp:msg k="isYourBD"/>
<br/>
<br/>
<gp:msg k="enterBirthDay"/><br/>
<form action="/Gplume/act/post/date" method="post">
<table align="center">
<tr>
<td><gp:msg k="year"/></td> <td><input type="text" name="year"/></td>
</tr>

<tr>
<td><gp:msg k="month"/></td> <td><input type="text" name="month"/></td>
</tr>

<tr>
<td><gp:msg k="day"/></td> <td><input type="text" name="day"/></td>
</tr>

</table>

<input type="submit" value="<gp:msg k="submit"/>"/>
</form>

</div>

</body>
</html>