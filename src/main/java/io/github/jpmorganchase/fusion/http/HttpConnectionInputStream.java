package io.github.jpmorganchase.fusion.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

final class HttpConnectionInputStream extends InputStream {

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
