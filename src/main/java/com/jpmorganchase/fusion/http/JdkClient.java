package com.jpmorganchase.fusion.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

//TODO: Come back and implement Proxy logic
public class JdkClient implements Client {

    private final Proxy proxy;

    public JdkClient(Proxy proxy) {
        this.proxy = proxy;
    }

    public JdkClient() {
        this.proxy = Proxy.NO_PROXY;
    }

    @Override
    public HttpResponse<String> get(String path, Map<String, String> headers) {
        return executeMethod("GET", path, headers);
    }

    //TODO:Refactor to make common with the String handling code
    @Override
    public HttpResponse<InputStream> getInputStream(String path, Map<String, String> headers) {
        URL url = parseUrl(path);
        HttpURLConnection connection = openConnection(url);

        headers.forEach(connection::setRequestProperty);
        connection.setDoOutput(true);
        int httpCode;

        httpCode = executeRequest(connection, "GET");

        //TODO: Also handle error case - see logic in regular GET
        InputStream responseStream = new HttpConnectionInputStream(connection);

        return HttpResponse.<InputStream>builder()
                .body(responseStream)
                .headers(connection.getHeaderFields())
                .statusCode(httpCode)
                .build();
    }

    private static final class HttpConnectionInputStream extends InputStream {

        private final HttpURLConnection connection;

        public HttpConnectionInputStream(HttpURLConnection connection) {
            this.connection = connection;
        }

        @Override
        public int read() throws IOException {
            return connection.getInputStream().read();
        }

        @Override
        public void close() throws IOException {
            connection.getInputStream().close();
            connection.disconnect();
        }
    }

    @Override
    public HttpResponse<String> post(String path, Map<String, String> headers, String body) {
        return executeMethod("POST", path, headers, body);
    }

    //TODO: Refactor for common code
    @Override
    public HttpResponse<String> put(String path, Map<String, String> headers, InputStream body) {
        URL url = parseUrl(path);
        HttpURLConnection connection = openConnection(url);

        try {
            headers.forEach(connection::setRequestProperty);
            connection.setDoOutput(true);
            int httpCode;

            if (body != null) {
                try {
                    connection.setRequestMethod("PUT");
                    OutputStream os = connection.getOutputStream();
                    byte[] buf = new byte[8192];
                    int length;
                    while(( length = body.read(buf)) != -1){
                        os.write(buf, 0, length);
                    }
                    httpCode = connection.getResponseCode();
                    //TODO: Close stuff?
                } catch (IOException e) {
                    throw new ClientException("Failed to send request data", e);
                }
            } else {
                throw new ClientException("No request body specified for PUT operation");
            }

            String response = getResponseBody(connection, httpCode);

            return HttpResponse.<String>builder()
                    .body(response)
                    .headers(connection.getHeaderFields())
                    .statusCode(httpCode)
                    .build();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpResponse<String> executeMethod(String method, String path, Map<String, String> headers) {
        return executeMethod(method, path, headers, null);
    }

    private HttpResponse<String> executeMethod(String method, String path, Map<String, String> headers, String body) {
        URL url = parseUrl(path);
        HttpURLConnection connection = openConnection(url);


        try {
            headers.forEach(connection::setRequestProperty);
            connection.setDoOutput(true);
            int httpCode;

            if (body != null) {
                try {
                    connection.setRequestMethod(method);
                    PrintStream os = new PrintStream(connection.getOutputStream());
                    os.print(body);
                    httpCode = connection.getResponseCode();
                } catch (IOException e) {
                    throw new ClientException("Failed to send request data", e);
                }
            } else {
                httpCode = executeRequest(connection, method);
            }

            String response = getResponseBody(connection, httpCode);

            return HttpResponse.<String>builder()
                    .body(response)
                    .headers(connection.getHeaderFields())
                    .statusCode(httpCode)
                    .build();

        } finally {
            if (connection != null) {
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

    //TODO: Refactor this code to clean it up
    private String getResponseBody(HttpURLConnection connection, int httpCode) {
        BufferedReader reader;
        if (100 <= httpCode && httpCode <= 399) {
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                throw new ClientException("Failed to get InputStream from response", e);
            }
        } else {
            //TODO: need to handle null case?
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException e) {
            throw new ClientException("Failed to read data from response", e);
        }

        //TODO: Close the BufferedReader?

        return out.toString();
    }

    private int executeRequest(HttpURLConnection connection, String httpMethod) {
        try {
            connection.setRequestMethod(httpMethod);
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new ClientException("Error performing HTTP operation", e);
        }
    }
}
