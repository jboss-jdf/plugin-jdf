JBoss Developer Framework plugin
=======
Author: Rafael Benevides
Summary: Forge plugin to help you select the right JBoss stack for your project.


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

In Forge console type:

    jdf use-stack --runtime

and press _Tab_. This will load the list of available runtimes from the JBoss Developer Framework Stacks repository (by default <https://raw.github.com/jboss-jdf/jdf-stack/1.0.0.Final/stacks.yaml>). You can then select the stack to use.

Then you can also specify the bom version to use

    jdf use-stack --runtime jboss-as711runtime --bom jboss-javaee-6_0-all-301

You can add multiples stacks to your project, simply run the `jdf use-stack` command again.

The list of available stacks is cached, and reloaded once a day. You can force a reload by running the `refresh-stacks` command:

    jdf refresh-stacks

You can also view the available boms by running:

    jdf show-boms

You can also view the available runtimes by running:

    jdf show-runtimes

Custom Repository
-----------------

If you want to change the default stack repository, then you will need to modify the Forge configuration.

1. Edit `~/.forge/config.xml` in your favorite editor
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

