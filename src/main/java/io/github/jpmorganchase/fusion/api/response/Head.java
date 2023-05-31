package io.github.jpmorganchase.fusion.api.response;

import static java.util.Objects.nonNull;

import io.github.jpmorganchase.fusion.api.tools.ContentRangeParser;
import io.github.jpmorganchase.fusion.api.tools.RegexBasedContentRangeParser;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class Head {

    private static String CHECKSUM_HEADER = "x-jpmc-checksum-sha256";
    private static String VERSION_HEADER = "x-jpmc-version-id";
    private static String PART_COUNT_HEADER = "x-jpmc-mp-parts-count";
    private static String CONTENT_LENGTH_HEADER = "Content-Length";
    private static String CONTENT_RANGE_HEADER = "Content-Range";
    private static String CHECKSUM_SEPARATOR = "-";

    private String version;
    private String checksum;
    private int partCount;
    private long contentLength;
    private ContentRange contentRange;
    private boolean isMultipart;

    public static class HeadBuilder {

        private Map<String, List<String>> headers;

        private ContentRangeParser contentRangeParser;

        public HeadBuilder fromHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public HeadBuilder contentRangeParser(ContentRangeParser parser) {
            this.contentRangeParser = parser;
            return this;
        }

        /**
         * Builder used to represent details of the Head of a distribution
         * @return Head object
         */
        public Head build() {

            if (null != headers) {
                handlePartCountHeader();
                handleChecksumHeader();
                handleVersionHeader();
                handleContentLength();
                handleContentRangeHeader();
            }
            return new Head(version, checksum, partCount, contentLength, contentRange, isMultipart);
        }

        private void handleContentLength() {
            if (this.headers.containsKey(CONTENT_LENGTH_HEADER)) {
                List<String> values = this.headers.get(CONTENT_LENGTH_HEADER);
                if (!values.isEmpty() && nonNull(values.get(0))) {
                    this.contentLength = Long.parseLong(values.get(0));
                }
            }
        }

        private void handleVersionHeader() {
            if (this.headers.containsKey(VERSION_HEADER)) {
                List<String> values = this.headers.get(VERSION_HEADER);
                if (!values.isEmpty() && nonNull(values.get(0))) {
                    this.version = values.get(0);
                }
            }
        }

        private void handleChecksumHeader() {
            if (this.headers.containsKey(CHECKSUM_HEADER)) {
                List<String> values = this.headers.get(CHECKSUM_HEADER);
                if (!values.isEmpty() && nonNull(values.get(0))) {
                    this.checksum = values.get(0).split(CHECKSUM_SEPARATOR)[0];
                }
            }
        }

        private void handlePartCountHeader() {
            if (this.headers.containsKey(PART_COUNT_HEADER)) {
                List<String> values = this.headers.get(PART_COUNT_HEADER);
                if (!values.isEmpty() && nonNull(values.get(0))) {
                    this.isMultipart = true;
                    this.partCount = Integer.parseInt(values.get(0));
                }
            }
        }

        private void handleContentRangeHeader() {
            if (this.headers.containsKey(CONTENT_RANGE_HEADER)) {

                if (Objects.isNull(contentRangeParser)) {
                    contentRangeParser = new RegexBasedContentRangeParser();
                }

                List<String> values = this.headers.get(CONTENT_RANGE_HEADER);
                if (!values.isEmpty() && nonNull(values.get(0))) {
                    this.contentRange = contentRangeParser.parse(values.get(0));
                }
            }
        }
    }
}
