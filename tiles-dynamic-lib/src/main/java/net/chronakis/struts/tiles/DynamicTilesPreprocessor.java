package net.chronakis.struts.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.chain.Context;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.FactoryNotFoundException;
import org.apache.struts.tiles.NoSuchDefinitionException;
import org.apache.struts.tiles.TilesUtil;
import org.apache.struts.tiles.commands.TilesPreProcessor;
import org.apache.struts.tiles.xmlDefinition.DefinitionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tiles preprocessor has the ability to substitute variables on the
 * values of the definition path property and all attribute values
 * per request.
 * To use this you will need to use the specially crafted factory called
 * {@link DynamicTilesFactoryActual} which adds another equally useful feature
 * <p>
 * The format is the one used around ant, ${variableName} and it is looked up
 * in a Map<String, String> set in the servlet context, session and request objects
 * <p>
 * It gives you the very useful feature of being able to dynamically change the
 * template (layout.jsp) used without resorting to lengthy, ugly jsp definition files
 * <p>
 * Use the following functions to pass the variable Maps
 * <ul>
 *   <li> {@link DynamicTilesUtils#setVariables(Map, String, ServletRequest)}
 *   <li> {@link DynamicTilesUtils#setVariables(Map, String, javax.servlet.http.HttpSession)}
 *   <li> {@link DynamicTilesUtils#setVariables(Map, String, ServletRequest)}
 * </ul>
 * 
 * <p>
 * The search for the correct variable value works like this:
 * <ul>
 *   <li>Use the map provided by the servlet context</li>
 *   <li>Then add or override values from the session</li>
 *   <li>Finally add or override values from the request object</li>
 * </ul> 
 * 
 * @author Ioannis Chronakis
 *
 */
public class DynamicTilesPreprocessor extends TilesPreProcessor {
	private static final Logger logger = LoggerFactory.getLogger(DynamicTilesPreprocessor.class);

	// TODO Have to check thread safety
	DefinitionsFactory origFactory;
	Map<String, String> vars;

	/**
	 * This is the method that is called for the request chain
	 */
	@Override
	public boolean execute(Context context) throws Exception {
		logger.debug("execute() - START");
		
		ServletActionContext sacontext = (ServletActionContext) context;
		
		ModuleConfig moduleConfig = sacontext.getModuleConfig();
		String modulePrefix = "";
		if(moduleConfig == null)
			logger.warn("Could not find the module config from the servlet action context. Using default module as prefix");
		else
			modulePrefix = moduleConfig.getPrefix();
		
		logger.info("Using module prefix: " + modulePrefix);
		
		
		origFactory = DynamicTilesFactoryActual.getOriginalDefinitionsFactory(modulePrefix, sacontext.getContext());
		if(origFactory == null) {
			logger.debug("Cannot find the origFactory for prefix {} - Aborting variable substitution - execute() END", modulePrefix);
			return super.execute(context);
		}
		
		ForwardConfig forwardConfig = sacontext.getForwardConfig();
		if (forwardConfig == null || forwardConfig.getPath() == null) {
			logger.debug("No forwrd config - not a tiles forward - execute() END");
			return super.execute(context);
		}

		logger.debug("Searching for definiton for: " + forwardConfig.getPath());
		ComponentDefinition definition = getDefinition(forwardConfig.getPath(), sacontext.getRequest(), sacontext.getContext());
		
		if(definition == null) {
			logger.debug("No definition found for path: " + forwardConfig.getPath());
			return super.execute(context);
		}

		logger.debug("Definition for " + forwardConfig.getPath() + " is: name: " + definition.getName() + ", path: " + definition.getPath());
			
		// Retrieve the variables and process recursively
		vars = getVariables(modulePrefix, sacontext.getRequest(), sacontext.getContext());
		if(vars != null)
			processVars(definition, sacontext.getRequest(), sacontext.getContext());
		
		logger.debug("Definition for " + forwardConfig.getPath() + " became: name: " + definition.getName() + ", path: " + definition.getPath());

		logger.debug("TilesPreProcessor.execute() - END");
		return super.execute(context);
	}

	
	/**
	 * Process variable substitutions for definitions recursively, starting with the rootDef 
	 * 
	 * @throws DefinitionsFactoryException 
	 * @throws NoSuchDefinitionException */
	private void processVars(ComponentDefinition def, HttpServletRequest request, ServletContext context) throws NoSuchDefinitionException, DefinitionsFactoryException {
		// Find the original definition
		ComponentDefinition origDef = origFactory.getDefinition(def.getName(), request, context);
		if(origDef == null)
			logger.warn("Original definiton not found");
		else
			logger.info("Found original definiton");
		
		// Parse path variables
		def.setPath(substituteVars(origDef.getPath(), vars));
		
		
		// Replace attribute values
		Map attrs = def.getAttributes();
		for (Object attrKey : attrs.keySet())
			attrs.put(attrKey, substituteVars((String) origDef.getAttribute((String) attrKey), vars));
		
		// If this attribute is a tile, we will need to process it recursively
		// To avoid concurrent iteration, we will have to do this outside the above loop
		String attrValue;
		for (Object attrKey : attrs.keySet()) {
			attrValue = (String) attrs.get(attrKey);

			ComponentDefinition subDef = getDefinition(attrValue, request, context);
			if(subDef != null) {
				logger.debug("  Attribute "  + attrKey + " looks like a tile definition. Attempting to process nested tile");
				processVars(subDef, request, context);
			}
		}
	}
	
	/**
	 * <p>Substitute the actual variables.</p>
	 */
	private String substituteVars(String subject, Map<String, String> vars) {
		String result = subject;
		logger.trace("Processing string " + subject);
		
		Pattern p = Pattern.compile("\\$\\{.+?\\}");
		Matcher m = p.matcher(subject);
		while(m.find()) {
			String varKey = subject.substring(m.start() + 2 , m.end() - 1);
			String varVal = vars.get(varKey);
			if(varVal == null) {
				logger.warn("Variable " + varKey + " not found in any of the servletContext, session or request");
				continue;
			}
			logger.trace("Found replacement substring: " + varKey + " with value " + varVal);
			result = result.replaceAll("\\$\\{" + varKey + "\\}", varVal);
		}
		
		logger.trace("Processed string is: " + result);
		return result;
		
	}


	/**
	 * Wrapper to get a definition without the hassle
	 */
	private ComponentDefinition getDefinition(String name, ServletRequest request, ServletContext context) {
		ComponentDefinition def = null;
		try {
			 def = TilesUtil.getDefinition(name, request, context);
		} catch (FactoryNotFoundException e) {
			logger.error(e.getLocalizedMessage(), e);
		} catch (DefinitionsFactoryException e) {
			logger.warn(e.getLocalizedMessage(), e);
		}

		return def;
	}
	
	
	
	/**
	 * This is used to access the variables and reconstruct a Map. The rules are
	 * 
	 * <ul>
	 * <ul>
	 *   <li>Use the map provided by the servlet context</li>
	 *   <li>Then add or override values from the session</li>
	 *   <li>Finally add or override values from the request object</li>
	 * </ul>
	 *  
	 * @return The newly created map
	 */
	protected static Map<String, String>getVariables(String modulePrefix, HttpServletRequest request, ServletContext context) {
		HashMap<String, String> vars = new HashMap<String, String>();
		Map<String, String> varsPart;
		varsPart = DynamicTilesUtils.getVariables(modulePrefix, context);
		if(varsPart != null) vars.putAll(varsPart);
		
		varsPart = DynamicTilesUtils.getVariables(modulePrefix, request.getSession());
		if(varsPart != null) vars.putAll(varsPart);
		
		varsPart = DynamicTilesUtils.getVariables(modulePrefix, request);
		if(varsPart != null) vars.putAll(varsPart);
		
		return vars;
	}
}
