

# Introduction #

Cupid comes packaged with multiple views for conveniently visualizing information in Eclipse; additionally, Cupid integrates with Eclipse's UI (the Standard Widget Toolkit) to provide additional visualization in plug-ins and Eclipse itself.

# Views #

Cupid provides the following views, accessible under `Window -> Show View -> Other... -> Cupid`:

  * The [inspector view](Visualizations#Selection_Inspector_View.md) shows the output of capabilities that apply to the item currently selected in the workbench (e.g., a compilation unit).

  * The [mapping view](Visualizations#Mapping_View.md) shows the relationship between two sets of data, typically created with the MappingWizard

  * The [chart views](Visualizations#Chart_View.md)  chart the output of a capability. Like the inspector view, the chart view supports outputting the result for the current workbench selection. Currently Cupid supports histograms and piecharts via [JFreeChart](http://www.jfree.org/jfreechart/); boxplot and bar chart support is planned in the future.

## Selection Inspector View ##

The selection inspector view computes capabilities that apply to the current IDE selection (currently only structural selections, e.g., table, tree, and list selections). Much like Eclipse's expression and watch view in the debugger, you can view the fields and getter methods for the returned value by expanding the nodes in the tree. Clicking on an item in the tree shows the `toString()` value for that object.

To hide a capability from the selection inspector, un-check the capability on the `Cupid -> Selection Inspector` preference page.

## Mapping View ##

The mapping view shows a mapping between to sets of data, typically created with the MappingWizard. To open the mapping view for a capability, right-click the capability in the Capability Bulletin Board and click `Show Mapping`.

## Chart View ##

Chart views chart the output of a capability for the current workbench selection; currently histograms and pie charts are supported. To open the chart view for a capability, right-click the capability in the Capability Bulletin Board. If the capability has multiple outputs that are compatible with the chart, you can select the output to display from the drop-down on the chart view.

The following chart views and capability types are currently supported:
  * Histogram: a capability producing a `Number` result (e.g., `Integer` or `Double`)
  * Pie Chart: any capability

# Conditional Formatting #

Most plugins use Eclipse's Standard Widget Toolkit (SWT) widgets. Cupid's conditional formatting feature lets you write rules that override these widget's item formatting according to capability-based rules; _currently only trees and tables are supported_.

To create a formatting rule, select one or more objects in a view, right-click, and select `Cupid -> Create Formatting Rule` from the context menu that appears. In the wizard that appears, you specify:

  1. The input type for the rule (the supertypes of the type you selected are listed)
  1. An (optional) transformer capability
  1. A predicate (based on the input type, or the output type of the capability you selected). The code should return a `boolean` value.
  1. The formatting overrides

Formatting rules are managed on the `Cupid -> Conditional Formatting` preference page. To add a new rule, click the "Add" button. To edit a rule, select the rule from the list and click edit.
To enable a rule, ensure that the checkbox for the capability in the list is checked. You can enable or disable multiple rules simultaneously by selecting the rules and clicking "Enable" or "Disable", respectively.

# Problem Markers #

Cupid provides a hook for creating Eclipse problem markers (the mechanism Eclipse uses to mark compiler warnings, compiler errors, and TODOs). Currently there are no UI wizards for producing problem markers, however developers can look at the source for the continuous testing plug-in for example usage.