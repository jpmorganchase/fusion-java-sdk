package io.github.jpmorganchase.fusion.api.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple parser to quickly and cheaply extract the error detail from a json response.
 */
public class RegexBasedErrorParser {
    private static final String PATTERN = "\"error\"\\s*:\\s*\"([^\"]+)\"";
    public static final String UNKNOWN = "Unknown";

    public static String get(final String json) {
        Pattern regex = Pattern.compile(PATTERN);
        Matcher matcher = regex.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return UNKNOWN;
    }
}
