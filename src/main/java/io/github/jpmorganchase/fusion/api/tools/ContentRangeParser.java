package io.github.jpmorganchase.fusion.api.tools;

import io.github.jpmorganchase.fusion.api.response.ContentRange;

public interface ContentRangeParser {

    ContentRange parse(String contentLength);
}
