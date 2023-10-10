package io.github.jpmorganchase.fusion.packaging;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * This test will run under failsafe after the JAR has been built for the project
 * We will check that the manifest in the JAR contains the correct version
 * This will ensure that when we populate the user agent we can retrieve the version 
 */
public class PackageManifestTest {

    private static final String ROOT_DIR = System.getProperty("user.dir");
    private static final String POM_FILE = Paths.get(ROOT_DIR, "pom.xml").toString();

    @Test
    @Tag("integration")
    void manifestContainsMavenVersionNumber() throws Exception{
        String pomVersion = pomVersion();
        String manifestVersion = manifestVersionFromJar(pomVersion);        
        assertThat(manifestVersion, is(pomVersion));
    }

    private String pomVersion() throws Exception{
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(POM_FILE));
        return model.getVersion();
    }

    private String manifestVersionFromJar(String pomVersion) throws Exception{
        String jarPath = Paths.get(ROOT_DIR, "target", "fusion-sdk-"+pomVersion+".jar").toString();
        JarFile jarFile = new JarFile(jarPath);
        Manifest manifest = jarFile.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String manifestVersion = attributes.getValue("Implementation-Version");
        jarFile.close();
        return manifestVersion;
    }

}
