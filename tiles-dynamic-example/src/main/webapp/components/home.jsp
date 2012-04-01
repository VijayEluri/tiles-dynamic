<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<p>This web project demonstrates how to use your custom TilesPreProcessor to dynamically modify tiles definitions during runtime</p>
<p>At the moment, it works only with tiles xml definitions.</p>
<p>It works and look great, you can change any tiles definition parameter from your code.
For example, you can change the template or the value of an attribute</p>
<h4>The advantages are: </h4>
<ul>
	<li>Perfect control, allowing things like changing themes and templates on the fly</li>
	<li>Works pretty well with nested tiles</li>
	<li>Very clean approach</li>
</ul>
<h4>The disadvantages are: </h4>
<ul>
	<li>If you want to use .jsp definitions, it will not work</li>
</ul>
<h4>The problems are: </h4>
<ul>
	<li>The substitution of the variables is happening only on first load. You will have to restart to change the behaviour</li>
	<li>The code is very dumb, but this was a feasibility test anyway.</li>
</ul>
<h4>The solutions are: </h4>
<ul>
	<li>The quick one is to maintain a map with the definition properties/attributes that were controlled by variables,
	where the original value will be held and this is which should be replaced</li>
	<li>The proper one is to subclass the DefinitionsFactory and allow to access the original XmlDefinition,
	so that we can substitute based on that</li>
</ul>
<h4>Future plans: </h4>
<ul>
	<li>Allow management of nested inherited tiles without the hassle of the replication.<br/>
	I already have a prototype implementation, but first I will have to take a look at the
	tiles 2 project because they claim they do it.</li>
</ul>
<p>Take a look at the following pages and then at the source code to see how you arrange dynamically the template:</p>
<ul>
	<li><a href="help.do">help.do/.jsp for flat template (no nesting)</a></li>
	<li><a href="task.do">task.do/jsp for nested tiles</a></li>
</ul>
<p>Just edit the above jsps and change the bean that controls the template play</p>