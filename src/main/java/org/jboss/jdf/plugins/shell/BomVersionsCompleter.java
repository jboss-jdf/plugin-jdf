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
package org.jboss.jdf.plugins.shell;

import static org.jboss.jdf.plugins.JDFPlugin.OPTION_RUNTIME;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.CommandCompleterState;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;
import org.jboss.jdf.stacks.model.BomVersion;
import org.jboss.jdf.stacks.model.Runtime;

/**
 * Return the list of stack boms
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class BomVersionsCompleter extends SimpleTokenCompleter {

    @Inject
    private List<Runtime> availableRuntimes;

    private CommandCompleterState state;

    @Override
    public void complete(CommandCompleterState state) {
        this.state = state;
        super.complete(state);
    }

    @Override
    public Iterable<String> getCompletionTokens() {
        String runtimeId = getInformedRuntime();
        Runtime runtime = getSelectedRuntime(runtimeId);
        List<String> bomIds = new ArrayList<String>();
        for (BomVersion bomVersion : runtime.getBoms()) {
            bomIds.add(bomVersion.getId());
        }
        return bomIds;
    }

    /**
     * @return
     */
    private String getInformedRuntime() {
        String completeCommand = state.getBuffer();
        String[] splitedCommand = completeCommand.split("[\\s]++"); // split by one or more whitespaces
        int cont = 0;
        for (String token : splitedCommand) {
            cont++;
            if (("--" + OPTION_RUNTIME).equals(token)) {
                break;
            }
        }
        return splitedCommand[cont];
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
}
