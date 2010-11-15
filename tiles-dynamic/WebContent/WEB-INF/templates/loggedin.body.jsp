<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles-el" prefix="t" %>
<%@ taglib uri="http://struts.apache.org/tags-html-el" prefix="h" %>
<div id="task-nav" style="width: 35%; float: left;">
	<t:insert attribute="nav" flush="true"/>
</div>
<div id="task-content" style="width: 60%; float: right;">
	<t:insert attribute="content" flush="true"/>
</div>
<div id="task-footer" style="clear: both;">
</div>
