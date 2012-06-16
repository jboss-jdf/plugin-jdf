package org.jboss.jdf.plugins.providers;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Represents the list of various JDF Stacks
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 *
 */
public enum JDFStackProvider {
	
	JBOSS_JAVAEE6_WITH_ERRAI(JDFEE6ErraiBOMProvider.class),
	JBOSS_JAVAEE6_WITH_HIBERNATE(JDFEE6HibernateBOMProvider.class),
	JBOSS_JAVAEE6_WITH_TOOLS(JDFEE6ToolsBOMProvider.class),
	JBOSS_JAVAEE6_WITH_TRANSACTIONS(JDFEE6TransactionsBOMProvider.class);
	
	private Class<? extends JDFBOMProvider> provider;

	private JDFStackProvider(Class<? extends JDFBOMProvider> provider){
		this.provider = provider;
	}
	
	/*
	 * The class org.jboss.forge.shell.util.BeanManagerUtils was not used
	 * because Lincoln warned to *** NOT USE THAT CLASS*** 
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JDFBOMProvider getProvider(final BeanManager beanManager){
		Bean<?> bean = beanManager.resolve(beanManager.getBeans(provider));
		if (bean != null){
			CreationalContext creationalContext = (CreationalContext) beanManager.createCreationalContext(bean);
			if (creationalContext != null){
				JDFBOMProvider instance =  (JDFBOMProvider) bean.create(creationalContext);
				return instance;
			}
		}
		throw new TypeNotPresentException("Can't find the class of type " + provider.getClass(), null);
	}

}
