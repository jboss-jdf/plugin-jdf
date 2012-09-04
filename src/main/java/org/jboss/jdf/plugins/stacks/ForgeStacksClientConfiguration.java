/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jdf.plugins.stacks;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.jboss.forge.ForgeEnvironment;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.jdf.stacks.client.StacksClientConfiguration;

/**
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class ForgeStacksClientConfiguration implements StacksClientConfiguration {

    private static final String JDF_ELEMENT = "jdf";
    private static final String STACKSREPO_ELEMENT = "stacksRepo";

    @Inject
    private Configuration configuration;

    @Inject
    private ForgeEnvironment environment;

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#getUrl()
     */
    @Override
    public URL getUrl() {
        Configuration userConfig = configuration.getScopedConfiguration(ConfigurationScope.USER);
        verifyIfNeedConfigurationReset(userConfig);
        Configuration jdfConfig = userConfig.subset(JDF_ELEMENT);
        String stacksRepo = jdfConfig.getString(STACKSREPO_ELEMENT);
        try {
            if (stacksRepo == null) {
                return new URL(DEFAULT_STACKS_REPO);
            } else {
                return new URL(stacksRepo);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Wrong repository URL " + stacksRepo);
        }
    }

    /**
     * Verifies if the config file need to be updated. The update procedure will proceed if the version in config file points to
     * a official repository and it is changed in this release
     * 
     * @param userConfig
     * @param defaultUrl
     */
    private void verifyIfNeedConfigurationReset(Configuration userConfig) {
        Configuration jdfConfig = userConfig.subset(JDF_ELEMENT);
        String defaultRepoPrefix = DEFAULT_STACKS_REPO.substring(0, DEFAULT_STACKS_REPO.lastIndexOf("jdf-stack/"));
        String configRepo = jdfConfig.getString(STACKSREPO_ELEMENT);
        // If config uses the official
        if (configRepo.startsWith(defaultRepoPrefix)) {
            // silently update the configuration
            userConfig.setProperty(JDF_ELEMENT + "." + STACKSREPO_ELEMENT, DEFAULT_STACKS_REPO);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#setUrl(java.net.URL)
     */
    @Override
    public void setUrl(URL url) {
        Configuration userConfig = configuration.getScopedConfiguration(ConfigurationScope.USER);
        userConfig.setProperty(JDF_ELEMENT + "." + STACKSREPO_ELEMENT, url.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#getProxyHost()
     */
    @Override
    public String getProxyHost() {
        Configuration proxyConfig = configuration.getScopedConfiguration(ConfigurationScope.USER).subset("proxy");
        if (proxyConfig != null && !proxyConfig.isEmpty()) {
            return proxyConfig.getString("host");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#setProxyHost(java.lang.String)
     */
    @Override
    public void setProxyHost(String proxyHost) {
        throw new RuntimeException("Not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#getProxyPort()
     */
    @Override
    public int getProxyPort() {
        Configuration proxyConfig = configuration.getScopedConfiguration(ConfigurationScope.USER).subset("proxy");
        if (proxyConfig != null && !proxyConfig.isEmpty()) {
            return proxyConfig.getInt("port");
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#setProxyPort(int)
     */
    @Override
    public void setProxyPort(int proxyPort) {
        throw new RuntimeException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#getProxyUser()
     */
    @Override
    public String getProxyUser() {
        Configuration proxyConfig = configuration.getScopedConfiguration(ConfigurationScope.USER).subset("proxy");
        if (proxyConfig != null && !proxyConfig.isEmpty()) {
            proxyConfig.getString("username");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#setProxyUser(java.lang.String)
     */
    @Override
    public void setProxyUser(String proxyUser) {
        throw new RuntimeException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#getProxyPassword()
     */
    @Override
    public String getProxyPassword() {
        Configuration proxyConfig = configuration.getScopedConfiguration(ConfigurationScope.USER).subset("proxy");
        if (proxyConfig != null && !proxyConfig.isEmpty()) {
            proxyConfig.getString("password");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#setProxyPassword(java.lang.String)
     */
    @Override
    public void setProxyPassword(String proxyPassword) {
        throw new RuntimeException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#isOnline()
     */
    @Override
    public boolean isOnline() {
        return environment.isOnline();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#setOnline(boolean)
     */
    @Override
    public void setOnline(boolean online) {
        throw new RuntimeException("Not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#getCacheRefreshPeriodInSeconds()
     */
    @Override
    public int getCacheRefreshPeriodInSeconds() {
        return 86400;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.StacksClientConfiguration#setCacheRefreshPeriodInSeconds(int)
     */
    @Override
    public void setCacheRefreshPeriodInSeconds(int seconds) {
        throw new RuntimeException("Not implemented");

    }

}
