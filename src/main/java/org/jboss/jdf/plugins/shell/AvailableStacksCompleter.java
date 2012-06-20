package org.jboss.jdf.plugins.shell;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;
import org.jboss.jdf.plugins.stacks.Stack;

public class AvailableStacksCompleter extends SimpleTokenCompleter
{

   @Inject
   private List<Stack> availableStacks;

   @Override
   public Iterable<?> getCompletionTokens()
   {
      return availableStacks;
   }

}
