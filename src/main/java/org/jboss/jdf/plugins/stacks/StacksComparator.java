package org.jboss.jdf.plugins.stacks;

import java.util.Comparator;

public class StacksComparator implements Comparator<String>
{

   @Override
   public int compare(String version1, String version2)
   {
      if (version1.endsWith("*")){
         return -1;
      }else{
         return +1;
      }
   }

}
