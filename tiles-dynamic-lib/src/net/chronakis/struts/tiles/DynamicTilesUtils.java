package net.chronakis.struts.tiles;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.ModuleUtils;

/**
 * 
 * @author Ioannis Chronakis
 *
 */
public class DynamicTilesUtils {
	static final String VARIABLES_KEY = "DynamicTilesPreprocessorVariables";
	
	/**
	 * (Borowed from the tiles implementation, but it was protected)
	 * 
     * Get the current ModuleConfig.
     * <br>
     * Lookup in the request and do selectModule if not found. The side effect
     * is, that the ModuleConfig object is set in the request if it was not present.
     * @param request Current request.
     * @param servletContext Current servlet context*.
     * @return The ModuleConfig for current request.
     */
    static ModuleConfig getModuleConfig(
        HttpServletRequest request,
        ServletContext servletContext) {

        ModuleConfig moduleConfig =
            ModuleUtils.getInstance().getModuleConfig(request);

        if (moduleConfig == null) {
            // ModuleConfig not found in current request. Select it.
            ModuleUtils.getInstance().selectModule(request, servletContext);
            moduleConfig = ModuleUtils.getInstance().getModuleConfig(request);
        }

        return moduleConfig;
    }


	public static void setVariable(String name, String value, String modulePrefix, ServletContext context) {
		DynamicTilesUtils.getVariables(modulePrefix, context).put(name, value);
	}

	public static void setVariable(String name, String value, String modulePrefix, HttpSession session) {
		DynamicTilesUtils.getVariables(modulePrefix, session).put(name, value);
	}

	public static void setVariable(String name, String value, String modulePrefix, ServletRequest request) {
		DynamicTilesUtils.getVariables(modulePrefix, request).put(name, value);
	}

	public static String getVariable(String name, String modulePrefix, ServletContext context) {
		return DynamicTilesUtils.getVariables(modulePrefix, context).get(name);
	}

	public static String getVariable(String name, String modulePrefix, HttpSession session) {
		return DynamicTilesUtils.getVariables(modulePrefix, session).get(name);
	}

	public static String getVariable(String name, String modulePrefix, ServletRequest request) {
		return DynamicTilesUtils.getVariables(modulePrefix, request).get(name);
	}

	public static void removeVariable(String name, String modulePrefix, ServletContext context) {
		DynamicTilesUtils.getVariables(modulePrefix, context).remove(name);
	}

	public static void removeVariable(String name, String modulePrefix, HttpSession session) {
		DynamicTilesUtils.getVariables(modulePrefix, session).remove(name);
	}

	public static void removeVariable(String name, String modulePrefix, ServletRequest request) {
		DynamicTilesUtils.getVariables(modulePrefix, request).remove(name);
	}

	/** Retrieves the variables from the servlet context. If they are not set, it creates an empty object */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getVariables(String modulePrefix, ServletContext context) {
		Map<String, String> vars = (Map<String, String>) context.getAttribute(VARIABLES_KEY);
		if(vars == null) {
			vars = new HashMap<String, String>();
			context.setAttribute(VARIABLES_KEY, vars);
		}
		return vars;
	}

	/** Retrieves the variables from the session. If they are not set, it creates an empty object */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getVariables(String modulePrefix, HttpSession session) {
		Map<String, String> vars = (Map<String, String>) session.getAttribute(VARIABLES_KEY);
		if(vars == null) {
			vars = new HashMap<String, String>();
			session.setAttribute(VARIABLES_KEY, vars);
		}
		return vars;
	}

	/** Retrieves the variables from the request. If they are not set, it creates an empty object */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getVariables(String modulePrefix, ServletRequest request) {
		Map<String, String> vars = (Map<String, String>) request.getAttribute(VARIABLES_KEY);
		if(vars == null) {
			vars = new HashMap<String, String>();
			request.setAttribute(VARIABLES_KEY, vars);
		}
		return vars;
	}

	public static void setVariables(Map<String, String> vars, String modulePrefix, ServletContext context) {
		context.setAttribute(VARIABLES_KEY, vars);
	}

	public static void setVariables(Map<String, String> vars, String modulePrefix, HttpSession session) {
		session.setAttribute(VARIABLES_KEY, vars);
	}

	public static void setVariables(Map<String, String> variables, String modulePrefix, ServletRequest request) {
		request.setAttribute(VARIABLES_KEY, variables);
	}

	public static void removeVariables(String modulePrefix, ServletContext context) {
		context.removeAttribute(VARIABLES_KEY);
	}

	public static void removeVariables(String modulePrefix, HttpSession session) {
		session.removeAttribute(VARIABLES_KEY);
	}

	public static void removeVariables(String modulePrefix, ServletRequest request) {
		request.removeAttribute(VARIABLES_KEY);
	}
}
