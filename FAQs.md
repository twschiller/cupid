# Frequently Asked Questions #

## Who develops Cupid? ##

Cupid is developed at the [University of Washington](http://cs.washington.edu) by [Todd Schiller](http://toddschiller.com).

## How can I contribute? ##

  * Write and share new capabilities
  * Develop new Cupid-compatible visualizations
  * Expose more plug-in features as Cupid capabilities

## How does Cupid work? ##

The core of Cupid is a futures execution engine built on top of the Eclipse jobs API.

## How can I make my plugin work with Cupid? ##

To make your plugin work with Cupid, wrap some of your plugin
capabilities using the `ICapability` interface, then publish the
capabilities using the `ICapabilityPublish` extension point. If your plugin works
using Eclipse's `IResource` hierarchy, the process should be
straight-forward. Please contact us with any questions.

## Can Cupid handle capabilties that modify resources? ##

Currently, capabilities must be "pure," that is not modify any workspace/project state. In the future we hope to integrate with the [Solstice project](https://bitbucket.org/kivancmuslu/solstice/wiki/Home) to support impure plug-ins.