# Wisdom-Framework ~ Web Is Dynamic and Modular

Main documentation is available at [wisdom-framework.org](http://wisdom-framework.org)

## Project structure

* `core` -> core modules building the `base-runtime`, it also contains the `wisdom-maven-plugin`.
* `framework` -> contains all technical services composing the `wisdom-runtime`.
* `extensions` -> contains Wisdom extensions used in this project. For example the asciidoc support is provided to
generate the documentation.
* `documentation` -> contains the documentation, samples and tutorials

## Building Wisdom

Building Wisdom is a two steps process. There are two reasons for that. First, Wisdom is built on Wisdom. Then, Maven
has a (big) limitation when having a reactor building an extension (a plugin extending the lifecycle) used by other
modules.

So to build Wisdom:

1. Gets the code using `git clone https://github.com/wisdom-framework/wisdom.git` (replace the url if you are using a
 fork)
2. Build Wisdom Core
````
mvn clean install -Pcore,\!framework,\!extensions,\!documentation
````
3. Build the rest of the framework
````
mvn clean install -P\!core,framework,extensions,documentation
````

That's it.

**IMPORTANT**: Wisdom requires JDK 7+ to be built.

### Skipping tests

Launch Maven with: `-DskipTests`.
