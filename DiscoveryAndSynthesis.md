

# Introduction #

Cupid is an ecosystem for discovering, synthesizing (transforming), and visualizing information in Eclipse. Information synthesis in Cupid is based on _capabilities_. Capabilities are services (functions) that, given input(s), tell Eclipse how to produce output(s).
Cupid provides context menus and wizards for creating new information on-the-fly in Eclipse; plug-in authors can write plug-ins that expose capabilities to Cupid using Cupid's extension point schema.

# Capability Bulletin Board #

Cupid's Capability Bulletin Board lists all of the capabilities that are available, including their description, input type(s), output type(s).

To open the Capability Bulletin Board, use the Eclipse menu to access the Cupid views `Window -> Show View -> Other... -> Cupid` and select "Cupid Capabilities".

# Extract Field Wizard #

The "extract field wizard" provides a quick and easy way to create a capability that returns the field or getter of an object.

There are two ways to open the extract field Wizard.
  * Click the "Extract Field Capability" button in the Eclipse toolbar
  * Right click a UI object (tree, list, or table item) in the Eclipse interface and select `Cupid -> Extract Capability`; this provides a convenient way to extract data from other plug-ins.

In the dialog that opens, select the method or field from the list and click "Finish" to create the capability.

The wizard lets you search for a type by clicking the "Select" button. See the [Type Search Setup](DiscoveryAndSynthesis#Type_Search_Setup.md) section for how to properly setup Eclipse's type search dialog for searching types contributed by plug-ins.

# Pipeline Wizard #

The pipeline wizard provides a quick way of pipelining (linking) multiple capabilities; To open the pipeline wizard, click the "Create New Capability" button in the Eclipse toolbar.

The wizard contains three parts:
  1. Meta information: provide a name and description for the pipeline (the pipeline's unique id is automatically generated)
  1. Available capabilities: the list of available capabilities to pipeline; if the result of a capability has fields or getters, these will be displayed as child nodes for the capability in the tree.
  1. Capability pipeline: the current capability pipeline; lists the name, input type, and output type for each component capability.

To add a capability to the pipeline, double click the node in the "Available Capabilities" tree. If the pipeline contains component capabilities, capabilities that can be attached to the end of the pipeline are shown in black; capabilities that cannot be attached to the end of the pipeline are shown in red (their input type is incompatible with the last output in the pipeline).

To delete a capability from the pipeline, select the capability from the table and press the delete key.

# Mapping Wizard #

The mapping wizard lets you define a mapping between two datasets, by setting a source type or capability, a value generating capability, and a mapping function. The [mapping view](Visualizations#Mapping_View.md) visualization provides a convenient way to view the output of a mapping capability.

There are two ways to open the mapping wizard:
  * Click the "Mapping Wizard" button in the Eclipse toolbar
  * Right click a UI object (tree, list, or table item) in the Eclipse interface and select `Cupid -> Create Mapping`

## Map Key ##

The map key defines a key set for the mapping; the map key(s) can either be a single `Object` or the output of a capability (`Capability Output`).

## Map Value Generator ##

The map value generator is the capability that provides the set of possible outputs for each key. Each key can be mapped to multiple outputs in the map value set.

When the map key type is `Object`, the value generator must not require any input; when the map key type is `Capability Output`, the value generator capability input type must be compatible with the key generator input type.

## Mapping Function ##

The mapping function is a predicate the indicates whether or not a map key is linked to value in the map value set. By default, the keys and values themselves are compared for equality. However, in most cases, the keys and values should be linked by fields or getters. After you've selected the map key and map value, you can choose the fields or getters to link by using the combobox.

## Example Mapping: Hg Author -> Commits ##

An example mapping is to map each Hg committer to their commits. This mapping can be created using the following steps:

  1. Create a key set of Hg authors
    * Select the `Capability Output` key type
    * Expand the `Hg Log` capability node in the map key tree
    * Select `[ getAuthor ]`
  1. Create a value set of Hg commits
    * Select the `Hg Log` capability in the map values tree
  1. Link between the author and the commit author
    * Select `getAuthor` from the combobox in the mapping function editor; it will read: "Where `<Value>` of key equals `getAuthor` of value"

# Scripting #

In some cases, wizards may not provide enough functionality. Cupid's scripting feature lets you script custom data transformations using Java.

The first time Eclipse runs with the scripting feature installed, Cupid will create a new Java project called `Cupid` in your workspace. The scripting plugin watches the project, dynamically loading capability scripts that you define.

## Creating a New Capability Script ##

Cupid provides a wizard for creating new scripts and properly updating the Cupid project's references. There are three ways to open the wizard:

  * Click the "Java Capability" button in the Eclipse toolbar
  * From the Eclipse menu, select `File -> New -> Other`. Then select the `Cupid -> Java Capability` entry from the New File dialog.
  * Right click a list or tree item in the Eclipse interface and click `Cupid -> Create Script`.

The wizard lets you choose a name, description, input, and output types for your new capability. To choose the input and output types, click the "Select" buttons. See the [Type Search Setup](DiscoveryAndSynthesis#Type_Search_Setup.md) section for how to properly setup Eclipse's type search dialog for searching types contributed by plugins.

When you click "Finish", Cupid will create a skeleton class for the capability and open a Java editor.

**Note: you currently must use the type search dialog for the Cupid project's class path to be properly updated.**

## Writing a Capability Script ##

Instructions for writing a capability script are available on the WritingCapabilities page.

# Type Search Setup #

By default, Eclipse's type search searches the types provided by and referenced by projects in the workspace. When creating capabilities in Cupid, we'd like the search to also include the types contributed by the currently installed plug-ins. To add these types to the search scope:

  1. Open the `Plug-in Development` preference page
  1. Ensure the "Include all plug-ins from  target in  Java search" option is selected.

Note that including all plug-ins in the search scope may slow down type search; therefore, you may wish to
leave this feature disabled when you are not searching for new types to create capabilities (e.g., with the scripting wizard).