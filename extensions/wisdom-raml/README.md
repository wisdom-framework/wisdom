# wisdom-raml 

The wisdom-raml project contains two projects: 
- A wisdom maven plugin that allows to create a [raml](http://raml.org/) specification from a wisdom controller. 
- A wisdom monitor extension that create a raml api console for each raml files present in `/assets/raml`. The monitor extension wrapped the raml [api-console](https://github.com/mulesoft/api-console).

## Install 

Simply add the following configuration to your project.

### The plugin

```xml
<!-- the plugin -->
<plugin>
  <groupId>org.wisdom-framework</groupId>
  <artifactId>wisdom-raml-maven-plugin</artifactId>
  <version>${wisdom.version}</version>
  <executions>
    <execution>
      <id>create-raml-spec</id>
        <goals>
          <goal>create-raml</goal>
        </goals>
      </execution>
    </executions>
</plugin>
```

You can configure the base uri via the `baseUri` configuration property, default is: http://localhost:9000.

### The monitor extension
```xml
<dependency>
  <groupId>org.wisdom-framework</groupId>
  <artifactId>wisdom-raml-monitor-console</artifactId>
  <version>${wisdom.version}</version>
</dependency>
```
### Validation Constraints 

In addition to the Wisdom annotations, the raml plugin also support some of the [validation constraints](https://docs.oracle.com/javaee/7/api/javax/validation/constraints/package-summary.html) on the route method parameter.

For now it supports : `@NotNull` `@Min` `@Max`. 

### Extra 

The wisdom-raml projects supports several custom javadoc type. 

tag | value sample | multiple | description
----|--------------|----------|-------------
@body.sample | `{"name" : "nobody"} ` | yes | body content sample 
