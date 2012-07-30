JBoss Developer Framework plugin
=======
Author: Rafael Benevides

To simplify selecting stacks using Forge.


What is it?
-----------

This is a plugin for [JBoss Forge](http://jboss.org/forge) to simplify the setup of a [JBoss Stacks](http://www.jboss.org/jdf/stack/stacks).

The stack is provided by Maven BOMs (Bill of Materials); read more [about Maven BOMs](http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html). JBoss Developer Framework provides a BOM for [every stack](http://www.jboss.org/jdf/stack/jboss-bom/).


System requirements
-------------------

All you need is to use this plugin is JBoss Forge 1.0.5 (or latter) and a working internet connection.


Installing the plugin
---------------------

Assuming Forge is running, at the Forge prompt, type:

    forge install-plugin jdf


Running the plugin
-------------------

NOTE: The first time you use the plugin, you must have a working internet connection.

In Forge console type:

    jdf use-stack --stack

and press _Tab_. This will load the list of available stacks from the JBoss Developer Framework Stacks repository (by default <https://raw.github.com/jboss-jdf/jdf-stack/master/stacks.yaml>). You can then select the stack to use.

Or you can also specify the version of the stack to use (by default, Forge will select the version currently recommended by JBoss Developer Framework):

    jdf use-stack --stack jboss-javaee-6.0-with-errai --version 1.0.0.Final

You can add multiples stacks to your project, simply run the `jdf use-stack` command again.

The list of available stacks is cached, and reloaded once a day. You can force a reload by running the `refresh-stacks` command:

    jdf refresh-stacks

You can also view the available stacks by running:

    jdf show-stacks


Custom Repository
-----------------

If you want to change the default stack repository, then you will need to modify the Forge configuration.

1. Edit `~/.forge/config.xml` in your favourite editor
2. Add the `<stacksRepo>` element, with the custom location, to the `<jdf>` element. For example:  

        <configuration> 
            ...
            <jdf> 
                <stacksRepo>file:///home/benevides/stacks.yaml</stacksRepo> 
            </jdf>
            ... 
        </configuration> 

3. Run `jdf refresh-stacks` to force an update of the repository information

Proxy Configuration
-------------------
If you access the Internet through a proxy, you must [configure Forge](https://docs.jboss.org/author/display/FORGE/Configure+HTTP+Proxy).

Offline use
------------

If you do not have a working internet connection, the plugin will use the cached list of available stacks.

You can force Forge to use the cached list of available stacks:  

    set OFFLINE true


Troubleshooting
---------------

You can turn on debugging messages:   
   
   set VERBOSE true

