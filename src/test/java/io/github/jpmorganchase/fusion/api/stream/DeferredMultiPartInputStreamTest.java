package io.github.jpmorganchase.fusion.api.stream;

import io.github.jpmorganchase.fusion.api.request.CallableParts;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
class DeferredMultiPartInputStreamTest {

    @Mock
    CallableParts parts;

    DeferredMultiPartInputStream testee;

    OngoingStubbing<Future<InputStream>> partStubbing;

    String actualDataRead;

    @Test
    public void testCanHandleStreamComposedOfMultipleParts() throws Exception {

        givenCallForPartReturns("foobar1");
        givenNextCallForPartReturns("foobar2");
        givenFinalCallForNextPart();
        givenDeferredInputStream();

        whenReadIsCalledUntilAllDataIsRead();

        thenDataShouldBeAsExpected("foobar1foobar2");
        thenPartsShouldHaveBeenInvokedAsExpected(3);
    }

    @Test
    public void testCanHandleStreamComposedOfSinglePart() throws Exception {
        givenCallForPartReturns("foobar1");
        givenFinalCallForNextPart();
        givenDeferredInputStream();

        whenReadIsCalledUntilAllDataIsRead();

        thenDataShouldBeAsExpected("foobar1");
        thenPartsShouldHaveBeenInvokedAsExpected(2);
    }

    @Test
    public void testCanHandleNoParts() throws Exception {
        givenCallForPartReturnsNull();
        givenDeferredInputStream();

        whenReadIsCalledUntilAllDataIsRead();

        thenPartsShouldHaveBeenInvokedAsExpected(1);
    }

    private void thenPartsShouldHaveBeenInvokedAsExpected(int times) {
        Mockito.verify(parts, Mockito.times(times)).next();
    }

    private void thenDataShouldBeAsExpected(String expected) {
        MatcherAssert.assertThat(actualDataRead, CoreMatchers.equalTo(expected));
    }

    private void givenDeferredInputStream() throws Exception {
        testee = DeferredMultiPartInputStream.builder().parts(parts).build();
    }

    private void whenReadIsCalledUntilAllDataIsRead() throws Exception {
        int byteRead;
        StringBuilder sb = new StringBuilder();

        while ((byteRead = testee.read()) != -1) {
            sb.append(new String(new byte[] {(byte) byteRead}, StandardCharsets.UTF_8));
        }
        actualDataRead = sb.toString();
    }

    private void givenCallForPartReturnsNull() {
        Mockito.when(parts.next()).thenReturn(CompletableFuture.completedFuture(null));
    }

    private void givenFinalCallForNextPart() {
        partStubbing.thenReturn(CompletableFuture.completedFuture(null));
    }

    private void givenNextCallForPartReturns(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
        partStubbing.thenReturn(CompletableFuture.completedFuture(inputStream));
    }

    private void givenCallForPartReturns(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
        partStubbing = Mockito.when(parts.next()).thenReturn(CompletableFuture.completedFuture(inputStream));
    }
}
