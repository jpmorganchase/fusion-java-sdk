package io.github.jpmorganchase.fusion.api.request;

import static org.mockito.Mockito.times;

import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"SameParameterValue"})
@ExtendWith(MockitoExtension.class)
class CallablePartTest {

    @Mock
    PartFetcher fetcher;

    CallablePart testee;

    GetPartResponse expected;

    InputStream actual;

    @Test
    public void testInvokesFetcher() {

        // Given
        givenCallablePart("foo/bar/1");
        givenExpectedResponse("foobar", "foo-checksum-bar");
        givenExpectedCallToPartFetcher("foo/bar/1");

        // When
        whenCallablePartCallMethodIsInvoked();

        // Then
        thenGetPartResponseShouldMatchExpected();
        thenPartFetcherShouldHaveBeenCalledOnce("foo/bar/1");
    }

    private void thenPartFetcherShouldHaveBeenCalledOnce(String path) {
        Mockito.verify(fetcher, times(1)).fetch(path);
    }

    private void thenGetPartResponseShouldMatchExpected() {
        MatcherAssert.assertThat(actual, CoreMatchers.equalTo(expected.getContent()));
    }

    private void whenCallablePartCallMethodIsInvoked() {
        actual = testee.call();
    }

    private void givenExpectedCallToPartFetcher(String path) {
        Mockito.when(fetcher.fetch(path)).thenReturn(expected);
    }

    private void givenExpectedResponse(String data, String checksum) {
        expected = GetPartResponse.builder()
                .content(new ByteArrayInputStream(data.getBytes()))
                .head(Head.builder().checksum(checksum).build())
                .build();
    }

    private void givenCallablePart(String path) {
        testee = CallablePart.builder().path(path).partFetcher(fetcher).build();
    }
}
