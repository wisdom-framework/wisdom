import org.wisdom.framework.maven.integration.BuiltProject;
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
assertThat(project.wisdom()).isDirectory();
def bundle = new File(project.app(), project.bundleName);
assertThat(bundle).isFile();

// ---- Webjars copied ----
assertThat(new File(project.app(), "jquery-2.1.0-2.jar")).isFile();
assertThat(new File(project.app(), "bootstrap-3.1.1.jar")).isFile();

// ---- Webjars unpacked ----
def webjars = new File(project.target(), "webjars");
def libs = new File(project.target(), "wisdom/assets/libs");
def jquery = new File(webjars, "jquery");
def bootstrap = new File(webjars, "bootstrap");

// jquery
assertThat(new File(jquery, "jquery.js")).isFile();
assertThat(new File(jquery, "jquery.min.js")).isFile();
assertThat(new File(jquery, "jquery.min.map")).isFile();

// bootstrap
assertThat(new File(bootstrap, "css/bootstrap.css")).isFile();
assertThat(new File(bootstrap, "css/bootstrap.min.css")).isFile();
assertThat(new File(bootstrap, "js/bootstrap.js")).isFile();
assertThat(new File(bootstrap, "js/bootstrap.min.js")).isFile();

// acorn is in the 'provided' scope, it must not be unpacked in target/webjars, but in target/wisdom/assets/libs.
assertThat(new File(webjars, "acorn")).doesNotExist();
assertThat(new File(libs, "acorn")).isDirectory();

return true;
