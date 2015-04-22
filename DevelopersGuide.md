# Introduction #

This document describes the repository structure, as well as the development tools used. If you are interested in contributing to Cupid, please contact Todd Schiller (tws@cs.washington.edu) at the University of Washington.



# Mercurial Repository #

The main development work occurs on the `default` branch. The `stable` branch is used for producing snapshot builds and staging releases.

# CheckStyle (Coding Style Enforcement) #

We use [Checkstyle for Eclipse](http://eclipse-cs.sourceforge.net/) to enforce coding style. The configuration file is at the root of the repository in the [checkstyle-rules](https://cupid.eclipselabs.org.codespot.com/hg/checkstyle-rules) file. We haven't run CheckStyle on the project in awhile.

# FindBugs (Static Analyzer) #

We use the [FindBugs](http://findbugs.sourceforge.net/downloads.html) static analyzer to detect possible bugs in the Cupid source code. Ensure that FindBugs reports no possible bugs before checking in code.

# Copyrights and Licenses #

Copyrights and licenses are managed by the [Copyright Wizard](http://www.wdev91.com/?p=cpw) Eclipse plug-in. If a user contributes to a file, they should be added to the contributor list in the file's header.