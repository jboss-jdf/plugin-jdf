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
package org.jboss.jdf.plugins;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.jdf.plugins.providers.JDFBOMProvider;
import org.jboss.jdf.plugins.shell.AvailableRuntimesCompleter;
import org.jboss.jdf.plugins.shell.BomVersionsCompleter;
import org.jboss.jdf.plugins.stacks.ForgeStacksClientConfiguration;
import org.jboss.jdf.plugins.stacks.ForgeStacksMessages;
import org.jboss.jdf.plugins.stacks.StacksUtil;
import org.jboss.jdf.stacks.client.StacksClient;
import org.jboss.jdf.stacks.model.Bom;
import org.jboss.jdf.stacks.model.BomVersion;
import org.jboss.jdf.stacks.model.Runtime;

/**
 * The JDF Plugin itself
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 */
@Alias("jdf")
@RequiresProject
public class JDFPlugin implements Plugin {

    public static final String OPTION_BOM = "bom";

    public static final String OPTION_RUNTIME = "runtime";

    @Inject
    private ForgeStacksClientConfiguration stacksClientConfiguration;

    @Inject
    private ForgeStacksMessages stacksMessages;

    @Inject
    private List<Bom> availableBoms;

    @Inject
    private List<Runtime> availableRuntimes;

    @Inject
    private List<BomVersion> availableBomVersions;

    @Inject
    private JDFBOMProvider bomProvider;

    @Inject
    private ShellPrompt shellPrompt;

    @Inject
    private StacksUtil stacksUtil;

    @Command(value = "use-stack", help = "Enable JDF JBoss Stack in to a Project")
    public void useStack(
            @Option(name = OPTION_RUNTIME, required = true, completer = AvailableRuntimesCompleter.class, description = "Runtime id") String runtimeId,
            @Option(name = OPTION_BOM, required = true, completer = BomVersionsCompleter.class, description = "Bom Version Id") String bomVersionId,
            PipeOut out) {
        Runtime selectedRuntime = getSelectedRuntime(runtimeId);
        BomVersion selectedBomVersion = getSelectedBomVersion(bomVersionId);
        // validate input
        if (isInvalidInput(runtimeId, selectedRuntime, bomVersionId, selectedBomVersion, out)) {
            return;
        }

        if (bomProvider.isDependencyManagementInstalled(selectedBomVersion.getBom().getGroupId(), selectedBomVersion.getBom()
                .getArtifactId())) {
            handleStackAlreadyInstaled(selectedBomVersion, out);
        } else {
            handleStackInstalation(selectedBomVersion, out);
        }
    }

    @Command(value = "show-boms", help = "List the available BOMs")
    public void listBoms(PipeOut out) {
        for (Bom bom : availableBoms) {
            out.println(" - " + out.renderColor(ShellColor.BOLD, bom.getArtifactId()) + " (" + bom.getName() + ")");
            out.println("\tDescription: " + bom.getDescription());
            out.println("\tArtifactId: " + bom.getArtifactId());
            out.println("\tGroupId: " + bom.getGroupId());
            out.println("\tRecommended Version: " + out.renderColor(ShellColor.GREEN, bom.getRecommendedVersion()));
            List<BomVersion> bomVersions = new ArrayList<BomVersion>();
            for (BomVersion bomVersion : availableBomVersions) {
                // if version corresponds to the groupId and artifactId of the bom parameter
                if (bomVersion.getBom().getGroupId().equals(bom.getGroupId())
                        && bomVersion.getBom().getArtifactId().equals(bom.getArtifactId())) {
                    bomVersions.add(bomVersion);
                }
            }
            if (bomVersions.size() > 0) {
                out.println("\tAvailable Versions:");
            }
            for (BomVersion availableVersion : bomVersions) {
                out.println(ShellColor.BLUE, "\t\t - " + availableVersion.getVersion());
            }
            out.println();
        }
    }

    @Command(value = "show-runtimes", help = "List the available Runtimes")
    public void listRuntimes(PipeOut out) {
        for (Runtime runtime : availableRuntimes) {
            out.println(" - " + out.renderColor(ShellColor.BOLD, runtime.getId()) + " (" + runtime.getName() + " - "
                    + runtime.getVersion() + ") ");
            out.println("\tBOMs:");
            for (BomVersion bomVersion : runtime.getBoms()) {
                out.print(ShellColor.BLUE, "\t\t - " + bomVersion.getId());
                out.println(" (" + bomVersion.getBom().getName() + " - " + bomVersion.getVersion() + ")");
            }
            out.print("\tDefault BOM: " + out.renderColor(ShellColor.GREEN, runtime.getDefaultBom().getId()));
            out.println(" (" + runtime.getDefaultBom().getBom().getName() + " - " + runtime.getDefaultBom().getVersion() + ")");
            out.println();
        }
    }

    @Command(value = "refresh-stacks", help = "Force the update of the Stacks. It is updated automatically once a day")
    public void refreshStacks(PipeOut out) {
        // Destroying the cache, forces it to be updated
        new StacksClient(stacksClientConfiguration, stacksMessages).eraseRepositoryCache();
        // Force the update
        List<Runtime> runtimes = stacksUtil.retrieveAvailableRuntimes();
        if (runtimes != null) {
            ShellMessages.success(out, "Stacks updated from the following repository: " + stacksClientConfiguration.getUrl());
        }
    }

    /**
     * Interacts with the user in a Stack Installation
     * 
     * @param selectedRuntime
     * 
     * @param selectedStack
     * @param version
     * @param out
     */
    private void handleStackInstalation(BomVersion bomVersion, PipeOut out) {
        bomProvider.installBom(bomVersion.getBom().getGroupId(), bomVersion.getBom().getArtifactId(), bomVersion.getVersion());
        ShellMessages.success(out, "Stack " + bomVersion.getId() + "  installed!");
    }

    /**
     * Interacts with the user with a Stack is already installed. If the installed Stack is in a different version, the JDF
     * plugin prompts the user if an update is necessary.
     * 
     * @param selectedStack
     * @param version
     * @param out
     */
    private void handleStackAlreadyInstaled(BomVersion bomVersion, PipeOut out) {
        String previousStackVersion = bomProvider.getInstalledVersionStack(bomVersion.getBom().getGroupId(), bomVersion
                .getBom().getArtifactId());
        ShellMessages.info(out, "BOM " + bomVersion.getId() + " already installed");
        // If <> installed stack version
        if (!previousStackVersion.equals(bomVersion.getVersion())) {
            ShellMessages.warn(out, " Another version of this bom is installed: " + previousStackVersion);
            boolean shouldUpdate = shellPrompt.promptBoolean(
                    "Do you want to change this BOM version to: " + bomVersion.getVersion() + " ?", false);
            if (shouldUpdate) {
                bomProvider.removeBom(bomVersion.getBom().getGroupId(), bomVersion.getBom().getArtifactId(),
                        previousStackVersion);
                handleStackInstalation(bomVersion, out);
            }
        }
        out.println();
    }

    /**
     * Validate the user input values.
     * 
     * The selected Stack should be one of the available stacks.
     * 
     * The version must be one of the available versions of the stack.
     * 
     * @param bomVersionId
     * @param runtimeId
     * 
     * @param selectedBom
     * @param informedBomId
     * @param version
     * @param out
     * @return true if has any invalid input
     */
    private boolean isInvalidInput(String runtimeId, Runtime selectedRuntime, String bomVersionId,
            BomVersion selectedBomVersion, PipeOut out) {
        if (selectedRuntime == null) {
            ShellMessages.error(out, "There is no Runtime [" + runtimeId + "]. Try one of those: ");
            for (Runtime runtime : availableRuntimes) {
                out.println(runtime.getId());
            }
            return true;
        }
        if (selectedBomVersion == null) {
            ShellMessages.error(out, "There is no BOM version [" + bomVersionId + "]. Try one of those: ");
            for (BomVersion bomVersion : availableBomVersions) {
                out.println(bomVersion.getId());
            }
            return true;
        }
        if (!selectedRuntime.getBoms().contains(selectedBomVersion)) {
            ShellMessages.error(out, "There is no BOM version [" + bomVersionId + "] for this Runtime [" + runtimeId
                    + "]. Try one of those: ");
            for (BomVersion bomVersion : selectedRuntime.getBoms()) {
                out.println(bomVersion.getId());
            }
            return true;
        }
        return false;
    }

    /**
     * Finds the runtime object based on its id
     * 
     * @param runtimeId the runtime id
     * @return runtime
     */
    private Runtime getSelectedRuntime(String runtimeId) {
        for (Runtime runtime : availableRuntimes) {
            if (runtime.getId().equals(runtimeId)) {
                return runtime;
            }
        }
        return null;
    }

    /**
     * Finds the Bom Version object based on its id
     * 
     * @param runtimeId the runtime id
     * @return runtime
     */
    private BomVersion getSelectedBomVersion(String bomVersionId) {
        for (BomVersion bomVersion : availableBomVersions) {
            if (bomVersion.getId().equals(bomVersionId)) {
                return bomVersion;
            }
        }
        return null;
    }

}
