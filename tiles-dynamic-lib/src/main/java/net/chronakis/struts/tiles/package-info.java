/**
 * Dynamic tiles is an extension to the struts tiles 1.x library
 * (<a href="http://struts.apache.org/1.x/struts-tiles/index.html">http://struts.apache.org/1.x/struts-tiles/index.html</a>)
 * that provides two new features:
 * <p>
 * <ol>
 *   <li>Ant style ${varName} variable substitution for the path property and all the attribute values at runtime
 *   <li>Much less chatty syntax when for nested tiles, making inheritance look good
 * </ol>
 * <p>
 * The former is a very simple concept to explain, however the latter is much more complicated. The best way to understand it,
 * is to have a look at the example web application that comes as a separate war file but can be found either
 * at the github page of the project, or from maven repository.
 * <p>
 * 
 * <h2>Ant style variable substitution during run time</h2>
 * 
 * Tiles configuration is loaded when the web application starts and
 * remains the same until the developer changes the xml file. One has to
 * have configured all the permutations of the site before starting the server.
 * <p>
 * The dynamic tiles adds the feature to use ant style variables, e.g. ${headerPage}
 * instead of predefined pages in the configuration xml file and then at any point during
 * servlet initialisation, session initialisation or at http request to provide the actual
 * value of this variable. And this is done by passing a plain {@link java.util.Map}
 * with the variable name as key and the actual path as value.
 * <p>
 * To enable this feature, you just need to add the {@link net.chronakis.struts.tiles.DynamicTilesPreprocessor}
 * in the change-config.xml file. Just replace the default org.apache.struts.tiles.commands.TilesPreProcessor
 * with the new one net.chronakis.struts.tiles.DynamicTilesPreprocessor.
 * <p>
 * Then you need to add actual variables in the tiles configuration file. Have a look a the example site
 * for ideas. Independent of how many variables you are going to use, you should always provide some defaults
 * at servlet initialisation, by using the {@link net.chronakis.struts.tiles.DynamicTilesUtils#setVariables(Map, String, javax.servlet.ServletContext)}
 * A good place is inside the struts plugin.<p>
 * Then, depending on what you want to do, you can set more variables with session scope using 
 * the {@link net.chronakis.struts.tiles.DynamicTilesUtils#setVariables(Map, String, javax.servlet.http.HttpSession)} method
 * and refine even further per request (e.g. from within a struts action) using the
 * {@link net.chronakis.struts.tiles.DynamicTilesUtils#setVariables(Map, String, ServletRequest)} method.
 * <p>
 * What is important is that the more specific scope overrides the less specific. So you can override
 * the servlet defined ones with those form the user's session and the session with those from the particular
 * request.
 * 
 * <h2>Special compact syntax for inheriting nested tiles</h2>
 * 
 * This is an experimental feature. that despite the fact it worked well it is left a bit unfinished:
 * It only supports one nested level, it has never been generalised. The reason is that it felt
 * like stretching something far beyond the original specifications, so the work left unfinished.
 * <p>
 * For details on how this work, please have a look a the comment on the
 * {@link net.chronakis.struts.tiles.DynamicTilesFactoryActual}
 * also at the tiles-dynamic-example project /WEB-INF/tiles-config.xml for an example
 * 
 * @author Ioannis Chronakis
 */
package net.chronakis.struts.tiles;

import java.util.Map;
import javax.servlet.ServletRequest;

