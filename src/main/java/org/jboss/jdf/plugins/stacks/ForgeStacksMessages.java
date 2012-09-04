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

import javax.inject.Inject;

import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.jdf.stacks.client.messages.StacksMessages;

/**
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class ForgeStacksMessages implements StacksMessages {

    @Inject
    private Shell shell;

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.messages.StacksMessages#showInfoMessage(java.lang.String)
     */
    @Override
    public void showInfoMessage(String infoMessage) {
        shell.println(infoMessage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.messages.StacksMessages#showWarnMessage(java.lang.String)
     */
    @Override
    public void showWarnMessage(String warnMessage) {
        ShellMessages.warn(shell, warnMessage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.messages.StacksMessages#showDebugMessage(java.lang.String)
     */
    @Override
    public void showDebugMessage(String debugMessage) {
        if (shell.isVerbose()) {
            shell.println();
            shell.println(debugMessage);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.messages.StacksMessages#showErrorMessage(java.lang.String)
     */
    @Override
    public void showErrorMessage(String errorMessage) {
        ShellMessages.error(shell, errorMessage);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.jdf.stacks.client.messages.StacksMessages#showErrorMessageWithCause(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void showErrorMessageWithCause(String errorMessage, Throwable cause) {
        ShellMessages.error(shell, errorMessage);
        if (shell.isVerbose()){
            cause.printStackTrace();
        }
    }

}
