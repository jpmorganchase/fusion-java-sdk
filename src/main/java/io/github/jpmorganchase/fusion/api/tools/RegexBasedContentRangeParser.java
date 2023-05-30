package io.github.jpmorganchase.fusion.api.tools;

import io.github.jpmorganchase.fusion.api.response.ContentRange;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool used to parse the "Content-Range" header for each pertinent element.
 * <p>
 * Content-Range header expected to hold a value following the pattern "bytes startPos-endPos/contentLength"
 */
public class RegexBasedContentRangeParser implements ContentRangeParser {

    private static final String PATTERN = "bytes (\\d+)-(\\d+)/(\\d+)";
    private static final Long EMPTY = -1L;

    @Override
    public ContentRange parse(String contentRange) {

        Pattern regex = Pattern.compile(PATTERN);
        Matcher matcher = regex.matcher(contentRange);
        if (matcher.find()) {
            return ContentRange.builder()
                    .start(Long.parseLong(matcher.group(1)))
                    .end(Long.parseLong(matcher.group(2)))
                    .total(Long.parseLong(matcher.group(3)))
                    .build();
        }

        return ContentRange.builder().start(EMPTY).end(EMPTY).total(EMPTY).build();
    }
}
