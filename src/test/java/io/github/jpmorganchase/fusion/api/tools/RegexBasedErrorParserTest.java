package io.github.jpmorganchase.fusion.api.tools;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class RegexBasedErrorParserTest {

    @Test
    public void testJsonWithError() {
        String json = "{\"error\": \"blah\"}";
        String actual = RegexBasedErrorParser.get(json);
        assertThat(actual, equalTo("blah"));
    }

    @Test
    public void testJsonWithErrorContainingSpaces() {
        String json = "{\"error\" : \"blah\"}";
        String actual = RegexBasedErrorParser.get(json);
        assertThat(actual, equalTo("blah"));
    }

    @Test
    public void testJsonWithoutError() {
        String json = "{\"detail\": \"blah\"}";
        String actual = RegexBasedErrorParser.get(json);
        assertThat(actual, equalTo("Unknown"));
    }

    @Test
    public void testJsonWithoutErrorButWithErrorInSentence() {
        String json = "{\"detail\": \"there is an error\"}";
        String actual = RegexBasedErrorParser.get(json);
        assertThat(actual, equalTo("Unknown"));
    }
}
