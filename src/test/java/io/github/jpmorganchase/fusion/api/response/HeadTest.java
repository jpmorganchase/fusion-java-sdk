package io.github.jpmorganchase.fusion.api.response;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

class HeadTest {

    Map<String, List<String>> responseHeaders = new HashMap<>();

    Head testee;

    @Test
    public void testChecksumIsParsedFromHeaders() {
        // given
        givenResponseHeader("x-jpmc-checksum-sha256", "foochecksum");

        // when
        headIsBuiltFromHeaders();

        // then
        assertThat(testee.getChecksum(), CoreMatchers.equalTo("foochecksum"));
    }

    @Test
    public void testChecksumIsParsedFromHeadersWhenPartCountIsAppended() {
        // given
        givenResponseHeader("x-jpmc-checksum-sha256", "foochecksum-5");

        // when
        headIsBuiltFromHeaders();

        // then
        assertThat(testee.getChecksum(), CoreMatchers.equalTo("foochecksum"));
    }

    private void headIsBuiltFromHeaders() {
        testee = Head.builder().fromHeaders(responseHeaders).build();
    }

    @Test
    public void testVersionIsParsedFromHeaders() {
        // given
        givenResponseHeader("x-jpmc-version-id", "some-version");

        // when
        headIsBuiltFromHeaders();

        // then
        assertThat(testee.getVersion(), CoreMatchers.equalTo("some-version"));
    }

    @Test
    public void testPartsCountIsParsedFromHeaders() {
        // given
        givenResponseHeader("x-jpmc-mp-parts-count", "5");

        // when
        headIsBuiltFromHeaders();

        // then
        assertThat(testee.getPartCount(), CoreMatchers.equalTo(5));
    }

    @Test
    public void testContentLengthIsParsedFromHeaders() {
        // given
        givenResponseHeader("Content-Length", "23");

        // when
        headIsBuiltFromHeaders();

        // then
        assertThat(testee.getContentLength(), CoreMatchers.equalTo(23L));
    }

    @Test
    public void testContentRangeIsParsedFromHeaders() {
        // given
        givenResponseHeader("Content-Range", "bytes 0-5/10");

        // when
        headIsBuiltFromHeaders();

        // then
        assertThat(
                testee.getContentRange(),
                CoreMatchers.equalTo(
                        ContentRange.builder().start(0L).end(5L).total(10L).build()));
    }

    private void givenResponseHeader(String key, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        responseHeaders.put(key, values);
    }
}
