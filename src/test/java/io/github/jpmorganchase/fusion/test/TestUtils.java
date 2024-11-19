package io.github.jpmorganchase.fusion.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {

    public static String loadJsonForIt(String resourcePath) {

        String pathPrefix = "src/test/resources/__files/";
        try {
            return new String(Files.readAllBytes(Paths.get(pathPrefix + resourcePath)));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + resourcePath, e);
        }
    }
}
