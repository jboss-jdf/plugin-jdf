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
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.jdf.plugins.providers.JDFBOMProvider;
import org.jboss.jdf.plugins.shell.AvailableStacksCompleter;
import org.jboss.jdf.plugins.shell.StackVersionCompleter;
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
   public static final String OPTION_STACK = "stack";

   @Inject
   private List<Stack> availableStacks;

   @Inject
   private JDFBOMProvider bomProvider;

   @Inject
   private ShellPrompt shellPrompt;

   @DefaultCommand(help = "Install a JDF JBoss Stack")
   public void installStack(
            @Option(name = OPTION_STACK, required = true, completer = AvailableStacksCompleter.class, description = "Stack Id") String stack,
            @Option(name = "version", required = true, completer = StackVersionCompleter.class,
                     description = "Stack Version - '*' Recommended JDF Stack Version") String version,
            PipeOut out)
   {
      Stack selectedStack = getSelectedStack(stack);
      // validate input
      if (isInvalidInput(selectedStack, stack, version, out))
      {
         return;
      }

      if (bomProvider.isDependencyManagementInstalled(selectedStack.getArtifact()))
      {
         String installedStackVersion = bomProvider.getInstalledVersionStack(selectedStack.getArtifact());
         out.print("Stack " + stack + " already installed");
         if (!installedStackVersion.equals(version))
         {
            out.print(" with a diferent version: " + installedStackVersion);
         }
         out.println();
      }
      else
      {
         if (!version.endsWith("*"))
         {
            boolean installNotRecommended = shellPrompt.promptBoolean(
                     "You didn't choose the recommended version. Do you want continue the instalation?", false);
            if (!installNotRecommended)
            {
               return;
            }
         }
         bomProvider.installBom(selectedStack.getArtifact(), version);
         out.println("Stack " + stack + " installed!");
      }
   }

   private boolean isInvalidInput(Stack selectedStack, String stack, String version, PipeOut out)
   {
      if (selectedStack == null)
      {
         out.println(ShellColor.RED, "There is no stack [" + stack + "]. Try one of those: " + availableStacks);
         return true;
      }
      if (!selectedStack.getVersions().contains(version))
      {
         out.println(ShellColor.RED, "There is no version [" + version + "] for this stack [" + selectedStack
                  + "]. Try one of those: " + selectedStack.getVersions());
         return true;
      }
      return false;

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
