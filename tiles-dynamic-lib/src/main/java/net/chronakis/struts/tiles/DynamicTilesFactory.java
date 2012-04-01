package net.chronakis.struts.tiles;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryConfig;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.NoSuchDefinitionException;
import org.apache.struts.tiles.definition.ComponentDefinitionsFactoryWrapper;

/**
 * <p>This is a wrapper class for the DynamicTilesFactoryActual<br/>
 * Documentation is available at {@link DynamicTilesFactoryActual}</p>
 * 
 * <p>The reason for this wrapper is to cope with the changes in the tiles interface.<br/>
 * The actual factory is a subclass of the tiles I18FactorySet which implement the old interface
 * so we need this wrapper to be able to make the actual factory module aware</p>
 *  
 * @author Ioannis Chronakis
 *
 */
public class DynamicTilesFactory implements DefinitionsFactory {
	/**
	 * The underlying factory
	 */
	DynamicTilesFactoryActual factory;

    /**
     * Factory configuration,
     */
    private DefinitionsFactoryConfig config = null;

	@Override
	public void init(DefinitionsFactoryConfig config, ServletContext servletContext) throws DefinitionsFactoryException {
        this.config = config;

        // create factory and initialize it
        if (factory == null) {
            factory = new DynamicTilesFactoryActual();
        }

        // The sole reason for this class is to be able to set the module prefix for the vars and the original definitins factory
        factory.setConfig(config);
        
        factory.initFactory(servletContext, ComponentDefinitionsFactoryWrapper.createConfigMap(config));
	}

    @Override
	public void destroy() {
    	factory = null;
    	config = null;
    }

	@Override
	public ComponentDefinition getDefinition(String name, ServletRequest request, ServletContext servletContext) throws NoSuchDefinitionException, DefinitionsFactoryException {
		return factory.getDefinition(name, request, servletContext);
	}

	@Override
	public void setConfig(DefinitionsFactoryConfig config, ServletContext servletContext) throws DefinitionsFactoryException {
		this.config = config;
	}

	@Override
	public DefinitionsFactoryConfig getConfig() {
		return config;
	}

}
