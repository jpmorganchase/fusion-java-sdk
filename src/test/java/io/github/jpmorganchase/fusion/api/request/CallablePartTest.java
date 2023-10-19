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

    DownloadRequest dr;

    InputStream actual;

    @Test
    public void testInvokesFetcher() {

        // Given
        givenDownloadRequest("/foo/bar/1", "foo", "bar");
        givenCallablePart(1);
        givenExpectedResponse("foobar", "foo-checksum-bar");
        givenExpectedCallToPartFetcher(1);

        // When
        whenCallablePartCallMethodIsInvoked();

        // Then
        thenGetPartResponseShouldMatchExpected();
        thenPartFetcherShouldHaveBeenCalledOnce(1);
    }

    private void givenDownloadRequest(String path, String catalog, String dataset) {
        dr = DownloadRequest.builder()
                .apiPath(path)
                .catalog(catalog)
                .dataset(dataset)
                .build();
    }

    private void thenPartFetcherShouldHaveBeenCalledOnce(int partNo) {
        Mockito.verify(fetcher, times(1))
                .fetch(PartRequest.builder().partNo(partNo).downloadRequest(dr).build());
    }

    private void thenGetPartResponseShouldMatchExpected() {
        MatcherAssert.assertThat(actual, CoreMatchers.equalTo(expected.getContent()));
    }

    private void whenCallablePartCallMethodIsInvoked() {
        actual = testee.call();
    }

    private void givenExpectedCallToPartFetcher(int partNo) {
        Mockito.when(fetcher.fetch(
                        PartRequest.builder().partNo(partNo).downloadRequest(dr).build()))
                .thenReturn(expected);
    }

    private void givenExpectedResponse(String data, String checksum) {
        expected = GetPartResponse.builder()
                .content(new ByteArrayInputStream(data.getBytes()))
                .head(Head.builder().checksum(checksum).build())
                .build();
    }

    private void givenCallablePart(int partNo) {
        testee = CallablePart.builder()
                .partNo(partNo)
                .downloadRequest(dr)
                .partFetcher(fetcher)
                .build();
    }
}
