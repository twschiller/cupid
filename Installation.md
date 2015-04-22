# Installing Cupid (Update Site) #

Cupid can be installed and updated automatically using Eclipse's built in installation and update system.

| Beta | http://cupid.eclipselabs.org.codespot.com/hg-history/stable/releases/beta/ |
|:-----|:---------------------------------------------------------------------------|
| Nightly Build Snapshot | http://cupid.eclipselabs.org.codespot.com/hg-history/stable/snapshots/nightly/ |

The "Cupid Optional Plug-ins" Feature will install all Cupid plug-ins that are compatible with your system; it's designed for use with the Eclipse Marketplace, we suggest you individually specify the additional plug-ins to install when using the update site.

Instructions for installing plug-ins are available on the Eclipse website: [adding a new software site](http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.user/tasks/tasks-127.htm?cp=0_3_15_5), and [installing new software](http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.user/tasks/tasks-124.htm?cp=0_3_15_1).

**Note: We don't currently offer signed binaries.** When installing Cupid from the update site, Eclipse will show a security warning that "you are installing software that contains unsigned content." Click OK to continue with installation (unless you have reason to believe that your connection with the Google Code server has been compromised).

**Having problems installing Cupid?** Send email to `tws@cs.washington.edu`.

## System Requirements ##
Cupid requires Eclipse 3.7.2 (Indigo SR2), Eclipse 4.x, or later.

# Features #

Cupid consists of a core plug-in and multiple extensions that provide additional information sources and visualizations. During installation, you can select which features to install; you can add additional features at any time using Eclipse's installation manager.

NOTE: the following features are _optional_. Give the information source features a try if you have the plug-in already installed; information can be pulled from any plug-in via the plug-in's information views.

  * Information Source Features
    * Mylyn capabilities (requires [Mylyn](http://www.eclipse.org/mylyn/))
    * Git capabilities (requires [EGit](http://www.eclipse.org/egit/))
    * Mercurial capabilities (requires [Mercurial Eclipse](http://javaforge.com/project/HGE))

  * Visualization Features
    * Chart views: e.g., pie charts and histograms
    * Mapping view (requires [Zest Visualization Toolkit and Zest SDK](http://www.eclipse.org/gef/zest/); see [Installing Zest](CupidExplained#InstallingZest.md) below)
    * Problem / information markers

More information about Cupid's features are available on the GettingStarted page.

## Installing Zest ##

[Zest](http://www.eclipse.org/gef/zest/) is a Visualization Tookit. Zest Releases can be installed via the update site: http://download.eclipse.org/tools/gef/updates/releases/. For some Eclipse installs, the update site might already be included as an update site but "disabled". To enable the update site, open the preferences window, go to `Install/Update -> Available Software Sites`, click the Zest update site, and then click "Enable". The update site will now be listed in the "Install new software..." dialog.

# Warranty #

The Software is provided "as is" without warranty of any kind, either express or implied, including without limitation any implied warranties of condition, uninterrupted use, merchantability, fitness for a particular purpose, or non-infringement