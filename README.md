plugin-jdf
==========

jdf plugin for Forge, that allows easy selection and use of JBoss BOMs
=======
Author: Rafael Benevides


What is it?
-----------

This is a plugin for [JBoss Forge] (http://jboss.org/forge) to simplify the setup
of a [JDF - JBoss Developer Framework] (http://www.jboss.org/jdf/) Stack 

The Stack is provided through the BOM concept. BOM is the abbreviation for Bill of Materials.
More information about the BOM concept can be found at http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html

JDF provides some BOMs each one for each available Stack. 
For more detailed information of each Stack you can take a look at: http://www.jboss.org/jdf/stack/jboss-bom/

The purpose of this plugin is to help the developers to add a JDF Stack to a Project.



System requirements
-------------------

All you need is to use this plugin is JBoss Forge 1.0.5 (or latter) and
an active Internet connection (at least for the first use of the plugin).




Installing the plugin
---------------------

Run forge console by typing

`forge` -- Linux  

`forge.bat` -- Windows

After in Forge prompt type  
`$ forge git-plugin https://github.com/jboss-jdf/plugin-jdf.git `



Running the plugin
-------------------

In Forge console type:
`jdf --stack [TAB]`

this should connect to the JDF Stacks repository 
(default to https://raw.github.com/jboss-jdf/jdf-stack/master/stacks.yaml)
and retrieve the list of available JDF Stacks.

OBS: If this is the first time that you use this plugin, you should be online.

You can also type the complete command: 
`
jdf --stack jboss-javaee-6.0-with-errai --version 1.0.0.Final
`

**You can add multiples stacks to your project



Custom Repository
-----------------

If for any reason you want to change the default repository location, you must configure the jdf settings in Forge

The jdf config file (jdfconfig.xml) is located inside forge config dir.

1. Open the user configuration in ~/.forge/config.xml (located in your home directory.)
2. Add the 'jdf' tag and required information inside the 'configuration' root tag:  
>"	<configuration> "
>"  		<jdf> "
>"			<stacksRepo>file:///home/benevides/stacks.yaml</stacksRepo> "
>"		</jdf> "
>"	</configuration> "



Proxy Configuration
-------------------
If your are accessing Internet through a proxy, you should configure Forge
For more information take a look at https://docs.jboss.org/author/display/FORGE/Configure+HTTP+Proxy



Offline use
------------

If for any reason you can not be online, the plugin will use a previously stored
cache file as source of available stacks.

If you want to force the offline use, you can set the OFFLINE property typing:  
`
set OFFLINE true
`



Troubleshooting
---------------

If you're experiencing some problems, you can turn on debug messages setting
the VERBOSE property typing:   
`
set VERBOSE true
`
