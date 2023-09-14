package io.github.jpmorganchase.fusion.http;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
public class JdkClient implements Client {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DEFAULT_ERROR = "{\"error\": \"Unable to perform requested action\"}";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    private final Proxy proxy;

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
        return executeMethod(METHOD_POST, path, headers, body);
    }

    @Override
    public HttpResponse<String> put(String path, Map<String, String> headers, InputStream body) {
        if (body == null) {
            throw new ClientException("No request body specified for PUT operation");
        }
        return executeMethod(METHOD_PUT, path, headers, body, true, this::getResponseBody);
    }

    @Override
    public HttpResponse<String> delete(String path, Map<String, String> headers, String body) {
        return executeMethod(METHOD_DELETE, path, headers, body);
    }

    private HttpResponse<String> executeMethod(String method, String path, Map<String, String> headers) {
        return executeMethod(method, path, headers, null);
    }

    private HttpResponse<String> executeMethod(String method, String path, Map<String, String> headers, String body) {
        InputStream bodyAsStream =
                body != null ? new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)) : null;
        return executeMethod(method, path, headers, bodyAsStream, true, this::getResponseBody);
    }

    private <T> HttpResponse<T> executeMethod(
            String method,
            String path,
            Map<String, String> headers,
            InputStream body,
            boolean closeConnection,
            Function<HttpURLConnection, T> resultMapper) {
        URL url = parseUrl(path);
        HttpURLConnection connection = openConnection(url);
        headers.forEach(connection::setRequestProperty);
        connection.setRequestProperty("User-Agent", UserAgentGenerator.getUserAgentString(this.getClass()));

        try {
            int httpCode;
            logRequest(connection, method);
            if (body != null) {
                httpCode = executeRequestWithBody(connection, method, body);
            } else {
                httpCode = executeRequest(connection, method);
            }

            HttpResponse<T> response = HttpResponse.<T>builder()
                    .body(resultMapper.apply(connection))
                    .headers(connection.getHeaderFields())
                    .statusCode(httpCode)
                    .build();
            logger.debug("Response: {}", response);
            return response;
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
            throw new ClientException(String.format("Malformed URL path received: %s", path), e);
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
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(getResponseStream(connection), StandardCharsets.UTF_8));
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
                return errorStream(connection);
            }
        } catch (IOException e) {
            throw new ClientException("Failed to get InputStream from response", e);
        }
    }

    private InputStream errorStream(final HttpURLConnection connection) {
        if (null == connection.getErrorStream()) {
            return new ByteArrayInputStream(DEFAULT_ERROR.getBytes(StandardCharsets.UTF_8));
        }
        return connection.getErrorStream();
    }

    private int executeRequest(HttpURLConnection connection, String httpMethod) {
        try {
            connection.setRequestMethod(httpMethod);
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new ClientException("Error performing HTTP operation", e);
        }
    }

    private int executeRequestWithBody(HttpURLConnection connection, String method, InputStream body) {
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

    private void logRequest(HttpURLConnection connection, String method) {
        logger.debug("Executing {} request for URL: {}", method, connection.getURL());
    }

    public static JdkClientBuilder builder() {
        return new CustomJdkClientBuilder();
    }

    public static class JdkClientBuilder {

        String url;
        int port;
        Proxy proxy = Proxy.NO_PROXY;

        public JdkClientBuilder url(String url) {
            this.url = url;
            return this;
        }

        public JdkClientBuilder port(int port) {
            this.port = port;
            return this;
        }

        public JdkClientBuilder noProxy() {
            this.proxy = Proxy.NO_PROXY;
            return this;
        }
    }

    private static class CustomJdkClientBuilder extends JdkClientBuilder {
        @Override
        public JdkClient build() {

            if (Objects.nonNull(url)) {
                this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url, port));
            }

            return super.build();
        }
    }
}
