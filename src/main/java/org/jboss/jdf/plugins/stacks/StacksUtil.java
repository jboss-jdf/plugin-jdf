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
public class StacksUtil
{

   public static final String DEFAULT_STACK_REPO = "https://raw.github.com/jboss-jdf/jdf-stack/master/stacks.yaml";
   private static final String JDF_ELEMENT = "jdf";
   private static final String STACKSREPO_ELEMENT = "stacksRepo";

   @Inject
   private Shell shell;

   @Inject
   private Configuration configuration;

   @Inject
   private ForgeEnvironment environment;

   /**
    * This method verifies all available Stacks in repository
    * 
    * @return Available Stacks
    */
   @Produces
   public List<Stack> retrieveAvailableStacks()
   {
      String stacksRepo = getStacksRepo();
      InputStream repoStream = getCachedRepoStream(stacksRepo, environment.isOnline());
      // if cache expired
      if (repoStream == null)
      {
         try
         {
            showVerboseMessage("Retrieving Stacks from Remote repository");
            repoStream = retrieveStacksFromRemoteRepository(stacksRepo);
            setCachedRepoStream(stacksRepo, repoStream);
            repoStream = getCachedRepoStream(stacksRepo, true);
         }
         catch (Exception e)
         {
            if (shell.isVerbose())
            {
               e.printStackTrace();
            }
            shell.println();
            ShellMessages.warn(shell, "It was not possible to contact the repository at " + stacksRepo);
            ShellMessages.warn(shell, "Falling back to cache!");
            repoStream = getCachedRepoStream(stacksRepo, false);
         }
      }
      // If the Repostream stills empty after falling back to cache
      if (repoStream == null)
      {
         ShellMessages.error(shell, "The Cache is empty. Try going online to get the list of available JDF Stacks!");
         return null;
      }
      List<Stack> stacks = populateStacksFromStream(repoStream);
      return stacks;

   }

   private void showVerboseMessage(String message)
   {
      if (shell.isVerbose())
      {
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
   private List<Stack> populateStacksFromStream(final InputStream stream)
   {
      List<Stack> stacksList = new ArrayList<Stack>();

      Constructor constructor = new CustomClassLoaderConstructor(Stack.class, this.getClass().getClassLoader());
      TypeDescription stackDescription = new TypeDescription(Stack.class);
      stackDescription.putListPropertyType("availableVersions", String.class);
      constructor.addTypeDescription(stackDescription);
      Yaml yaml = new Yaml(constructor);

      for (Object o : yaml.loadAll(stream))
      {
         if (o == null)
         {
            continue;
         }

         Stack stack = (Stack) o;
         if (!stack.getAvailableVersions().contains(stack.getRecommendedVersion()))
         {
            throw new RuntimeException("Invalid repository information: The recommended version ["
                     + stack.getRecommendedVersion() + " for stack [" + stack
                     + "] is not one of the available versions");
         }
         stacksList.add(stack);
      }
      return stacksList;
   }

   private InputStream retrieveStacksFromRemoteRepository(final String stacksRepo) throws ClientProtocolException,
            IOException, URISyntaxException
   {
      if (stacksRepo.startsWith("http"))
      {
         HttpGet httpGet = new HttpGet(stacksRepo);
         DefaultHttpClient client = new DefaultHttpClient();
         configureProxy(client);
         HttpResponse httpResponse = client.execute(httpGet);
         shell.println();
         switch (httpResponse.getStatusLine().getStatusCode())
         {
         case 200:
            showVerboseMessage("Connected to repository! Getting available Stacks");
            break;

         case 404:
            ShellMessages.error(shell, "Failed! (Stacks file not found: " + stacksRepo + ")");
            return null;

         default:
            ShellMessages.error(shell,"Failed! (server returned status code: " + httpResponse.getStatusLine().getStatusCode());
            return null;
         }
         return httpResponse.getEntity().getContent();
      }
      else if (stacksRepo.startsWith("file"))
      {
         return new FileInputStream(new File(URI.create(stacksRepo)));
      }
      return null;
   }

   private InputStream getCachedRepoStream(final String repo, final boolean online)
   {
      FileResource<?> cachedRepo = getCacheFileResource(repo);
      if (cachedRepo.exists())
      {
         long lastModified = cachedRepo.getUnderlyingResourceObject().lastModified();
         // if online, consider the cache valid until it expires after 1 minute
         if (!online || System.currentTimeMillis() - lastModified <= (1000 * 10))
         {
            return cachedRepo.getResourceInputStream();
         }
      }
      return null;
   }

   private void setCachedRepoStream(final String repo, final InputStream stream)
   {
      FileResource<?> cachedRepo = getCacheFileResource(repo);
      if (!cachedRepo.exists())
      {
         cachedRepo.createNewFile();
      }
      cachedRepo.setContents(stream);
   }

   public String getStacksRepo()
   {
      Configuration userConfig = configuration.getScopedConfiguration(ConfigurationScope.USER);
      Configuration jdfConfig = userConfig.subset(JDF_ELEMENT);
      String stacksRepo = jdfConfig.getString(STACKSREPO_ELEMENT);
      if (stacksRepo == null)
      {
         userConfig.setProperty(JDF_ELEMENT + "." + STACKSREPO_ELEMENT, DEFAULT_STACK_REPO);
         return DEFAULT_STACK_REPO;
      }
      else
      {
         return jdfConfig.getString("stacksRepo");
      }
   }

   @SuppressWarnings("unchecked")
   private FileResource<?> getCacheFileResource(final String repo)
   {
      return shell.getEnvironment().getConfigDirectory().getChildOfType(FileResource.class, "stacks.yaml");
   }

   private void configureProxy(final DefaultHttpClient client)
   {
      Configuration proxyConfig = configuration.getScopedConfiguration(ConfigurationScope.USER).subset("proxy");
      if (proxyConfig != null && !proxyConfig.isEmpty())
      {
         String proxyHost = proxyConfig.getString("host");
         int proxyPort = proxyConfig.getInt("port");
         HttpHost proxy = new HttpHost(proxyHost, proxyPort);
         client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
         String proxyUsername = proxyConfig.getString("username");
         if (proxyUsername != null && !proxyUsername.equals(""))
         {
            String proxyPassword = proxyConfig.getString("password");
            AuthScope authScope = new AuthScope(proxyHost, proxyPort);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
            client.getCredentialsProvider().setCredentials(authScope, credentials);
         }
      }
   }

}
