package io.github.jpmorganchase.fusion.http;

import java.io.InputStream;
import java.util.Map;

public interface Client {

    HttpResponse<String> get(String path, Map<String, String> headers);

    HttpResponse<InputStream> getInputStream(String path, Map<String, String> headers);

    HttpResponse<String> post(String path, Map<String, String> headers, String body);

    HttpResponse<String> put(String path, Map<String, String> headers, InputStream body);

    HttpResponse<String> put(String path, String body, Map<String, String> headers);

    HttpResponse<String> delete(String path, Map<String, String> headers, String body);
}
