import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.wisdom.framework.maven.integration.BuiltProject

import java.security.MessageDigest
import java.util.jar.JarFile

import static org.assertj.core.api.Assertions.assertThat

def md5(byte[] s) {
    MessageDigest digest = MessageDigest.getInstance("MD5")
    digest.update(s);
    new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
}

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

// 2) Check content

// 2.1 - assets and processed files
assertThat(bundle.getEntry("assets/main.less")).isNotNull();
assertThat(bundle.getEntry("assets/my.coffee")).isNotNull();
assertThat(bundle.getEntry("assets/main.css")).isNotNull();
assertThat(bundle.getEntry("assets/my.js")).isNotNull();

// Check the coffee file as the comment are removed by CoffeeScript
def content = IOUtils.toString(bundle.getInputStream(bundle.getEntry("assets/my.coffee")));
assertThat(content).contains(BuiltProject.ARTIFACT_ID + " - " + BuiltProject.VERSION);

// 2.2 - images copied to img, not filtered
assertThat(bundle.getEntry("img/cat.jpg")).isNotNull();
assertThat(bundle.getEntry("img/owl-small.png")).isNotNull();

def md51 = md5(FileUtils.readFileToByteArray(new File(basedir, "target/classes/img/cat.jpg")));
def ref = md5(FileUtils.readFileToByteArray(new File(basedir, "src/main/images/cat.jpg")));
assertThat(md51).isEqualTo(ref);

// 2.3 - js

assertThat(bundle.getEntry("js/square.js")).isNotNull();
content = IOUtils.toString(bundle.getInputStream(bundle.getEntry("js/square.js")));
assertThat(content).contains(BuiltProject.ARTIFACT_ID + " - " + BuiltProject.VERSION);

//---- Check that the instances are copied and filtered ----
def cfg = new File(basedir, "target/wisdom/application/my.configuration.cfg");
assertThat(cfg).isFile();
content = FileUtils.readFileToString(cfg);
assertThat(content)
        .contains("foo = foo")
        .contains("name = " + BuiltProject.ARTIFACT_ID);

return true;

