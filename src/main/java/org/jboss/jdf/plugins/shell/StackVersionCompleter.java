package org.jboss.jdf.plugins.shell;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.CommandCompleterState;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;
import org.jboss.jdf.plugins.stacks.Stack;
import org.jboss.jdf.plugins.stacks.StacksComparator;

import static org.jboss.jdf.plugins.JDFPlugin.OPTION_STACK;

public class StackVersionCompleter extends SimpleTokenCompleter
{

   private CommandCompleterState state;

   @Inject
   private List<Stack> availableStacks;

   @Override
   public Set<String> getCompletionTokens()
   {
      Set<String> versions = new TreeSet<String>(new StacksComparator());
      String informedStack = getInformedStack();
      Stack stack = getSelectedStack(informedStack);
      if (stack != null)
      {
            versions.addAll(stack.getVersions());
      }
      return versions;
   }

   private Stack getSelectedStack(String informedStack)
   {
      for (Stack stack : availableStacks)
      {
         if (informedStack.equals(stack.getId()))
         {
            return stack;
         }
      }
      return null;
   }

   private String getInformedStack()
   {
      String completeCommand = state.getBuffer();
      String[] splitedCommand = completeCommand.split("[\\s]++"); // split by one or more whitespaces
      int cont = 0;
      for (String token : splitedCommand)
      {
         cont++;
         if (("--" + OPTION_STACK).equals(token))
         {
            break;
         }
      }
      return splitedCommand[cont];
   }

   @Override
   public void complete(CommandCompleterState state)
   {
      this.state = state;
      super.complete(state);
   }

}
