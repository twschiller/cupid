<b><font size='5'>Examples and Use Cases</font></b>

This page contains a collection of examples of how to use Cupid. The approaches used in these examples can often be applied to perform other tasks. This page is organized by plug-in information source (e.g., Git, Mylyn, etc.).

**TIP: use your browser's search feature to help find relevant examples. Use Ctrl+F (Cmd+F on Mac) to open the search feature.**




---

# Version Control Examples #

<table>
<tr><td valign='top'>

<h2>Displaying the Most Frequent Committer (Author)</h2>

Let's tell Cupid to display the most frequent committer for a file in the Selection Inspector whenever we select the file.<br>
<br>
<ol><li>Open the Pipeline Wizard<br>
</li><li>Expand the "Git History" capability, and double-click the <code>[getAuthor]</code> entry to add the capability to the pipeline.<br>
</li><li>Add the "Most Frequent Element" capability<br>
</li><li>Name the capability "Most Frequent Committer", add description for the capability, and click <code>Finish</code>.</li></ol>

Open the Selection Inspector view (<code>Window -&gt; Show View -&gt; Other...</code>). Now, select a file to see the most frequent committer for the file.<br>
<br>
</td><td>
<img src='http://cupid.eclipselabs.org.codespot.com/hg.wiki/imgs/most-frequent-committer.png' />
</td></tr>
</table>

## Determining Which Files Have the Most Committers (Authors) ##

Let's create a capability to return the number of different committers to a file. Then, we'll create a report to determine which files have the most committers.

### Determining the number of committers to a file ###

First, create a pipeline that returns the number of commit authors for a file:

  1. Open the Pipeline Wizard
  1. Expand the "Git History" capability, and double-click to `[getAuthor]` entry to add the capability to the pipeline.
  1. Add the "Distinct (Remove Duplicate)" capability to the pipeline, this will remove any duplicate authors from the collection.
  1. Add the "Size / Count / Number of Elements" capability. This will return the number of distinct commit authors for the file.
  1. Name the pipeline "Number of Authors" and click `Finish`.

<table>
<tr><td valign='top'>

<h3>Reporting which file has the most committers</h3>

Next, use the report view to see which files have had the most committers.<br>
<br>
<ol><li>Open the "Cupid Capabilities" view<br>
</li><li>Locate the "Number of Authors" capability that you created in the <a href='UserStudyExamples#Determining_the_number_of_committers_to_a_file.md'>previous section</a>
</li><li>Right-click the capability and select <code>Create Report</code> from the context menu; the report view will open.</li></ol>

Now, select one or more files to view the number of authors for those files. Click the output column header to sort the files by the number of authors.<br>
</td><td>
<img src='http://cupid.eclipselabs.org.codespot.com/hg.wiki/imgs/authors-report.png' />
</td></tr>
</table>


---

# Mylyn Task Examples #

## Task Context ##

A task's context is the set of files, packages, and projects related to the task.

<table>
<tr><td valign='top'>

<h3>Viewing the number of projects in a task's context</h3>

This section describes how to create a pipeline that returns the number of projects referenced by a task (i.e., the number of projects in the task's context).<br>
<br>
<ol><li>Open the Pipeline Wizard<br>
</li><li>Type “project” in to the search box to find capabilities that work with projects.<br>
</li><li>Expand the Mylyn Task Context entry, and double-click the “getProject” entry. This creates a pipeline with a single component. The pipeline takes an input of type <code>ITask</code> and returns the list of <code>IProject</code>s in the task’s context.<br>
</li><li>Add the “Distinct (Remove Duplicates)” capability to the pipeline, which returns the set of unique elements for an input. Now the pipeline takes as input an <code>ITask</code> and returns a set of <code>IProject</code>s.<br>
</li><li>Add the “Size / Count / Number of Elements” capability to the pipeline, which returns number of elements in the input. The pipeline now has the signature <code>ITask -&gt; Integer</code>.<br>
</li><li>Name the pipeline “Number of Projects Referenced” and click “Finish”</li></ol>

Open the Selection Inspector view. Now, select a task in the Task list; the Selection Inspector will show the number of projects referenced by the task.<br>
<br>
</td><td>
<img src='http://cupid.eclipselabs.org.codespot.com/hg.wiki/imgs/project-reference-count.png' />
</td></tr>
</table>

### Reporting which tasks reference the most/fewest projects ###

This section describes how to use the  “Number of Projects Referenced” pipeline from the [previous section](UserStudyExamples#Viewing_the_number_of_projects_in_a_task's_context.md) to identify tasks that affect a large part of the project.

  1. Open the Cupid Capabilities view
  1. Locate the capability you created in the last section to return the number of projects in a task's context
  1. Right-click the capability and select `Create Report` from the context menu; this will open the report view

Now, select one or more tasks in the task list (hold down the Shift key to select multiple elements). The report will show the number of projects each task references. Click the column header for the output to sort the tasks by number of referenced projects.

<table>
<tr><td>

<h3>Listing tasks related to a project</h3>

This section describes how to create a pipeline that returns the tasks that have a particular project in their context.<br>
<br>
<ol><li>Open the Pipeline Wizard<br>
</li><li>Type "task" in the search box to find capabilities related to tasks<br>
</li><li>Add the "Tasks for Resource" capability, which takes an <code>IResource</code> and returns a list of <code>AbstractTasks</code>.<br>
</li><li>Add the "Distinct (Remove Duplicates)" capability, which will return a set of <code>AbstractTasks</code> produced by the previous capability (filtering out the duplicates)<br>
</li><li>Name the capability "Related Tasks" and select "Finish"</li></ol>

Open the Selection Inspector view. Select a project (or another resource) and the Selection Inspector will show a list of tasks referenced by the project.<br>
</td><td>
<img src='http://cupid.eclipselabs.org.codespot.com/hg.wiki/imgs/related-tasks-inspector.png' />
</td></tr>
</table>


## Viewing the Owner of a Task (the Developer Assigned to the Task) ##

When a developer is assigned to a task, they become the "owner" of the task. This section describes how to easily access and visualize ownership information for one or more tasks.

### Creating a capability that returns the owner of a task ###

  1. Open the Pipeline Wizard
  1. Expand the "Task (Identity)" capability and double-click the "getOwner" output to add the capability to the pipeline.
  1. Name the capability "Task Owner" and select "Finish"

### Viewing the owner of a task in the Selection Inspector ###

  1. Follow the directions in the [previous section](UserStudyExamples#Creating_a_capability_that_returns_the_owner_of_a_task.md) to create a capability "Task Owner" that returns the owner for a task
  1. Open the Selection Inspector View
  1. Click a task in the task list; the owner will be displayed for the "Task Owner" in the Selection Inspector

<table>
<tr>
<td valign='top'>

<h3>Viewing the relative frequency of owners with a Pie Chart</h3>

<ol><li>Follow the directions in the <a href='UserStudyExamples#Creating_a_capability_that_returns_the_owner_of_a_task.md'>previous section</a> to create a capability "Task Owner" that returns the owner for a task<br>
</li><li>Open the Cupid Capabilities view<br>
</li><li>Locate the "Task Owner" capability (e.g., by searching for <code>owner</code> in the search box)<br>
</li><li>Right-click the capability and select <code>Create Pie Chart</code> to open the pie chart view</li></ol>

Now, select one or more tasks in the task list; the Pie Chart will show the proportion of tasks assigned to each owner.<br>
</td><td>
<img src='http://cupid.eclipselabs.org.codespot.com/hg.wiki/imgs/owner-piechart.png' />
</td>
</tr></table>

### Finding all of the tasks owned by a developer ###

  1. Follow the directions in the [previous section](UserStudyExamples#Creating_a_capability_that_returns_the_owner_of_a_task.md) to create a capability "Task Owner" that returns the owner for a task
  1. Locate the "Task Owner" capability (e.g., by searching for `owner` in the search box).
  1. Right-click the capability and select `Create Report` to open the report view

Now, select one or more tasks in the task list; the Pie Chart will show the proportion of tasks assigned to each owner.


---

# Plug-in Mash-Up Examples #

## Highlighting Tasks Affected by the Last Commit ##

### Determining if a task was affected by the last commit ###

This section describes how to create a predicate capability that returns true if the input task was modified in the latest commit.

  1. Open the Pipeline Wizard
  1. Double-click the "Mylyn Task Context" capability to add it to the pipeline. The capability returns the context (a list of resources) for an input task.
  1. Select the "Git Modified?" capability. To select a particular revision or range of revisions, set the appropriate options.
  1. Double-click the "Git Modified?" capability to add it to the pipeline. Notice that this capability takes a single `IResource`. Cupid "lifts" the capability to work on each element in the list produced by the previous capability.
  1. Add the "Any (One or More)" capability. This will return true if the previous capability returned true for any of the list items.
  1. Name the pipeline "Task Affected by Last Commit?" and click "Finish"

### Reporting the affected tasks ###

Create a report for the "Task Affected by Last Commit?" capability and select the tasks of interest.

  1. Open the Capability View and locate the "Task Affected by Last Commit?" capability that you created in the [previous section](UserStudyExamples#Determining_if_a_task_was_affected_by_the_last_commit.md). This capability returns true if any file in the task's context was modified in the previous revision.
  1. Right-click the capability and select "Create Report"

Select one or more tasks to determine if they were affected by the last commit.

## Highlighting Stack Frames ##

In this example, we tell Cupid to highlight stack entries that correspond to files that we modified in the previous Mercurial revision.

### Stack frames modified in the last revision ###

Create a predicate capability that returns true if the resource corresponds to a stack frame was modified in the previous revision.

  1. Add the "Stack Frame Resource" capability to the pipeline, using the "Resource" output
  1. Add the "Hg Modified?" capability to the pipeline. In this case, we'll keep the "Revision" option as `-1` to indicate we want whether or not the resource was modified in the previous revision.
  1. Give the pipeline a name / description and click `OK`

### Stack frames for files in the context of the active task ###

Create a predicate capability that returns true if the resource corresponds to a resource (e.g., file) in the active tasks' context:

  1. Add the "Stack Frame Resource" capability to the pipeline, using the "Resource" output
  1. Add the "Mylyn In Active Task Context?" capability to the pipeline. The pipeline now returns true if the resource referenced by the stack frame was modified in the previous revision.
  1. Give the pipeline a name "Frame Related to Active Task?" and click `OK`

### Highlighting the stack frame ###

<font color='red'>Note: you do not need to use highlighting for the user study.</font>

Create a formatting rule for a stack frame using the predicate capability.

  1. When debugging, right click one of the stack frames and select `Cupid -> Create Formatting Rule` from the context menu
  1. In the formatting dialog, select `IJavaStackFrame` as the input type
  1. Select the predicate capability you create in one of the previous step. Since the capability returns a `boolean` value, there's no other logic to add.
  1. Click `Next` and define the formatting. For example, change the background to yellow.
  1. Click `Finish` to create the rule; you may need to collapse and re-expand the stack frame tree for the highlighting to appear.