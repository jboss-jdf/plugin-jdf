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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jdf.stacks.client.StacksClient;
import org.jboss.jdf.stacks.model.Bom;
import org.jboss.jdf.stacks.model.BomVersion;

/**
 * This is a Utility class that handle the available JDF Stacks from a repository using YAML
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class StacksUtil {

    @Inject
    private ForgeStacksClientConfiguration stacksClientConfiguration;

    @Inject
    private ForgeStacksMessages stacksMessages;

    /**
     * This method verifies all available Stacks in repository
     * 
     * @return Available Stacks
     */
    @Produces
    public List<Bom> retrieveAvailableBoms() {
        StacksClient stacksClient = new StacksClient(stacksClientConfiguration, stacksMessages);
        return stacksClient.getStacks().getAvailableBoms();
    }

    /**
     * 
     * Retrieve all bom versions
     * 
     * @param bom
     * @return
     */
    public List<BomVersion> getAllVersions(Bom bom) {
        StacksClient stacksClient = new StacksClient(stacksClientConfiguration, stacksMessages);

        List<BomVersion> bomVersions = new ArrayList<BomVersion>();
        List<BomVersion> allVersions = stacksClient.getStacks().getAvailableBomVersions();

        for (BomVersion bomVersion : allVersions) {
            // if version corresponds to the groupId and artifactId of the bom parameter
            if (bomVersion.getBom().getGroupId().equals(bom.getGroupId())
                    && bomVersion.getBom().getArtifactId().equals(bom.getArtifactId())) {
                bomVersions.add(bomVersion);
            }
        }
        return bomVersions;
    }

}
