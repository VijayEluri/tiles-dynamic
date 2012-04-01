<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles-el" prefix="t" %>
<%@ taglib uri="http://struts.apache.org/tags-html-el" prefix="h" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title><t:getAsString name="title"/></title>

	<!-- Stylesheet and javascript are constant, no need to polute tiles with them -->
	<link rel="stylesheet" type="text/css" href="<h:rewrite page='/default.css'></h:rewrite>"/>
	<script type="text/javascript" src="<h:rewrite page='/default.js'></h:rewrite>"></script>
	
	<!-- Determine here, if our application replaces or appends styles and scripts -->
</head>
<body>
<div id="master-container">
	<div id="header">
		<t:insert attribute="header" flush="true"/>
	</div>
	<div id="body">
		<t:insert attribute="body" flush="true"/>
	</div>
</div>
</body>
</html>