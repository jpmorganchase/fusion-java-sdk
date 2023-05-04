package io.github.jpmorganchase.fusion.pact.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

import lombok.SneakyThrows;

public class FileHelper {

    private FileHelper() {}

    /**
     This is a test method to read contents from an input stream and return them as a string.

     @param is The input stream to read from.
     @return The contents of the input stream as a string.
     */
    public static String readContentsFromStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
