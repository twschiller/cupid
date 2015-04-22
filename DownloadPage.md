# Installing Cupid (Update Site) #

Cupid can be installed and updated automatically using Eclipse's built in installation and update system.

| Nightly Build Snapshot | http://cupid.eclipselabs.org.codespot.com/hg-history/stable/snapshots/nightly/ |
|:-----------------------|:-------------------------------------------------------------------------------|
| Alpha Release | http://cupid.eclipselabs.org.codespot.com/hg-history/stable/releases/alpha/ |

Instructions for installing plug-ins are available on the Eclipse website: [adding a new software site](http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.user/tasks/tasks-127.htm?cp=0_3_15_5), and [installing new software](http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.user/tasks/tasks-124.htm?cp=0_3_15_1).

**Note: We don't currently offer signed binaries.** When installing Cupid from the update site, Eclipse will show a security warning that "you are installing software that contains unsigned content." Click OK to continue with installation (unless you have reason to believe that your connection with the Google Code server has been compromised).

# Features #

Cupid consists of a core plug-in and multiple extensions that provide additional information sources, visualization, and tools for scripting new capabilities. During installation, you can select which features to install; you can add additional features at any time using Eclipse's installation manager.

  * Information Source Features
    * Mylyn capabilities (requires [Mylyn](http://www.eclipse.org/mylyn/))
    * Subversion capabilities (requires [Subclipse](http://subclipse.tigris.org/); only Subclipse 1.6.x is currently supported)
    * Git capabilities (requires [EGit](http://www.eclipse.org/egit/))
    * Mercurial capabilities (requires [Mercurial Eclipse](http://javaforge.com/project/HGE))
    * JUnit testing capabilities

  * Visualization Features
    * Conditional formatting: creating rules for highlighting table and tree elements
    * Chart views: e.g., pie charts and histograms
    * Mapping view (requires [Zest](http://www.eclipse.org/gef/zest/))
    * Problem markers

  * Scripting Features: write scripts to transform and analyze data
    * Java scripting
    * Groovy scripting (_coming soon!_)

More information about Cupid's features are available on the SetupAndInstructions page.

# Warranty #

The Software is provided "as is" without warranty of any kind, either express or implied, including without limitation any implied warranties of condition, uninterrupted use, merchantability, fitness for a particular purpose, or non-infringement