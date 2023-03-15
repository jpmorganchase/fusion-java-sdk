package com.jpmorganchase.fusion.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HttpConnectionInputStreamTest {

    @Mock
    HttpURLConnection connection;

    @Mock
    InputStream underlyingStream;

    @Test
    public void readDelegatesCallsToTheConnection() throws Exception {
        HttpConnectionInputStream stream = new HttpConnectionInputStream(connection);

        when(connection.getInputStream()).thenReturn(underlyingStream);
        when(underlyingStream.read()).thenReturn(11);

        int result = stream.read();

        verify(connection, times(1)).getInputStream();
        assertThat(result, is(11));
    }

    @Test
    public void closeDelegatesCallsToTheConnection() throws Exception {
        HttpConnectionInputStream stream = new HttpConnectionInputStream(connection);

        when(connection.getInputStream()).thenReturn(underlyingStream);

        stream.close();

        verify(connection, times(1)).disconnect();
        verify(underlyingStream, times(1)).close();
    }
}
