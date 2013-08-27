/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.jdf;

import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.jdf.plugins.JDFPlugin;
import org.jboss.jdf.plugins.providers.JDFBOMProvider;
import org.jboss.jdf.plugins.stacks.ForgeStacksClientConfiguration;
import org.jboss.jdf.plugins.stacks.StacksUtil;
import org.jboss.jdf.stacks.model.Bom;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;

public class JdfPluginTest extends AbstractShellTest {

    private static final String STACK_GROUP = "org.jboss.bom";
    private static final String STACK_ARTIFACT = "jboss-javaee-6.0-with-errai";
    private static final String STACK_VERSION = "1.0.0.Final";

    @Inject
    private JDFBOMProvider bomProvider;

    @Inject
    private StacksUtil stacksUtil;

    @Inject
    private ForgeStacksClientConfiguration forgeStacksClientConfiguration;

    @Before
    public void setup() throws Exception {
        initializeJavaProject();
    }

    @Deployment
    public static JavaArchive getDeployment() {
        return AbstractShellTest.getDeployment().addPackages(true, JDFPlugin.class.getPackage());
    }

    @Test
    public void testShellexecuteUseStack() throws Exception {
        queueInputLines("y");
        getShell().execute("jdf use-stack --runtime jboss-as711runtime --bom jboss-javaee-6_0-all-301");
    }

    @Test
    public void testShellexecuteShowBoms() throws Exception {
        queueInputLines("y");
        getShell().execute("jdf show-boms");
    }

    @Test
    public void testShellexecuteShowRuntimes() throws Exception {
        queueInputLines("y");
        getShell().execute("jdf show-runtimes");
    }
    
    @Test
    public void testShellexecuteRefreshStacks() throws Exception {
        queueInputLines("y");
        getShell().execute("jdf refresh-stacks");
        Assert.assertNotNull("Stacks should be available after a refresh", stacksUtil.retrieveAvailableBoms());
    }

    @Test
    public void testAvailableStacks() throws Exception {
        List<Bom> availableStacks = stacksUtil.retrieveAvailableBoms();
//        System.out.println(availableStacks.size());
        Assert.assertTrue(availableStacks.size() == 34);
    }

    @Test
    public void testBOMInstallation() throws Exception {
        Assert.assertFalse(bomProvider.isDependencyManagementInstalled(STACK_GROUP, STACK_ARTIFACT));
        bomProvider.installBom(STACK_GROUP, STACK_ARTIFACT, STACK_VERSION);
        Assert.assertTrue("Stack should be installed", bomProvider.isDependencyManagementInstalled(STACK_GROUP, STACK_ARTIFACT));
    }

    @Test
    public void testBOMRemoval() throws Exception {
        Assert.assertFalse(bomProvider.isDependencyManagementInstalled(STACK_GROUP, STACK_ARTIFACT));
        testBOMInstallation();
        bomProvider.installBom(STACK_GROUP, STACK_ARTIFACT, STACK_VERSION);
        Assert.assertTrue("Stack should not be installed",
                bomProvider.isDependencyManagementInstalled(STACK_GROUP, STACK_ARTIFACT));
    }

    @Test
    public void testGetArtifactVersion() throws Exception {
        Assert.assertFalse(bomProvider.isDependencyManagementInstalled(STACK_GROUP, STACK_ARTIFACT));
        testBOMInstallation();
        bomProvider.installBom(STACK_GROUP, STACK_ARTIFACT, STACK_VERSION);
        Assert.assertEquals("Stack should be installed with the same version", STACK_VERSION,
                bomProvider.getInstalledVersionStack(STACK_GROUP, STACK_ARTIFACT));
    }

    @Test
    public void testStackRepoFile() throws Exception {
        String repo = forgeStacksClientConfiguration.getUrl().toString();
        Assert.assertEquals("https://raw.github.com/jboss-jdf/jdf-stack/1.0.0.Final/stacks.yaml", repo);
    }
}
