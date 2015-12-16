import org.wisdom.framework.maven.integration.BuiltProject;
import static org.assertj.core.api.Assertions.assertThat;

System.out.println("Checking " + basedir)
def project = new BuiltProject(basedir)

assertThat(project.wasBuilt()).isTrue()

// ---- Check Artifacts ----
assertThat(project.target()).isDirectory()
// Bundle
assertThat(new File(project.target(), project.bundleArtifactName)).isFile()
// Distribution
assertThat(new File(project.target(), project.distributionArtifactName)).isFile()

// ---- Wisdom Runtime ----
assertThat(project.wisdom()).isDirectory()
def bundle = new File(project.app(), project.bundleName)
assertThat(bundle).isFile()

// ---- Webjars copied ----
assertThat(new File(project.wisdom(), "application/jquery-2.1.0-2.jar")).isFile()
assertThat(new File(project.wisdom(), "application/bootstrap-3.1.1.jar")).isFile()

// ---- Optipng and jpegtran ----
project.assertContainedInLog(":optimize-images")

// ---- Unit Test ---
project.assertContainedInLog("Running sample.UnitTest")
project.assertContainedInLog("Tests run: 2, Failures: 0, Errors: 0, Skipped: 0")

// Integration-Test
project.assertContainedInLog("Running sample.BlackBoxIT")
project.assertContainedInLog("Running sample.FluentLeniumIT")
project.assertContainedInLog("Running sample.InContainerIT")

return true;

