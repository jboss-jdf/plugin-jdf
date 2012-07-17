package org.jboss.jdf;

import junit.framework.Assert;

import org.jboss.jdf.plugins.stacks.Parser;
import org.jboss.jdf.plugins.stacks.Parser.RuntimeType;
import org.jboss.jdf.plugins.stacks.Parser.Stacks;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParserTest
{

   private static Parser parser;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      parser = new Parser();
   }

   @Test
   public void test()
   {
      Stacks stacks = parser.parse(this.getClass().getResourceAsStream("/stacks.yaml"));
      Assert.assertEquals(6, stacks.getAvailableBoms().size());
      Assert.assertEquals(RuntimeType.EAP, stacks.getAvailableRuntimes().get(0).getType());
   }

}
