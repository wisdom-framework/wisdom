wisdom
===================


1- Install JDK 7

2- Install Maven 3.1.1, if you need help to upgrade it 'http://myjeeva.com/how-to-do-maven-upgrade-in-mac-os-x.html'

3- and configure it to use JDK7 'http://www.jayway.com/2013/03/08/configuring-maven-to-use-java-7-on-mac-os-x/'

4- Clone wisdom repo

5- Clone https://github.com/ow2-chameleon/core to ./chamelelon/runtime/

6- 'mvn clean install' in chameleon/runtime

7- 'mvn clean install' at project root

8- go to ./samples and run 'mvn clean wisdom:wisdom-maven-pgin:1.0-SNAPSHOT:run'

9- that's all folks go to 'http://localhost:9000/'
