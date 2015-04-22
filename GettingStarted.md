# Getting Started with Cupid #

This page provides an overview of Cupid and a basic feature walk-through.

_This wiki is a work in progress, and parts of it may still be under construction_



## What is Cupid? ##

Cupid is an information mash-up and visualization plug-in for Eclipse. Cupid lets you combine information from any plug-ins by mixing together data sources and scripts, and visualize the information using views such as charts and highlighting. The goal of Cupid is to give you immediate access the information you need to gain insight into your project and team.

Cupid can help answer and gain insight into many questions that arise during development, for example:

  * Who should I contact about this file? (who is the file's most frequent committer?)
  * Which parts of a debugging stack trace correspond to recently changed files?
  * What has Susan been working on?

## Why Use Cupid? ##

Plug-ins such as version control, task management, debugging, and testing provide information about your project. Together, they provide insight into your project and team, including how they have evolved over time. However, in many cases, these plug-ins don't show information in the form that's best suited for a for answering a particular question. Additionally, these plug-ins rarely share information in a way that can be combined.

Cupid is a framework for seamlessly pulling information out of these plug-ins, performing analysis, and visualizing the results.

## Getting Cupid ##

Instructions for installing the Cupid Eclipse plug-in can be found on the [Installation](Installation.md) page. We recommend that you install the _nightly_ release, as there have been numerous significant improvements since the alpha release.

The Cupid update site includes features for improved integration with MercurialEclipse, EGit, and Mylyn. These features are _optional_, we suggest you install the features for whichever plug-ins you're using.

## Running Cupid (Walkthrough) ##

This section provides a walk-through of Cupid's features.

### Viewing Available Information Sources / Scripts (Capabilities) ###

When you restart Eclipse after the installation, a good first step is to view the list of available Cupid capabilities. Capabilities are scripts / components / methods that take an input and produce an output. To view the list of available capabilities, open the "Bulletin Board View" by going to `Window -> Show View -> Other... -> Cupid` and selecting "Cupid Capabilities".

Each capability has a name, description, one or more inputs, one or more options (optional inputs), and one or more outputs. For example, the "Last Modified" capability has the the description "The file's last modified date". Unsurprisingly, it has the input type `IFile` and the output type `Date`. The list of capabilities will differ depending on which Cupid features you installed.

### Viewing Contextual Information with the Selection Inspector ###

To see the "Last Modified" capability in action, we'll open the "Selection Inspector" which displays information about whatever table/tree item you select in Eclipse. To do so, go to `Window -> Show View -> Other... -> Cupid` and select "Selection Inspector".

With the Selection Inspector window open, select a file from the Package Explorer (e.g., a Java file). You'll see multiple rows with the name of a capability and a value, similar to what you see when using the expression explorer in the Eclipse debugger. The first row called "Selected Object" contains the object information for the selected object --- you can expand the rows to see the object's fields and getter methods. Clicking a row will show the associated text, as when debugging. Since you've clicked a compilation unit, you will see a row for the "Last Modified" capability, which shows the date that the file was last modified.

The Selection Inspect shows single input capabilities that are compatible with the selection; a capability is compatible with a selection if the selection is a subclass of the capability's input type, or if the selection can be adapted to the input type. Cupid includes many basic adapters, e.g., to convert from a compilation unit to a file. You can define additional adapters via the `edu.washington.cs.cupid.typeAdapters` extension point.

### Visualizing Information with Charts ###

Cupid provides both histograms and pie charts for visualizing information. As with the Selection Inspector, charts are based on the table/tree entries you select in Eclipse.

Suppose you want to chart the day of the week each file was modified. Open the Bulletin Board View (i.e., the list of available capabilities), right-click the row for the "Last Modified" capability and select "Create Pie Chart". This will open the Pie Chart View. Select the `getDay` entry from the outputs drop-down. Now select a few Java files --- the pie chart will show the days when the files were modified (with 0 corresponding to Sunday).

### Highlighting Information with Conditional Formatting Rules ###

Conditional formatting rules are rules that determine when the default coloring / font for a table or tree entry should be overridden. Since you define the rules for a particular data type, and not for a particular view, these are helpful for highlighting information throughout Eclipse. To define a rule, you'll select an input type, a capability (optional), and write a Java snippet that returns either `true` or `false`.

To get started with formatting, let's define a rule to highlight files that were last modified on a particular date.

The quickest way to create a rule is to right-click the element(s) you want to create a rule for and select `Cupid -> Create Formatting Rule`.
Do this for Java file. In the Wizard that opens, select the `IFile` type from the drop-down since we'd like the rule to apply to all files, and not just Java compilation units. Next, select the "Last Modified" capability.

The next step is to define the predicate. The `val` variable in the code snippet the output of the capability (or the object, if not capability is selected). Auto-completion should work when defining a rule, just as when writing Java code. Let's specify that we want to highlight files last modified on March 4th (or another day of your choosing):

```
return val.getMonth() == 2 && val.getDate() == 4
```

Clicking the "Next" button will take you to the formatting definition page. Enter a rule for the name (or keep the default name). Let's highlight these files in yellow, so click the Background Color button and select yellow. Click "Finish" to complete creating the rule.

Now files last modified on the date you specified will be highlighted (in some cases you have to re-expand the nodes in the tree to have the new formatting rules take effect).