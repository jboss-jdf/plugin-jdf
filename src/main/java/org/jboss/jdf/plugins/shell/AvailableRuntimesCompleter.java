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

package org.jboss.jdf.plugins.shell;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;
import org.jboss.jdf.stacks.model.Runtime;

/**
 * 
 * Return the list of stack runtimes
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class AvailableRuntimesCompleter extends SimpleTokenCompleter {

    @Inject
    private List<Runtime> availableRuntimes;

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.forge.shell.completer.SimpleTokenCompleter#getCompletionTokens()
     */
    @Override
    public Iterable<?> getCompletionTokens() {
        List<String> runtimeIds = new ArrayList<String>();
        for (Runtime runtime : availableRuntimes) {
            runtimeIds.add(runtime.getId());
        }
        return runtimeIds;
    }

}
