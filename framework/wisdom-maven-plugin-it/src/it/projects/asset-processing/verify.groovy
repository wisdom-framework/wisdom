import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.wisdom.framework.maven.integration.BuiltProject;
import static org.assertj.core.api.Assertions.assertThat;

System.out.println("Checking " + basedir)
def project = new BuiltProject(basedir)

assertThat(project.wasBuilt()).isTrue()
project.assertErrorFreeLog()

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

// -- Check coffee script
def output = new File(project.target(), "classes/assets/coffee/math.js");
assertThat(output).isFile();
output = new File(project.target(), "classes/assets/coffee/math.js.map");
assertThat(output).isFile();
// No aggregation here.

// -- Check typescript
output = new File(project.target(), "classes/assets/ts/Animal.js");
assertThat(output).isFile();
def animal = FileUtils.readFileToString(output);
assertThat(animal).doesNotContain("A simple typescript class").contains("sam");

// -- Check less
output = new File(project.target(), "classes/assets/less/main.css");
assertThat(output).isFile();

// -- Check CSS aggregation
output = new File(project.target(), "classes/assets/acme-project-min.css");
assertThat(output).isFile();
def content = FileUtils.readFileToString(output);
assertThat(content).contains("p{color:red}#header{font-size:large}").doesNotContain("body{font-size:small}");

output = new File(project.target(), "classes/assets/out/min.css");
assertThat(output).isFile();
content = FileUtils.readFileToString(output);
assertThat(content).contains("p{color:red}#header{font-size:large}").contains("body{font-size:small}")
        .doesNotContain(".h1,h1{font-size:30px}");

// -- Check JS aggregation
output = new File(project.target(), "classes/assets/math-min.js");
assertThat(output).isFile();
content = FileUtils.readFileToString(output);
assertThat(content).contains("var math;math={root:Math.sqrt")
        .contains("function log(n){return Math.log(n)};")

// -- Check un-filtered extensions
output = new File(project.target(), "classes/assets/unfiltered/stuff.nf");
assertThat(output).isFile();
content = FileUtils.readFileToString(output);
assertThat(content).contains("project.version").doesNotContain("1.0");

// -- WebJar packaging
output = new File(project.target(), project.artifactName + "-webjar.jar");
assertThat(output).isFile();

// -- Check that the .nf file contained in the configuration directory is not filtered
output = new File(project.target(), "wisdom/conf/stuff.nf");
assertThat(output).isFile();
content = FileUtils.readFileToString(output);
assertThat(content).contains("project.version").doesNotContain("1.0");

return true;

