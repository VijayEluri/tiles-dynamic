<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://struts.apache.org/tags-html-el" prefix="h" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
This is the default header.<br/>
<form action="setLayout.do">
	<select name="masterLayoutPage" onchange="this.form.submit();">
		<option>Select a master template</option>
		<option value="/WEB-INF/templates/master.jsp">Default master template</option>
		<option value="/WEB-INF/templates/master-alt.jsp">Alternative master template</option>
	</select><br/>
	<ul>
		<li><a href="home.do">Home page</a></li>
		<li><a href="help.do">Help page</a></li>
		<li><a href="loggedinHome.do">My home page</a></li>
	</ul>
</form>
