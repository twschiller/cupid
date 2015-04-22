

# Introduction #

Cupid capabilities are services that, given an input, produce a job that calculates the result. This page described how to write a capability.

Each capability implements the `ICapability` interface. This interface provides metadata information (e.g., name, description), input type(s) information, output type(s) information, and a method for creating computations for an input.

For capabilities that take a single input and produce a single output (linear capabilities), Cupid provides convenience base classes: `LinearCapability` and `GenericLinearCapability`. Cupid provides the `AbstractBaseCapability` base class for capabilities with more than one input and/or output.

# Capability Metadata #

Each capability provides information about itself via the `ICapability` interface.

  * Name: a human-readable name for the capability; it helps if these are unique
  * Description: a human-readable description of the capability
  * Inputs and Options: named inputs with their corresponding types. Options are inputs with a default value.
  * Outputs: named outputs with their corresponding types.
  * Flags: a capability can have one or more of the following flags:
    * Transient: whether of not the result of the capability can be cached; in general, capabilities that access external resources (e.g., a version control repository) should be marked as transient.
    * Pure: whether or not the capability modifies the workspace. Note: impure capabilities are not presently supported
    * Local: whether or not the capability result for the input is independent of other peer-values.


# Input and Output Type Tokens #

Because Java removes generic type information during compilation ([type erasure](http://docs.oracle.com/javase/tutorial/java/generics/erasure.html)), Cupid uses [Google Guava's](https://code.google.com/p/guava-libraries/) `TypeToken` class for tracking information about Capability types. Each `IParameter` and `IOutput` defines a method `getType()` that returns the `TypeToken` for the input or output, respectively.

## Simple Types ##

To create a `TypeToken` for a non-generic class, use the `TypeToken.of(...)` method:

```
    TypeToken<String> type = TypeToken.of(String.class);
```

## Types with Generics ##

For generic classes with no type variables, a `TypeToken` can be created with the constructor:

```
   TypeToken<List<String>> type = new TypeToken<List<String>>() {};
```

# Linear Capabilities #

Linear capabilities take a single input and produce a single output;
most of the capabilities used with Cupid are linear. Cupid provides
the `LinearCapability` class as a bass class for these
capabilities.

The constructor for the superclass is passed the metadata for the capability:

```
public MyCapability() {
    super("MyCapabilityName", "My Capability Description",
        LocalTask.class, Integer.class, // input and output type
        Flag.PURE);
}	
```

The `getJob` method produces a computation for a given input, for example:

```
@Override
public LinearJob<IResource, Integer> getJob(final IResource input) {
    return new LinearJob<IResource, Integer>(this, input){
        @Override
        protected LinearStatus<Integer> run(final IProgressMonitor monitor) {
            try {
                monitor.beginTask("My Test Script", 100);                                 

                Integer result = ...; // perform computation
                return LinearStatus.makeOk(getCapability(), result);
            } catch (Exception e) {
                return LinearStatus.<Integer>makeError(e); // must explicitly write the normal return type
            } finally {
                monitor.done();
            }
        }
    };
}
```

Notice that the method returns a `LinearJob` which produces a
`LinearStatus` when run. For methods with more than one input / ouput,
the `getJob` method will return a `CapabilityJob` producing a
`CapabilityStatus` when run (see below).

## Parameterized Types ##

In some cases, a capability will be generic, with it's output type
depending on its input type. Cupid provides the
`GenericLinearCapability` class provides a base class for defining
these capabilities. These capabilities define a `getInputType` and
`getOutputType` method to properly return the generic type token.

To produce a `TypeToken` for a generic capability's parameter or
return type, the capability `Class` is provided to the TypeToken
constructor via the `getClass()` method:

```
   TypeToken<List<V>> type = new TypeToken<List<V>>(getClass()){};
```

For example, the `Most Frequent` capability packaged with Cupid which
returns the most frequent element in a list has the following
type signature:

```
public final class MostFrequent<V> extends GenericLinearCapability<List<V>, V> {
	
    // constructor and other methods are not shown

    @Override
    public TypeToken<List<V>> getInputType() {
        return new TypeToken<List<V>>(getClass()) {};
    }

    @Override
    public TypeToken<V> getOutputType() {
        return new TypeToken<V>(getClass()) {};
    }
}
```

## Immediately Returning a Value or Exception ##

In some cases, you may be able to safely and quickly precompute the
capability's result. In these cases, you can return an `ImmediateJob`
which immediately produces a precomputed value or exception.

```
@Override
public CapabilityJob<InputType, OutputType> getJob(final InputType input) {
        Output result = ...; // precompute value
	return new ImmediateJob<Input,Type OutputType>(this, input, result);
}
```

You should use `ImmediateJob`s sparingly -- the value of the input may
change between the time that the capability job is created and the
capability job runs. In general, `ImmediateJob` are ok to use for
small predicates that are part of larger pipelines (because the
pipeline forces the job to be run immediately after it is created).

# Performing Computation (Jobs) #

The `ICapability` interface provides a `getJob(input)` method that returns a `CapabilityJob` that calculates the output of the capability. The Cupid execution engine handles the scheduling of the job, additionally caching results.

A `CapabilityJob` provides a method `run(...)` which computes the result and returns one of three possible `CapabilityStatus`es:

  * `CapabilityStatus.makeOk(ICapabilityOutputs)`: indicates that the result was computed successfully
  * `CapabilityStatus.makeError(Throwable)`: indicates that an exception was thrown
  * `CapabilityStatus.makeCancelled()`: indicates that the job was cancelled

In general, a `getJob(...)` implementation will have the following form:

```

@Override
public CapabilityJob<CapabilityExample> getJob(final InputType input) {
     return new CapabilityJob<CapabilityExample>(this, input){
          @Override
	  protected CapabilityStatus run(final IProgressMonitor monitor) {
               try {
                    monitor.beginTask("Task Name", 1 /* amount of work */);

		    Object input1 = input.getValueArgument(PARAM_DEF1);
                    /** other inputs **/
					
                    /** perform computation **/
		
		    OutputBuilder result = new OutputBuilder(OutputExample.this);
		    result.add(OUT_DEF1, result1);
		    /** other outputs **/

                    return CapabilityStatus.makeOk(out.getOutputs(result));

               } catch (Exception ex) {
                   return CapabilityStatus.makeError(ex);

               } finally {
                    monitor.done();
               }
          }
     }
}
```

, where parameters and outputs are defined a class constants, e.g.:

```
public static final IParameter<IResource> PARAM_DEF1 = new Parameter<IResource>("Input", IResource.class);
public static final OptionalParameter<Integer> OPTION_DEF1= new OptionalParameter<Integer>("Option", Integer.class, 0);
public static final Output<String> OUT_DEF1 = new Output<String>("Output", TypeToken.of(String.class));		
```

# Progress Monitors #

Cupid integrates with Eclipse's progress monitor mechanism to display progress information to users. The `run(...)` method for each `CapabilityJobs` produced by a capability take a progress monitor as input. Some quick tips for properly using progress monitors are shown below; an detailed article about how to properly use progress monitors is available at http://www.eclipse.org/articles/Article-Progress-Monitors/article.html.

## Starting and Stopping Monitors ##

The `run(...)` method should start the `IProgressMonitor` by calling `beginTask(...)`; before returning, the method should call the `done()` method. A clean way to implement this is to wrap the whole job in a try-finally block.

```
try {
       monitor.beginTask("Task Name", 1 /* amount of work */);
       // computation       
} finally {
       monitor.done();
}
```

## Keeping Track of Progress ##

The call to `beginTask` includes the total amount of work the job will perform. Each time a unit(s) of work is performed, the work should be recorded with the progress monitor's `worked(...)` function.  For example, if the job performs a calculation for each element of a list, progress might be updated after each element is processed:

```
// List<Object> input;

monitor.beginTask("Task Name", input.length());

for (Object element : input){
    // calculation involving element
    monitor.worked(1);
}
```

## Monitoring Subtasks ##

Some library functions accept a progress monitor argument. When calling such functions inside of a job, _do not pass in the job's progress monitor_. Instead, create a `SubProgressMonitor`. The `SubProgressMonitor` includes an argument for the amount of work allocated to it by the main progress monitor. For example:

```
monitor.beginTask("My Capability Job", 100);
foo(x, y, new SubProgressMonitor("Subtask Name", 25)); // allocate 25 work units for this subtask
bar(x, y, new SubProgressMonitor("Subtask Name", 75)); // allocate 75 work units for this subtask
```

# Supporting Job Cancellation #

To support cancellation either by a user, or the Cupid calculation engine, jobs with multiple phases should override the [canceling()](http://help.eclipse.org/helios/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/jobs/Job.html#canceling()) method.