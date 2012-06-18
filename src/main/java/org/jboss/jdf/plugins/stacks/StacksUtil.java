package org.jboss.jdf.plugins.stacks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

/**
 * This is a Utility class that handle the available JDF Stacks from a repository using YAML
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class StacksUtil {

	private static final String PROP_STACKS_REPO = "STACKS_REPO";
	private static final String DEFAULT_STACK_REPO = "https://raw.github.com/rafabene/jdf-plugin/master/stacks.yaml";

	@Inject
	private Shell shell;

	/**
	 * This method verifies all available Stacks in repository
	 * 
	 * @return Available Stacks
	 */
	@Produces @ApplicationScoped
	public List<Stack> retrieveAvailableStacks() {
		String stacksRepo = getStacksRepo();
		InputStream repoStream = getCachedRepoStream(stacksRepo, true);
		// if cache expired
		if (repoStream == null) {
			try {
				repoStream = retrieveStacksFromRemoteRepository(stacksRepo);
				setCachedRepoStream(stacksRepo, repoStream);
			} catch (Exception e) {
				// Maybe we're offline
				shell.println();
				shell.println(ShellColor.YELLOW, "It was not possible to contact the repository at " + DEFAULT_STACK_REPO);
				shell.println(ShellColor.YELLOW, "Falling back to cache!");
				repoStream = getCachedRepoStream(stacksRepo, false);
			}
		}
		// If the Repostream stills empty after falling back to cache
		if (repoStream == null) {
			shell.println(ShellColor.RED, "The Cache is empty. Try going online to get the list of available JDF Stacks!");
			return null;
		}
		List<Stack> stacks = populateStacksFromStream(repoStream);
		return stacks;

	}

	/**
	 * Parses the Stream and converts YAML content to a Javabean
	 * 
	 * @param stream
	 * @return the converted list of stacks
	 */
	private List<Stack> populateStacksFromStream(InputStream stream) {
		List<Stack> stacksList = new ArrayList<Stack>();

		Constructor constructor = new CustomClassLoaderConstructor(Stack.class, this.getClass().getClassLoader());
		TypeDescription stackDescription = new TypeDescription(Stack.class);
		stackDescription.putListPropertyType("versions", String.class);
		constructor.addTypeDescription(stackDescription);
		Yaml yaml = new Yaml(constructor);

		for (Object o : yaml.loadAll(stream)) {
			if (o == null) {
				continue;
			}

			Stack stack = (Stack) o;
			stacksList.add(stack);
		}
		return stacksList;
	}

	private InputStream retrieveStacksFromRemoteRepository(String stacksRepo) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(stacksRepo);
		DefaultHttpClient client = new DefaultHttpClient();
		//TODO Available in Forge 1.0.6 - configureProxy(ProxySettings.fromForgeConfiguration(configuration), client);
		HttpResponse httpResponse = client.execute(httpGet);
		shell.println();
		switch (httpResponse.getStatusLine().getStatusCode()) {
		case 200:
			shell.println("connected to repository!");
			break;

		case 404:
			shell.println("failed! (plugin index not found: " + stacksRepo + ")");
			return null;

		default:
			shell.println("failed! (server returned status code: " + httpResponse.getStatusLine().getStatusCode());
			return null;
		}
		return httpResponse.getEntity().getContent();
	}

	private InputStream getCachedRepoStream(String repo, boolean online) {
		FileResource<?> cachedRepo = getCacheFileResource(repo);
		if (cachedRepo.exists()) {
			long lastModified = cachedRepo.getUnderlyingResourceObject().lastModified();
			//if online, consider the cache valid until it expires after 1 minute
			if (!online || System.currentTimeMillis() - lastModified <= (1000 * 60)) {
				return cachedRepo.getResourceInputStream();
			}
		}
		return null;
	}

	private void setCachedRepoStream(String repo, InputStream stream) {
		FileResource<?> cachedRepo = getCacheFileResource(repo);
		if (!cachedRepo.exists()) {
			cachedRepo.createNewFile();
		}
		cachedRepo.setContents(stream);
	}

	private String getStacksRepo() {
		String stacksRepo = (String) shell.getEnvironment().getProperty(PROP_STACKS_REPO);
		if (stacksRepo == null) {
			stacksRepo = DEFAULT_STACK_REPO;
		}
		return stacksRepo;
	}

	@SuppressWarnings("unchecked")
	private FileResource<?> getCacheFileResource(String repo) {
		return shell.getEnvironment().getConfigDirectory().getChildOfType(FileResource.class, "stacks.yaml");
	}

	/*
	 * Will be available in Forge 1.0.6 private static void configureProxy(final ProxySettings proxySettings, final DefaultHttpClient client) { if (proxySettings != null) {
	 * HttpHost proxy = new HttpHost(proxySettings.getProxyHost(), proxySettings.getProxyPort()); client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	 * 
	 * if (proxySettings.isAuthenticationSupported()) { AuthScope authScope = new AuthScope(proxySettings.getProxyHost(), proxySettings.getProxyPort()); UsernamePasswordCredentials
	 * credentials = new UsernamePasswordCredentials(proxySettings.getProxyUserName(), proxySettings.getProxyPassword()); client.getCredentialsProvider().setCredentials(authScope,
	 * credentials); } } }
	 */

}
