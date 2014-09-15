import org.wisdom.framework.maven.integration.BuiltProject;
import static org.assertj.core.api.Assertions.assertThat;

System.out.println("Checking " + basedir);
def project = new BuiltProject(basedir);

assertThat(project.wasBuilt()).isTrue();
project.assertErrorFreeLog();

// This test check #263 and some more resolution behavior

// p1 depends on X (syndication service), and Y (chat service)
// p2 depends on X and Z (shared preference service) (provided)
// p3 depends on p1 (test) and p2
// ensure that p3 has X in wisdom/application, but neither Y, Z or dependencies of Z.

def application = new File(basedir, "p3/target/wisdom/application");

// ensure that p2 is there, but not p1 (test dependency)
assertThat(new File(application, "org.wisdom.framework.test.acme.project.p2-1.0-testing.jar")).isFile();
assertThat(new File(application, "org.wisdom.framework.test.acme.project.p1-1.0-testing.jar")).doesNotExist();

// ensure the X is there (p2 -> X).
assertThat(new File(application, "org.ow2.chameleon.syndication.service-0.2.0.jar")).isFile();

// ensure that Y is not there (p1 -> Y, but p1 in test scope)
assertThat(new File(application, "org.ow2.chameleon.chat-1.0.0.jar")).doesNotExist();

// ensure that Z is not there nor one of its dependencies (p2 -> Z (provided))
assertThat(new File(application, "org.ow2.chameleon.sharedprefs.xml-implementation-0.2.0.jar")).doesNotExist();

return true;