import org.wisdom.maven.integration.BuiltProject;
import static org.assertj.core.api.Assertions.assertThat;

System.out.println("Checking " + basedir);
def project = new BuiltProject(basedir);

assertThat(project.wasBuilt()).isTrue();
project.assertErrorFreeLog();

// ---- Check Artifacts ----
assertThat(project.target()).isDirectory();
// Bundle
assertThat(new File(project.target(), BuiltProject.ARTIFACT_ID + "-" + BuiltProject.VERSION + ".jar")).isFile();
// Distribution
assertThat(new File(project.target(), BuiltProject.ARTIFACT_ID + "-" + BuiltProject.VERSION + ".zip")).isFile();

// ---- Wisdom Runtime ----
File wisdomRootDirectory = new File(project.target(), "wisdom");
assertThat(wisdomRootDirectory).isDirectory();
assertThat(new File(wisdomRootDirectory,
        "application/" + BuiltProject.ARTIFACT_ID + "-" + BuiltProject.VERSION + ".jar")).isFile();

// ---- Webjars copied ----
assertThat(new File(wisdomRootDirectory, "application/jquery-2.1.0-2.jar")).isFile();
assertThat(new File(wisdomRootDirectory, "application/bootstrap-3.1.1.jar")).isFile();

// ---- Optipng and jpegtran ----
project.assertContainedInLog("Optimizing " + project.target().getAbsolutePath()
        + "/it/projects/new-project/target/classes/assets/cat.jpg")
project.assertContainedInLog("Optimizing " + project.target().getAbsolutePath()
        + "/it/projects/new-project/target/classes/assets/owl-small.png")

