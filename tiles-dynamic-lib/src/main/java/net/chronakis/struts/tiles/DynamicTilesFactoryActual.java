package net.chronakis.struts.tiles;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts.tiles.DefinitionsFactoryConfig;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.TilesException;
import org.apache.struts.tiles.xmlDefinition.DefinitionsFactory;
import org.apache.struts.tiles.xmlDefinition.I18nFactorySet;
import org.apache.struts.tiles.xmlDefinition.XmlDefinition;
import org.apache.struts.tiles.xmlDefinition.XmlDefinitionsSet;
import org.apache.struts.tiles.xmlDefinition.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This is a custom factory that adds two new features to tiles</p>
 * <ol>
 *   <li>Ant style ${varName} variable substitution for the path property and all the attribute values at runtime
 *       (Requires the @link{DynamicTilesPreprocessor} to work)</li>
 *   <li>Very simplified inheritance syntax when using nested tiles.</li>
 * </ol>
 * 
 * <p>Regarding the variable substitution please read the javadoc page for @link{DynamicTilesPreprocessor}</p>
 * <p>Tiles support inheritance and works well till the moment you decide to use it with nested tiles.<br/>
 * Use case: A page with a header and a content component named master, header, body respecitively</p>
 * <pre>
 {@literal 
	definition name="master" path="masterLayout.jsp"
		put name="title"  value="Master default title"
		put name="header" value="/components/header.jsp"
		put name="body"   value="body_default_value"
	/definition
 }
 *</pre> 
 * <p>When e.g. user is logged in, then the content will use another tile as its value e.g. task.body<br/>
 * This loggedin.body will consist of two tiles, nav and content.<br/>
 * <pre>
 {@literal 
	definition name="loggedin.body" page="loggedin.bodyBodyLayout.jsp"
		put name="nav" 	   value="/components/nav.jsp"
		put name="content" value="content_default_value"
	/definition
  }
 * </pre>
 * 
 * Then you will define a set of pages that the user is logged in like this:
 * <pre>
 {@literal 
	definition name="loggedin" extends="master"
		put name="body"        value="loggedin.body"
	/definition
 }
 * </pre>
 * 
 * <p>Until now everything looks fine. But you will usually want to define various pages
 * of the logged in user. With standard tiles this is a real pain:<br/>
 * For each loggedin page, you will have to extend the logged in body to override the content
 * and then extend the logged in page and set the new body tile.<br/>
 * Let's see an example. e.g. you want a messages.page and a friends.page<br/>
 * 
 * {@literal 
	definition name="loggedin.page.body" extends="loggedin.body"
		<put name="content"   value="/components/task.jsp"
	/definition
	<definition name="messages.page" extends="loggedin"
		put name="body"   value="loggedin.page.body"
	/definition
 }
 *
 * <p>This can became quickly unreadable in real world with more attributes and difficult to manage</p>
 * <p>The DynamicTilesFactoryActual simplifies this dramatically by eliminating the need to extend
 * the nested tile. You can refer to the nested tile attributes (e.g. content) from within the
 * outside tile (e.g. messages.page) using the slash (/) character like this: <b>loggedin.body/content</b></p>
 * <pre>
  {@literal 
	definition name="task.page" extends="task"
		<put name="task.body/content"   value="/components/task.jsp"
	/definition
   }
   </pre>
 * 
 * <p>This makes thing enormously more readable for large tiles definitions<p>
 * 
 * <p><b>How it works</b><p>
 * 
 * <p>The DynamicTilesFactoryActual hooks in the loading process of the configuration file
 * and generates the missing definitions (called implied definitions) and sets the
 * attributes accordingly</p>
 * 
 * <p>To see those definitions, set the logger level of DynamicTilesFactoryActual to DEBUG or TRACE.
 * TRACE will print the effective file in the stdout and also create a file
 * in the same folder with the original tiles-config.xml (or anyway it is named)
 * named like tiles-config.effective.xml (or similarly). DEBUG level will only
 * produce this file<p>
 *  
 * <p><b>Known problems</b><p>
 * 
 * <p>It only works with one level of nested tiles. With the first opportunity I will convert it
 * to process nesting recursively, but testing is a pain because the number of implied definition
 * you will have to produce is exponential to the number of nested levels.</p>
 * 
 * <p><b>What if you don't want both features (variable substitution & nested inheritance syntax)</b><p>
 * 
 * <p>This is fine. DynamicTilesFactoryActual behaves exactly as the standard tiles if it does not find any
 * slashes (/) in the attribute names. In addition, if you do not want the variable substitution,
 * you can use it with the standard TilesPreprocessor and avoid using the provided custom chain-config.xml</p>
 * 
 * @author Ioannis Chronakis
 *
 */
public class DynamicTilesFactoryActual extends I18nFactorySet {
	private static final Logger log = LoggerFactory.getLogger(DynamicTilesFactoryActual.class);

	private static final String ORIG_DEFS_FACTORY_KEY = "tiles.original.definitions.factory";

	// These are for dumping the intermediate file
	private static String [] DEFPROPS = {
		"name", "path", "extends", "role"
	};

	private static final String XMLHEADER =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
		"<!DOCTYPE tiles-definitions PUBLIC\n" +
		"\"-//Apache Software Foundation//DTD Tiles Configuration 1.3//EN\"\n" +
		"\"http://struts.apache.org/dtds/tiles-config_1_3.dtd\">\n";


	/**
	 * This will hold a reference to the current xml config so that we can create
	 * a second definitions factory with the original values that we need to
	 * be able to do run time substitutions without reloading
	 */
	XmlDefinitionsSet xmlConfig;
	
	/**
	 * The factory with the original definitions
	 */
	DefinitionsFactory origFactory;
	
    /**
     * Factory configuration,
     */
    private DefinitionsFactoryConfig config = null;


    /** Just provide the constructors of the underlying I18nFactorySet */
	public DynamicTilesFactoryActual() {
		super();
	}

	
	/**
	 * This is very important to be called before the init by the wrapper!<br/>
	 * 
	 * It sets the configuration that contains the factoryName which is the module prefix
	 * and it is vital for compatibility with modules
	 *  
	 * @param config
	 */
	public void setConfig(DefinitionsFactoryConfig config) {
		this.config = config;
	}


	@Override
	public void initFactory(ServletContext servletContext, Map properties) throws DefinitionsFactoryException {
		// Call our superclass initialisation. This method will call the overriding methods of this class
		super.initFactory(servletContext, properties);
		
		// If init was not set, throw an exception
		if(config == null)
			throw new DefinitionsFactoryException("initFactory() called before calling the setConfig(). This disables the compatibility workaround. Please call the setConfig() before initFactory()");
		
		// Create a copy of the original factory and store it on the servlet
		origFactory = new DefinitionsFactory(xmlConfig);
		setOriginalDefinitionsFactory(origFactory, config.getFactoryName(), servletContext);

		// Read the default variables from the provided file if any
		String filename = (String) properties.get(DEFINITIONS_CONFIG_PARAMETER_NAME);
		Map<String, String> vars = null;
		try {
			vars = readDefaultVariables(servletContext.getRealPath(filename));
			DynamicTilesUtils.setVariables(vars, config.getFactoryName(), servletContext);
		} catch (IOException e) {
			log.warn("Cound not process default variable values: " + e);
		}
	}

	
	/**
	 * Override the default xml parser to use our own extensions
	 */
	@Override
	protected XmlDefinitionsSet parseXmlFile(ServletContext servletContext,	String filename, XmlDefinitionsSet xmlDefinitions) throws DefinitionsFactoryException {
		XmlDefinitionsSet rootXmlConfig = super.parseXmlFile(servletContext, filename, xmlDefinitions);

		// Here is where I will hook my function, brilliant!!!
		// This function is not only called at load time, but foreach request based on the locale suffix
		// If the file has already been processed, the rootXmlConfig is not reloaded and it is null
		// We should processes it only on the first time
		if (rootXmlConfig == null)
			return null;
		
        try {
			process(rootXmlConfig);
		} catch (TilesException e) {
			throw new DefinitionsFactoryException(e);
		}
        
		// Dump the file if it is requested
		if(log.isDebugEnabled())
			dump(rootXmlConfig, servletContext.getRealPath(filename));
		
		// Hold a copy
		this.xmlConfig = rootXmlConfig;
		
		return rootXmlConfig;
	}
	

	/**
	 * <p>This processes the loaded special syntax xml definitions to produce
	 * the derived standard syntax definition set<p>
	 * <p>There is no copy, the processed definitions are put back in the same definitions set</p>
	 *   
	 * FIXME Add error control and reporting
	 * 
	 * @param definitions
	 * @throws TilesException
	 */
	protected static void process(XmlDefinitionsSet definitions) throws DefinitionsFactoryException {
		// Now traverse the tree and modify it
		Map<String, XmlDefinition> defsMap = (Map<String, XmlDefinition>) definitions.getDefinitions();
		Map<String, XmlDefinition> impliedMap = new HashMap<String, XmlDefinition>();

		for (XmlDefinition def : defsMap.values()) {
			Map<String, String> impliedAttrs = new HashMap<String, String>();	// The implied attrs to add
			List<String> customAttrs = new LinkedList<String>();	// The custom attrs to remove
			Map attrs = def.getAttributes();
			for (Object attrKey : attrs.keySet()) {
				String attrName = attrKey.toString();
				if(!attrName.matches(".+?/.+")) continue;
				
				// An attribute that implies a definition
				String parts[] = attrName.split("/");
				if(parts.length < 2)
					throw new DefinitionsFactoryException("Malformed nested reference " + attrName + " in definition " + def.getName());
				String impSuperName = parts[0];  // The super of the implied
				String impSuperAttr = parts[1];  // The attribute of the super of the implied we override
				String impName = def.getName() + "/" + impSuperName;
				customAttrs.add(attrName);

				// Create the implied definition
				XmlDefinition impDef = new XmlDefinition();
				impDef.setName(impName);
				impDef.setExtends(impSuperName);
				impDef.putAttribute(impSuperAttr, attrs.get(attrName));
				impliedMap.put(impName, impDef);
				
				// We now need to override the tile from our super
				// that was pointing to the nested super with the new implied super.
				// We need to search which of our super attributes
				// was pointing to the impliedSuper. We will need to step up the
				// hierarchy till we find one
				String impliedAttrName = null;
				XmlDefinition superDef;

				// Start by searching ourselves
				superDef = def;
				while (superDef != null && impliedAttrName == null) {
					// Search for attributes with this value
					Map superAttrs = superDef.getAttributes();
					for (Object superAttrKey : superAttrs.keySet()) {
						String superAttrVal = (String) superAttrs.get(superAttrKey);
						if (superAttrVal == null) continue;
						if (superAttrVal.equals(impSuperName)) {
							impliedAttrName = (String) superAttrKey; // found it
							break;
						}
					}
					superDef = superDef.getExtends() == null
							 ? null
							 : definitions.getDefinition(superDef.getExtends()); 
				}
				// Store on the map (avoid altering attributes while looping in them
				if (impliedAttrName == null)
					throw new DefinitionsFactoryException("Failed to find attribute " + impSuperName + " in " + def.getName() + " and all it's parents");
				
				impliedAttrs.put(impliedAttrName, def.getName() + "/" + impSuperName);
			} // for each attribute
			
			// Add the implied attribute and remove the custom syntax one
			for (Object key : impliedAttrs.keySet())	attrs.put(key, impliedAttrs.get(key));
			for (String key : customAttrs)				attrs.remove(key);
			
		} // for each definition
		
		// We should have by now the implied definitions, add them
		for (XmlDefinition impDef : impliedMap.values()) {
			definitions.putDefinition(impDef);			
		}
	}
	
	
	/**
	 * <p>Processes the side file called the same like tiles config but with an added extension .properties</p>
	 * <p>This is a completely non elegant but convenient way to initialise your variables</p>
	 * 
	 * <p>This is still a test implementation and I would not like to extend the parser yet
	 * because I would like to find a better way of modifying easily tiles values
	 * and the variable approach may change in the future, if a simple way to access nested tiles
	 * can be done</p>
	 * 
	 * @param filename
	 * @throws IOException 
	 */
	protected static Map<String, String> readDefaultVariables(String filename) throws IOException {
		String propsFilename = replaceIgnoreCase("\\.xml$", ".default.properties", filename);
		File propsFile = new File(propsFilename);
		if(!propsFile.exists()) {
			throw new  IOException("Tiles default variable file does not exist, starting with uninitialised values. File should be: " + propsFilename);
		}
		
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(propsFile);
		props.load(fis);
		
		Map<String, String> vars = new HashMap<String, String>();
		for (Object propKey : props.keySet())
			vars.put((String)propKey, props.getProperty((String)propKey));

		log.info("Found and parsed default variable file at " + propsFilename);
		if (log.isTraceEnabled())
			props.list(System.out);
		
		return vars;
	}
	
	
	/**
	 * Does a case insensitive replace based on a pattern
	 * @param regex
	 * @param subject
	 */
	public static String replaceIgnoreCase(String regex, String replacement, String subject) {
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(subject);
		return m.replaceAll(replacement);
	}

	
	/**
	 * Wrapper to dump that hides exceptions
	 * 
	 * @param definitions
	 * @param origDefsFile
	 */
	protected static void dump(XmlDefinitionsSet definitions, String origDefsFile) {
		String interFile = replaceIgnoreCase("\\.xml$", ".effective.xml", origDefsFile);
		try {
			OutputStream out = new FileOutputStream(interFile);
			dump(out, definitions);
			log.debug("Effective tiles file has been dumped to: " + interFile);
		} catch (IOException e) {
			log.warn("Could not write the intermediate configuration file at " + interFile + ". Reason: " + e);
		}
		
		if(log.isTraceEnabled())
			try {
				dump(System.out, definitions);
			} catch (IOException e) {
				log.warn("Could not print the intermediate configurations to stdout " + interFile + ". Reason: " + e);
			}
	}

	
	/**
	 * This writes the definitions to a file or the stdout
	 * 
	 * @param out
	 * @param definitions
	 * @throws IOException
	 */
	protected static void dump(OutputStream out, XmlDefinitionsSet definitions) throws IOException {
		PrintWriter pw = new PrintWriter(out);
		
		pw.println(XMLHEADER);
		pw.println("<tiles-definitions>");
		Collection<XmlDefinition> defs = (Collection<XmlDefinition>) definitions.getDefinitions().values();
		try {
			for (XmlDefinition def : defs) {
				// Print the definition properties
				pw.print("    <definition ");
				for (String propName : DEFPROPS) {
					Object propVal = PropertyUtils.getProperty(def, propName);
					if (propVal == null)
						continue;
					pw.print(propName + "=\"" + propVal + "\" ");
				}
				pw.println(">");
				// Now print the definition attributes
				Map attrs = def.getAttributes();
				for (Object attrKey : attrs.keySet()) {
					pw.println("       <put name=\"" + attrKey + "\" value=\"" + attrs.get(attrKey) + "\"/>");
				}
				pw.println("    </definition>");
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.println("</tiles-definitions>");
		pw.flush();
		pw.close();
	}
	
	//
	// Accessors
	//
	
	public static void setOriginalDefinitionsFactory(DefinitionsFactory origFactory, String modulePrefix, ServletContext servletContext) {
		servletContext.setAttribute(ORIG_DEFS_FACTORY_KEY + modulePrefix, origFactory);
	}
	
	public static DefinitionsFactory getOriginalDefinitionsFactory(String modulePrefix, ServletContext servletContext) {
		return (DefinitionsFactory) servletContext.getAttribute(ORIG_DEFS_FACTORY_KEY + modulePrefix);
	}

	public static void removeOriginalDefinitionsFactory(String modulePrefix, ServletContext servletContext) {
		servletContext.removeAttribute(ORIG_DEFS_FACTORY_KEY + modulePrefix);
	}
	

	/**
	 * For testing
	 */
	public static void main(String[] args) {
		String filename = "WebContent/WEB-INF/tiles-config-test.xml";
		String outfile = "WebContent/WEB-INF/tiles-config-test.effective.xml";
		
		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(filename));
		} catch (IOException ex) {
			System.out.println("can't open file '" + filename + "' : "
					+ ex.getMessage());
			System.exit(-1);
		}

		// Parse the file
		XmlDefinitionsSet definitions = null;
		try {
			XmlParser parser = new XmlParser();
			parser.setValidating(true);
			definitions = new XmlDefinitionsSet();
			System.out.println("  Parse file");
			parser.parse(input, definitions);
			System.out.println("  done.");
		} catch (Exception ex) {
			System.out.println("Error during parsing '" + filename + "' : "
					+ ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}

		// Create the modified definitions
		try {
			process(definitions);
		} catch (TilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Generate the parsed modified xml file
		try {
			dump(new FileOutputStream(outfile), definitions);
			dump(System.out, definitions);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
