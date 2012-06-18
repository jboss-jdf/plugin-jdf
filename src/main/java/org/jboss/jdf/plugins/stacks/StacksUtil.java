package org.jboss.jdf.plugins.stacks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
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

	@Inject
	private Configuration configuration;

	/**
	 * This method verifies all available Stacks in repository
	 * 
	 * @return Available Stacks
	 */
	@Produces
	public List<Stack> retrieveAvailableStacks() {
		String stacksRepo = getStacksRepo();
		InputStream repoStream = getCachedRepoStream(stacksRepo, true);
		// if cache expired
		if (repoStream == null) {
			try {
				showVerboseMessage("Retrieving Stacks from Remote repository");
				repoStream = retrieveStacksFromRemoteRepository(stacksRepo);
				setCachedRepoStream(stacksRepo, repoStream);
				repoStream = getCachedRepoStream(stacksRepo, true);
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

	private void showVerboseMessage(String message) {
		if (shell.isVerbose()) {
			shell.println();
			shell.println(message);
		}
	}

	/**
	 * Parses the Stream and converts YAML content to a Javabean
	 * 
	 * @param stream
	 * @return the converted list of stacks
	 */
	private List<Stack> populateStacksFromStream(final InputStream stream) {
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

	private InputStream retrieveStacksFromRemoteRepository(final String stacksRepo) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(stacksRepo);
		DefaultHttpClient client = new DefaultHttpClient();
		configureProxy(client);
		HttpResponse httpResponse = client.execute(httpGet);
		shell.println();
		switch (httpResponse.getStatusLine().getStatusCode()) {
		case 200:
			showVerboseMessage("Connected to repository! Getting available Stacks");
			break;

		case 404:
			shell.println("Failed! (Stacks file not found: " + stacksRepo + ")");
			return null;

		default:
			shell.println("Failed! (server returned status code: " + httpResponse.getStatusLine().getStatusCode());
			return null;
		}
		return httpResponse.getEntity().getContent();
	}

	private InputStream getCachedRepoStream(final String repo, final boolean online) {
		FileResource<?> cachedRepo = getCacheFileResource(repo);
		if (cachedRepo.exists()) {
			long lastModified = cachedRepo.getUnderlyingResourceObject().lastModified();
			// if online, consider the cache valid until it expires after 1 minute
			if (!online || System.currentTimeMillis() - lastModified <= (1000 * 10)) {
				return cachedRepo.getResourceInputStream();
			}
		}
		return null;
	}

	private void setCachedRepoStream(final String repo, final InputStream stream) {
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
	private FileResource<?> getCacheFileResource(final String repo) {
		return shell.getEnvironment().getConfigDirectory().getChildOfType(FileResource.class, "stacks.yaml");
	}

	private void configureProxy(final DefaultHttpClient client) {
		Configuration proxyConfig = configuration.getScopedConfiguration(ConfigurationScope.USER).subset("proxy");
		if (proxyConfig != null && !proxyConfig.isEmpty()) {
			String proxyHost = proxyConfig.getString("host");
			int proxyPort = proxyConfig.getInt("port");
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			String proxyUsername = proxyConfig.getString("username");
			if (proxyUsername != null && !proxyUsername.equals("")) {
				String proxyPassword = proxyConfig.getString("password");
				AuthScope authScope = new AuthScope(proxyHost, proxyPort);
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
				client.getCredentialsProvider().setCredentials(authScope, credentials);
			}
		}
	}

}
