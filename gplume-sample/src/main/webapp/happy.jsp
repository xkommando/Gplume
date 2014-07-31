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
${msg}
<br/>
<%= ((Date)request.getAttribute("date")).toString() %>
</body>
</html>