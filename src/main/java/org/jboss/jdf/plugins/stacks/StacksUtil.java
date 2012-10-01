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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jdf.stacks.client.StacksClient;
import org.jboss.jdf.stacks.model.Bom;
import org.jboss.jdf.stacks.model.BomVersion;
import org.jboss.jdf.stacks.model.Runtime;

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

    private StacksClient stacksClient;

    @PostConstruct
    void setupStacksClient() {
        stacksClient = new StacksClient(stacksClientConfiguration, stacksMessages);
    }

    /**
     * This method retrieves all available Runtimes in repository
     * 
     * @return Available Runtimes
     */
    @Produces
    public List<Runtime> retrieveAvailableRuntimes() {
        return stacksClient.getStacks().getAvailableRuntimes();
    }

    /**
     * This method retrieves all available BOMs in repository
     * 
     * @return Available BOMs
     */
    @Produces
    public List<Bom> retrieveAvailableBoms() {
        return stacksClient.getStacks().getAvailableBoms();
    }

    /**
     * This method retrieve all available BOM versions in repository
     * 
     * @return Available BOM versions
     */
    @Produces
    public List<BomVersion> retrieveAvailableBomVersions() {
        return stacksClient.getStacks().getAvailableBomVersions();
    }

}
