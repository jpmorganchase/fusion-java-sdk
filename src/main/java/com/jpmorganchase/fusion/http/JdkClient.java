package com.jpmorganchase.fusion.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;

public class JdkClient implements Client {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    private final Proxy proxy;

    public JdkClient(Proxy proxy) {
        this.proxy = proxy;
    }

    public JdkClient() {
        this.proxy = Proxy.NO_PROXY;
    }

    @Override
    public HttpResponse<String> get(String path, Map<String, String> headers) {
        return executeMethod(METHOD_GET, path, headers);
    }

    @Override
    public HttpResponse<InputStream> getInputStream(String path, Map<String, String> headers) {
        return executeMethod(METHOD_GET, path, headers, null, false, HttpConnectionInputStream::new);
    }

    @Override
    public HttpResponse<String> post(String path, Map<String, String> headers, String body) {
        if (body == null) {
            throw new ClientException("No request body specified for POST operation");
        }
        return executeMethod(METHOD_POST, path, headers, body);
    }

    @Override
    public HttpResponse<String> put(String path, Map<String, String> headers, InputStream body) {
        if (body == null) {
            throw new ClientException("No request body specified for PUT operation");
        }
        return executeMethod(METHOD_PUT, path, headers, body, true, this::getResponseBody);
    }

    private HttpResponse<String> executeMethod(String method, String path, Map<String, String> headers) {
        return executeMethod(method, path, headers, null);
    }

    private HttpResponse<String> executeMethod(String method, String path, Map<String, String> headers, String body) {
        InputStream bodyAsStream = body != null ? new ByteArrayInputStream(body.getBytes()) : null;
        return executeMethod(method, path, headers, bodyAsStream, true, this::getResponseBody);
    }

    private <T> HttpResponse<T> executeMethod(String method, String path, Map<String, String> headers, InputStream body, boolean closeConnection, Function<HttpURLConnection, T> resultMapper) {
        URL url = parseUrl(path);
        HttpURLConnection connection = openConnection(url);
        headers.forEach(connection::setRequestProperty);

        try {
            int httpCode;
            if (body != null) {
                httpCode = executeRequestWithBody(connection, method, body);
            } else {
                httpCode = executeRequest(connection, method);
            }

            return HttpResponse.<T>builder()
                    .body(resultMapper.apply(connection))
                    .headers(connection.getHeaderFields())
                    .statusCode(httpCode)
                    .build();
        } finally {
            if (closeConnection) {
                connection.disconnect();
            }
        }
    }

    private URL parseUrl(String path) {
        try {
            return new URL(path);
        } catch (MalformedURLException e) {
            throw new ClientException("Malformed URL path received", e);
        }
    }

    private HttpURLConnection openConnection(URL url) {
        try {
            return (HttpURLConnection) url.openConnection(proxy);
        } catch (IOException e) {
            throw new ClientException("Error establishing HTTP connection", e);
        }
    }

    private String getResponseBody(HttpURLConnection connection) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getResponseStream(connection)));
        StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new ClientException("Failed to read data from response", e);
        }
        return out.toString();
    }

    private InputStream getResponseStream(HttpURLConnection connection) {
        try {
            int httpCode = connection.getResponseCode();
            if (100 <= httpCode && httpCode <= 399) {
                return connection.getInputStream();
            } else {
                //TODO: need to handle null case?
                return connection.getErrorStream();
            }
        } catch (IOException e) {
            throw new ClientException("Failed to get InputStream from response", e);
        }
    }

    private int executeRequest(HttpURLConnection connection, String httpMethod) {
        try {
            connection.setRequestMethod(httpMethod);
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new ClientException("Error performing HTTP operation", e);
        }
    }

    private int executeRequestWithBody(HttpURLConnection connection, String method, InputStream body){
        try {
            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            OutputStream os = connection.getOutputStream();
            byte[] buf = new byte[8192];
            int length;
            while ((length = body.read(buf)) != -1) {
                os.write(buf, 0, length);
            }
            body.close();
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new ClientException("Failed to send request data", e);
        }
    }
}
