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

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.jdf.plugins.providers.JDFBOMProvider;
import org.jboss.jdf.plugins.shell.AvailableStacksCompleter;
import org.jboss.jdf.plugins.shell.JDFVersionCompleter;
import org.jboss.jdf.plugins.stacks.Stack;

/**
 * The JDF Plugin itself
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 */
@Alias("jdf")
@RequiresProject
public class JDFPlugin implements Plugin
{

   @Inject
   private List<Stack> availableStacks;

   @Inject
   private JDFBOMProvider bomProvider;

   @DefaultCommand(help = "Install a JDF JBoss Stack")
   public void installStack(
            @Option(name = "stack", required = true, completer = AvailableStacksCompleter.class) String stack,
            @Option(name = "version", required = true, completer = JDFVersionCompleter.class) String version,
            PipeOut out)
   {
      Stack selectedStack = getSelectedStack(stack);
      // validate input
      if (selectedStack == null)
      {
         out.println(ShellColor.RED, "There is no stack [" + stack + "]. Try one of those: " + availableStacks);
         return;
      }
      if (!selectedStack.getVersions().contains(version))
      {
         out.println(ShellColor.RED, "There is no version [" + version + "] for this stack [" + selectedStack
                  + "]. Try one of those: " + selectedStack.getVersions());
         return;
      }

      if (bomProvider.isDependencyManagementInstalled(selectedStack.getArtifact()))
      {
         out.println("Stack " + stack + " already installed");
      }
      else
      {
         bomProvider.installBom(selectedStack.getArtifact(), version);
         out.println("Stack " + stack + " installed!");
      }
   }

   private Stack getSelectedStack(String informedStack)
   {
      for (Stack stack : availableStacks)
      {
         if (stack.getId().equals(informedStack))
         {
            return stack;
         }
      }
      return null;
   }

}
