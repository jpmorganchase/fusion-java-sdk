package io.github.jpmorganchase.fusion.api.request;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
class CallablePartsTest {

    LinkedList<CallablePart> callableParts = new LinkedList<>();
    LinkedList<InputStream> expected = new LinkedList<>();

    LinkedList<Future<InputStream>> actual = new LinkedList<>();

    CallableParts testee;

    @Mock
    ExecutorService executor;

    @Test
    public void testFutureIsReturned() throws Exception {

        // Given
        givenPart("foo/bar/1");
        givenPart("foo/bar/3");
        givenCallToExecutorForPartReturns(0, "foobar1");
        givenCallToExecutorForPartReturns(1, "foobar2");
        givenCallablePartsIsConstructed();

        // When
        whenNextIsInvoked(3);

        // Then
        thenFutureReturnedShouldBeAsExpected(0, expected.get(0));
        thenFutureReturnedShouldBeAsExpected(1, expected.get(1));
        thenFutureReturnedShouldBeAsExpected(2, null);
        thenExecutorHasBeenInvokedTheExpectedNumberOfTimes(2);
    }

    private void thenExecutorHasBeenInvokedTheExpectedNumberOfTimes(int times) {
        Mockito.verify(executor, Mockito.times(times)).submit(Mockito.any(CallablePart.class));
    }

    private void thenFutureReturnedShouldBeAsExpected(int idx, InputStream inputStream) throws Exception {
        assertThat(actual.get(idx).get(), equalTo(inputStream));
    }

    private void givenCallablePartsIsConstructed() throws IOException {
        testee = CallableParts.builder().parts(callableParts).executor(executor).build();
    }

    private void whenNextIsInvoked(int times) {
        for (int i = 0; i < times; i++) {
            actual.add(testee.next());
        }
    }

    private void givenCallToExecutorForPartReturns(int idx, String data) {
        InputStream is = new ByteArrayInputStream(data.getBytes());
        Mockito.when(executor.submit(callableParts.get(idx))).thenReturn(CompletableFuture.completedFuture(is));

        expected.add(idx, is);
    }

    private void givenPart(String path) {
        CallablePart cp1 = CallablePart.builder().path(path).build();
        callableParts.add(cp1);
    }
}
