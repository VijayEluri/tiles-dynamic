package net.chronakis.struts.tiles.example;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import net.chronakis.struts.tiles.DynamicTilesPreprocessor;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

public class DynamicTilesExampleStrutsPlugin implements PlugIn {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * Just set the substitution variables to use the plugin
	 */
	@Override
	public void init(ActionServlet actionServlet, ModuleConfig moduleConfig) throws ServletException {
		// You can setup here the startup variables
		// For now we do not care because they are set by the supplied .default.properties file
//		Map<String, String> defaultVars = new HashMap<String, String>();
//		defaultVars.put("masterLayoutPage", "/WEB-INF/templates/master.jsp");
//		defaultVars.put("loggedinBodyLayoutPage", "/WEB-INF/templates/loggedin.body.jsp");
//		DynamicTilesPreprocessor.setServletVariables(defaultVars, actionServlet.getServletContext());
	}
	
}
