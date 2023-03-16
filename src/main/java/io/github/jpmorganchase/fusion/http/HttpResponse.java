package io.github.jpmorganchase.fusion.http;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HttpResponse<T> {

    int statusCode;
    Map<String, List<String>> headers;
    T body;

    public boolean isError() {
        return statusCode >= 400;
    }
    
}