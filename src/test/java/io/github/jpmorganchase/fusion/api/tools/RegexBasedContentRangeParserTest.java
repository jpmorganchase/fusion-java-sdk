package io.github.jpmorganchase.fusion.api.tools;

import static org.junit.jupiter.api.Assertions.*;

import io.github.jpmorganchase.fusion.api.response.ContentRange;
import org.junit.jupiter.api.Test;

class RegexBasedContentRangeParserTest {

    @Test
    public void testParseValidContentRange() {
        RegexBasedContentRangeParser parser = new RegexBasedContentRangeParser();
        String contentRange = "bytes 0-499/1000";

        ContentRange result = parser.parse(contentRange);

        assertNotNull(result);
        assertEquals(0L, result.getStart());
        assertEquals(499L, result.getEnd());
        assertEquals(1000L, result.getTotal());
    }

    @Test
    public void testParseInvalidContentRange() {
        RegexBasedContentRangeParser parser = new RegexBasedContentRangeParser();
        String contentRange = "invalid";

        ContentRange result = parser.parse(contentRange);

        assertNotNull(result);
        assertEquals(-1L, result.getStart());
        assertEquals(-1L, result.getEnd());
        assertEquals(-1L, result.getTotal());
    }
}
