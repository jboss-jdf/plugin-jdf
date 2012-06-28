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
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
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

   @Command(value = "use-stack", help = "Enable JDF JBoss Stack in to a Project")
   public void installStack(
            @Option(name = OPTION_STACK, required = true, completer = AvailableStacksCompleter.class, description = "Stack Id") String stack,
            @Option(name = "version", required = false, completer = StackVersionCompleter.class,
                     description = "Recommended JDF Stack Version") String version,
            PipeOut out)
   {
      Stack selectedStack = getSelectedStack(stack);
      String chosenVersion = chooseVersion(selectedStack, version);
      // validate input
      if (isInvalidInput(selectedStack, stack, chosenVersion, out))
      {
         return;
      }

      if (bomProvider.isDependencyManagementInstalled(selectedStack.getArtifact()))
      {
         handleStackAlreadyInstaled(selectedStack, chosenVersion, out);
      }
      else
      {
         handleStackInstalation(selectedStack, chosenVersion, out);
      }
   }

   @Command(value = "show-stacks", help = "List the available stacks")
   public void listStacks(PipeOut out)
   {
      for (Stack stack : availableStacks)
      {
         out.println(" - " + out.renderColor(ShellColor.BOLD, stack.getId()) + " (" + stack.getName() + ")");
         out.println("\tDescription: " + stack.getDescription());
         out.println("\tArtifact: " + stack.getArtifact());
         out.println("\tRecommended Version: " + out.renderColor(ShellColor.GREEN, stack.getRecommendedVersion()));
         if (stack.getAvailableVersions().size() > 0)
         {
            out.println("\tAvailable Versions:");
         }
         for (String availableVersion: stack.getAvailableVersions()){
            out.println(ShellColor.BLUE, "\t\t - " + availableVersion);
         }
         out.println();
      }
   }

   /**
    * Permits the user choose on of the available versions of the informed stack.
    * 
    * @param selectedStack
    * @param version if null, user must chose one version of the selectedStack
    * @return the chosen version
    */
   private String chooseVersion(Stack selectedStack, String version)
   {
      if (selectedStack != null && version == null)
      {
         return shellPrompt.promptChoiceTyped("Whice version of stack " + selectedStack,
                  selectedStack.getAvailableVersions(), selectedStack.getRecommendedVersion());
      }
      return version;
   }

   /**
    * Interacts with the user in a Stack Installation
    * 
    * @param selectedStack
    * @param version
    * @param out
    */
   private void handleStackInstalation(Stack selectedStack, String version, PipeOut out)
   {
      if (!selectedStack.getRecommendedVersion().equals(version))
      {
         boolean installNotRecommended = shellPrompt.promptBoolean(
                  "You didn't choose the recommended version. Do you want continue the installation?", false);
         if (!installNotRecommended)
         {
            return;
         }
      }
      addStack(selectedStack, version, out);
   }

   /**
    * Add a Stack (almost) without user interaction
    * 
    * @param selectedStack
    * @param version
    * @param out
    */
   private void addStack(Stack selectedStack, String version, PipeOut out)
   {
      bomProvider.installBom(selectedStack.getArtifact(), version);
      ShellMessages.success(out, "Stack " + selectedStack.getName() + " version " + version + " installed!");
   }

   /**
    * Interacts with the user with a Stack is already installed. If the installed Stack is in a different version, the
    * JDF plugin prompts the user if an update is necessary.
    * 
    * @param selectedStack
    * @param version
    * @param out
    */
   private void handleStackAlreadyInstaled(Stack selectedStack, String version, PipeOut out)
   {
      String previousStackVersion = bomProvider.getInstalledVersionStack(selectedStack.getArtifact());
      ShellMessages.info(out, "Stack " + selectedStack.getName() + " already installed");
      // If <> installed stack version
      if (!previousStackVersion.equals(version))
      {
         ShellMessages.warn(out, " Another version of this stack is installed: " + previousStackVersion);
         boolean shouldUpdate = shellPrompt
                  .promptBoolean("Do you want to update this Stack version to: " + version + " ?", false);
         if (shouldUpdate)
         {
            bomProvider.removeBom(selectedStack.getArtifact(), previousStackVersion);
            // For an atomic update, adding stack has no user interaction if the new version is not one of the
            // recommended. So addStack() is called instead of handleStackInstalation()
            addStack(selectedStack, version, out);
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
    * @param selectedStack
    * @param stack
    * @param version
    * @param out
    * @return true if has any invalid input
    */
   private boolean isInvalidInput(Stack selectedStack, String stack, String version, PipeOut out)
   {
      if (selectedStack == null)
      {
         ShellMessages.error(out, "There is no stack [" + stack + "]. Try one of those: " + availableStacks);
         return true;
      }
      if (!selectedStack.getAvailableVersions().contains(version))
      {
         ShellMessages.error(out, "There is no version [" + version + "] for this stack [" + selectedStack
                  + "]. Try one of those: " + selectedStack.getAvailableVersions());
         return true;
      }
      return false;

   }

   /**
    * Finds the stack object based on its id
    * 
    * @param informedStack the stack id
    * @return stack
    */
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
