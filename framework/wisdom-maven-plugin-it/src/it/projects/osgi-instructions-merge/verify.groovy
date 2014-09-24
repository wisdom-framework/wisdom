import org.wisdom.framework.maven.integration.BuiltProject

import java.util.jar.JarFile;

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

//---- Check bundle content ----
def bundle = new JarFile(new File(project.target(), project.bundleArtifactName))

// 1) Check bundle manifest
def manifest = bundle.getManifest().mainAttributes;
assertThat(manifest.getValue("Bundle-SymbolicName")).isEqualTo("org.wisdom.framework.test.acme.project")
assertThat(manifest.getValue("Bundle-Version")).isEqualTo("1.0.0.testing")
assertThat(manifest.getValue("Include-Resource")).contains("assets/cat.jpg=target/classes/assets/cat.jpg")

// Import-Package: * has computed the set of imported packages.
assertThat(manifest.getValue("Import-Package")).contains("org.wisdom.api.annotations;version=")

// foo is exported (as specified in the osgi.bnd file)
assertThat(manifest.getValue("Export-Package")).contains("foo")
assertThat(manifest.containsKey("iPOJO-Components"))

// 2) Check content
assertThat(bundle.getEntry("foo/WelcomeController2.class")).isNotNull()
assertThat(bundle.getEntry("sample/WelcomeController.class")).isNotNull()
assertThat(bundle.getEntry("assets/cat.jpg")).isNotNull()

return true;

