package org.jboss.jdf.plugins.stacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

/**
 * This is a Utility class that handle the JDF BOMs
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class StacksUtil {

	private static final String PROP_STACKS_REPO = "STACKS_REPO";
	private static final String DEFAULT_STACK_REPO = "https://raw.github.com/rafabene/jdf-plugin/master/stacks.yaml";

	@Inject
	private Shell shell;

	public void retrieveAvailableStacks() throws Exception {
		String stacksRepo = getStacksRepo();
		InputStream repoStream = getCachedRepoStream(stacksRepo);
		if (repoStream == null) {
			repoStream = retrieveStacksFromRemoteRepository(stacksRepo);
			setCachedRepoStream(stacksRepo, repoStream);
			repoStream = getCachedRepoStream(stacksRepo);
		}
		List<Stack> stacks = populaStacksFromStream(repoStream);
		shell.println(stacks.toString());

	}
	
	public static void main(String[] args) throws FileNotFoundException {
		File f = new File("/home/rafael/.forge/httpsrawgithubcomrafabenejdfpluginmasterstacksyaml.yaml");
		FileInputStream fis = new FileInputStream(f);
		StacksUtil s = new StacksUtil();
		s.populaStacksFromStream(fis);
	}

	@SuppressWarnings({ "unchecked" })
	private List<Stack> populaStacksFromStream(InputStream stream) {
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
//			stack.setId((String) map.get(Stack.PROP_ID));
//			stack.setName((String) map.get(Stack.PROP_NAME));
//			stack.setDescription((String) map.get(Stack.PROP_DESCRIPTION));
//			stack.setArtifact((String) map.get(Stack.PROP_ARTIFACT));
//			stack.getVersions().addAll((List<String>) map.get(Stack.PROP_VERSIONS));
			stacksList.add(stack);
		}
		return stacksList;
	}

	private InputStream retrieveStacksFromRemoteRepository(String stacksRepo) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(stacksRepo);

		DefaultHttpClient client = new DefaultHttpClient();
		// Available in Forge 1.0.6 - configureProxy(ProxySettings.fromForgeConfiguration(configuration), client);
		HttpResponse httpResponse = client.execute(httpGet);

		switch (httpResponse.getStatusLine().getStatusCode()) {
		case 200:
			shell.println("connected!");
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

	@SuppressWarnings("unchecked")
	private InputStream getCachedRepoStream(String repo) {
		FileResource<?> cachedRepo = shell.getEnvironment().getConfigDirectory().getChildOfType(FileResource.class, repo.replaceAll("[^a-zA-Z0-9]+", "") + ".yaml");
		if (cachedRepo.exists()) {
			long lastModified = cachedRepo.getUnderlyingResourceObject().lastModified();
			if (System.currentTimeMillis() - lastModified <= 60000) {
				return cachedRepo.getResourceInputStream();
			} else {
				cachedRepo.delete();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void setCachedRepoStream(String repo, InputStream stream) {
		FileResource<?> cachedRepo = shell.getEnvironment().getConfigDirectory().getChildOfType(FileResource.class, repo.replaceAll("[^a-zA-Z0-9]+", "") + ".yaml");
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

	/*
	 * Will be available in Forge 1.0.6 private static void configureProxy(final ProxySettings proxySettings, final DefaultHttpClient client) { if (proxySettings != null) {
	 * HttpHost proxy = new HttpHost(proxySettings.getProxyHost(), proxySettings.getProxyPort()); client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	 * 
	 * if (proxySettings.isAuthenticationSupported()) { AuthScope authScope = new AuthScope(proxySettings.getProxyHost(), proxySettings.getProxyPort()); UsernamePasswordCredentials
	 * credentials = new UsernamePasswordCredentials(proxySettings.getProxyUserName(), proxySettings.getProxyPassword()); client.getCredentialsProvider().setCredentials(authScope,
	 * credentials); } } }
	 */

}
