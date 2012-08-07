/*
 * JBoss, Home of Professional Open Source
 * Copyright <Year>, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.jdf.plugins.stacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.jboss.forge.ForgeEnvironment;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.jdf.plugins.stacks.Parser.Bom;
import org.jboss.jdf.plugins.stacks.Parser.BomVersion;

/**
 * This is a Utility class that handle the available JDF Stacks from a repository using YAML
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class StacksUtil {

    private static final String BRANCH = "$BRANCH$";
    private static final String JDF_ELEMENT = "jdf";
    private static final String STACKSREPO_ELEMENT = "stacksRepo";
    private static final String STACK_BRANCH = "Beta4";
    // private static final String STACK_BRANCH = "master";
    private static final String DEFAULT_STACK_REPO = "https://raw.github.com/jboss-jdf/jdf-stack/" + BRANCH + "/stacks.yaml";

    @Inject
    private Shell shell;

    @Inject
    private Configuration configuration;

    @Inject
    private ForgeEnvironment environment;

    @Inject
    private Parser parser;

    /**
     * This method verifies all available Stacks in repository
     * 
     * @return Available Stacks
     */
    @Produces
    public List<Bom> retrieveAvailableBoms() {
        InputStream inputStream = getStacksInputStream();
        List<Bom> boms = new ArrayList<Parser.Bom>();
        if (inputStream != null) {
            boms.addAll(parser.parse(inputStream).getAvailableBoms());
        }
        return boms;
    }

    /**
     * 
     * Retrieve all bom versions
     * 
     * @param bom
     * @return
     */
    public List<BomVersion> getAllVersions(Bom bom) {
        InputStream inputStream = getStacksInputStream();
        List<BomVersion> bomVersions = new ArrayList<BomVersion>();
        if (inputStream != null) {
            List<BomVersion> allVersions = parser.parse(inputStream).getAvailableBomVersions();
            for(BomVersion bomVersion: allVersions){
                //if version corresponds to the groupId and artifactId of the bom parameter
                if (bomVersion.getBom().getGroupId().equals(bom.getGroupId()) && bomVersion.getBom().getArtifactId().equals(bom.getArtifactId())){
                    bomVersions.add(bomVersion);
                }
            }
        }
        return bomVersions;
    }

    private InputStream getStacksInputStream() {
        String stacksRepo = getStacksRepo();
        InputStream repoStream = getCachedRepoStream(stacksRepo, environment.isOnline());
        // if cache expired
        if (repoStream == null) {
            try {
                showVerboseMessage("Retrieving Stacks from Remote repository");
                repoStream = retrieveStacksFromRemoteRepository(stacksRepo);
                setCachedRepoStream(stacksRepo, repoStream);
                repoStream = getCachedRepoStream(stacksRepo, true);
            } catch (Exception e) {
                if (shell.isVerbose()) {
                    e.printStackTrace();
                }
                shell.println();
                ShellMessages.warn(shell, "It was not possible to contact the repository at " + stacksRepo);
                ShellMessages.warn(shell, "Falling back to cache!");
                repoStream = getCachedRepoStream(stacksRepo, false);
            }
        }
        // If the Repostream stills empty after falling back to cache
        if (repoStream == null) {
            ShellMessages.error(shell, "The Cache is empty. Try going online to get the list of available JDF Stacks!");
            return null;
        } else {
            return repoStream;
        }
    }

    public List<BomVersion> getAvailableVersions(Bom bom) {
        return null;
    }

    private void showVerboseMessage(String message) {
        if (shell.isVerbose()) {
            shell.println();
            shell.println(message);
        }
    }

    private InputStream retrieveStacksFromRemoteRepository(final String stacksRepo) throws ClientProtocolException,
            IOException, URISyntaxException {
        if (stacksRepo.startsWith("http")) {
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
                    ShellMessages.error(shell, "Failed! (Stacks file not found: " + stacksRepo + ")");
                    return null;

                default:
                    ShellMessages.error(shell, "Failed! (server returned status code: "
                            + httpResponse.getStatusLine().getStatusCode());
                    return null;
            }
            return httpResponse.getEntity().getContent();
        } else if (stacksRepo.startsWith("file")) {
            return new FileInputStream(new File(URI.create(stacksRepo)));
        }
        return null;
    }

    private InputStream getCachedRepoStream(final String repo, final boolean online) {
        FileResource<?> cachedRepo = getCacheFileResource();
        if (cachedRepo.exists()) {
            long lastModified = cachedRepo.getUnderlyingResourceObject().lastModified();
            // if online, consider the cache valid until it expires after 24 hours
            if (!online || System.currentTimeMillis() - lastModified <= (1000 * 60 * 60 * 24)) {
                return cachedRepo.getResourceInputStream();
            }
        }
        return null;
    }

    private void setCachedRepoStream(final String repo, final InputStream stream) {
        FileResource<?> cachedRepo = getCacheFileResource();
        if (!cachedRepo.exists()) {
            cachedRepo.createNewFile();
        }
        cachedRepo.setContents(stream);
    }

    public String getStacksRepo() {
        Configuration userConfig = configuration.getScopedConfiguration(ConfigurationScope.USER);
        verifyIfNeedConfigurationUpdate(userConfig);
        Configuration jdfConfig = userConfig.subset(JDF_ELEMENT);
        String stacksRepo = jdfConfig.getString(STACKSREPO_ELEMENT);
        if (stacksRepo == null) {
            String jdfStackRepo = DEFAULT_STACK_REPO.replaceAll(BRANCH, STACK_BRANCH);
            userConfig.setProperty(JDF_ELEMENT + "." + STACKSREPO_ELEMENT, jdfStackRepo);
            return jdfStackRepo;
        } else {
            return jdfConfig.getString(STACKSREPO_ELEMENT);
        }
    }

    /**
     * Verifies if the config file need to be updated. The update procedure will proceed if the version in config file points to
     * a official repository and it is changed in this release
     * 
     * @param userConfig
     */
    private void verifyIfNeedConfigurationUpdate(Configuration userConfig) {
        Configuration jdfConfig = userConfig.subset(JDF_ELEMENT);
        String defaultRepoPrefix = DEFAULT_STACK_REPO.substring(0, DEFAULT_STACK_REPO.lastIndexOf(BRANCH));
        String configRepo = jdfConfig.getString(STACKSREPO_ELEMENT);
        String releaseRepo = DEFAULT_STACK_REPO.replace(BRANCH, STACK_BRANCH);
        // If config uses the official repo and it is changed in this release
        if (configRepo.startsWith(defaultRepoPrefix) && !configRepo.equals(releaseRepo)) {
            // silently update the configuration
            userConfig.setProperty(JDF_ELEMENT + "." + STACKSREPO_ELEMENT, releaseRepo);
            // silently reset the cache
            eraseRepositoryCache();
            retrieveAvailableBoms();
        }

    }

    @SuppressWarnings("unchecked")
    private FileResource<?> getCacheFileResource() {
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

    public void eraseRepositoryCache() {
        FileResource<?> repositoryCache = getCacheFileResource();
        repositoryCache.delete();

    }

}
