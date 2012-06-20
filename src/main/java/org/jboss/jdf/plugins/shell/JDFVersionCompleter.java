package org.jboss.jdf.plugins.shell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;
import org.jboss.jdf.plugins.stacks.Stack;

public class JDFVersionCompleter extends SimpleTokenCompleter
{

   @Inject
   private List<Stack> availableStacks;

   private Set<String> availableVersions;

   @Override
   public Set<String> getCompletionTokens()
   {
      if (availableVersions == null)
      {
         availableVersions = new HashSet<String>();
         for (Stack s : availableStacks)
         {
            availableVersions.addAll(s.getVersions());

         }
      }
      return availableVersions;
   }

}
