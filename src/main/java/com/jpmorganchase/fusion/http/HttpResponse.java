package com.jpmorganchase.fusion.http;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class HttpResponse <T>{

    int statusCode;
    Map<String, List<String>> headers;
    T body;

}