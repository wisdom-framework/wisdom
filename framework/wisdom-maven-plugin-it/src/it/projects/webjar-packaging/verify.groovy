import org.wisdom.framework.maven.integration.BuiltProject;
import static org.assertj.core.api.Assertions.assertThat;

System.out.println("Checking " + basedir);
def project = new BuiltProject(basedir);

assertThat(project.wasBuilt()).isTrue();

// We only check for the build success, as the second project has integration tests checking the right behavior.

project.assertErrorFreeLog();

return true;