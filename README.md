wisdom ~ Web Is Dynamic and Modular
=======

Building Wisdom
===============

Because Wisdom is built on top of Wisdom, building has to be done in two steps. First,
the main API and the Wisdom Maven Plugin are built. Then, service implementations and technical feature are processed
 and aggregated inside a `base runtime`. Finally, a third step is building advanced technical services and support
 and compose the `Wisdom Runtime`.

    mvn clean install -Pbase
    mvn clean install

_Why having two runtime projects?_ Because Wisdom services are tested against Wisdom itself. So to avoid loops,
we test them on the base runtime.

----------------

1- Install JDK 7

2- Install Maven 3.1.1, if you need help to upgrade it 'http://myjeeva.com/how-to-do-maven-upgrade-in-mac-os-x.html'

3- and configure it to use JDK7 'http://www.jayway.com/2013/03/08/configuring-maven-to-use-java-7-on-mac-os-x/'

4- Clone wisdom repo

5- Clone https://github.com/ow2-chameleon/core to ./chameleon/runtime/

6- 'mvn clean install' in chameleon/runtime

(Step 7-8 can be skipped if you don't want to run **wisdom-test**. However, you'll have to comment **wisdom-test** module in the root pom)

7- Clone https://github.com/ow2-chameleon/osgi-testing-helpers to ./chameleon/test/

8- 'mvn clean install' in chameleon/test

9- 'mvn clean install' at project root (**samples** module should be commented for the first 'mvn clean install')

10- go to ./samples and run 'mvn clean wisdom:wisdom-maven-plugin:1.0-SNAPSHOT:run'

11- that's all folks go to 'http://localhost:9000/samples'
