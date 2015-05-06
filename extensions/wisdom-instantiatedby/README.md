# @InstantiatedBy support

This extension to Wisdom Framework adds an annotation `@org.wisdom.framework.intances.api.InstantiatedBy`. This 
annotation _replaces_ the iPOJO `@Instantiate` annotation and let you define which configuration admin configuration 
_instantiates_ the instance.
 
## Rationale and Example
 
Let's see an example to be a bit more clear. Generally, you create an iPOJO instance like that:
 
```
@Component
@Provides
@Instantiate
public class Acme {
    //...
}
```

This creates the instance without configuration. 

To pass a configuration, you creates a `cfg` file containing the configuration. The name of the file must be the 
fully-qualified name of the class appended with `-` and an id (for instance `org.foo.Acme-default.cfg`):

```
@Component
@Provides
// Instantiated from the cfg file.
public class Acme {
    //...
}
```

However, on limitation of this pattern the the inability to create several instances from the same `cfg` file. This 
is where the `@InstantiatedBy` annotation comes into the game.

Let's imagine you have two components A and B than need to be controlled from the same configuration (i.e. `cfg` 
file). You can do it with `@InstantiatedBy`:
  
```
@Component
@Provides
@InstantiatedBy("com.acme")
public class A {

}

// and

@Component
@Provides
@InstantiatedBy("com.acme")
public class B {

}
```

The value given to the `@InstantiatedBy` is the name of the configuration admin configuration that create the 
instances. For instance in the previous example it can be `cfg` files:

* `com.acme.cfg` - this is a regular config admin configuration
* `com.acme-default.cfg` - this is factory configuration

If you create several factory configurations (with different name), it instantiates another times the bound factories.

## Creating, Updating and Deleting
 
Thanks to this centralization, you can create a set of instances, update this set of instances and delete the set of 
instances by manipulating only one configuration admin configuration (or `cfg` file).

## Links
 
`cfg` file mechanism is described here: http://ow2-chameleon.github.io/core/snapshot/chameleon-directory-monitor.html#/configuration_deployment 
 
 
 


 
 