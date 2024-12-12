package io.github.jpmorganchase.fusion.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUtils {

    public static String loadJsonForIt(String resourcePath) {

        String pathPrefix = "src/test/resources/__files/";
        try {
            return new String(Files.readAllBytes(Paths.get(pathPrefix + resourcePath)));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + resourcePath, e);
        }
    }

    public static List<Object> listOf(Object... args) {
        return new ArrayList<>(Arrays.asList(args));
    }
}
