<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<h3>Logged in navigation</h3>
<form action="setLayout.do">
	<select name="loggedinBodyLayoutPage" onchange="this.form.submit();">
		<option>Select a loggedin body template</option>
		<option value="/WEB-INF/templates/loggedin.body.jsp">Default loggedin body template</option>
		<option value="/WEB-INF/templates/loggedin.body-alt.jsp">Alternative loggedin body template</option>
	</select><br/>
	<ul>
		<li><a href="loggedinHome.do">My home</a></li>
		<li><a href="loggedinMessages.do">My Messages</a></li>
	</ul>
	
</form>
