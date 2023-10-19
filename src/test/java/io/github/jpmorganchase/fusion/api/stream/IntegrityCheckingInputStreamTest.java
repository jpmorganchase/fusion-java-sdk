package io.github.jpmorganchase.fusion.api.stream;

import io.github.jpmorganchase.fusion.digest.PartChecker;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("SameParameterValue")
class IntegrityCheckingInputStreamTest {

    IntegrityCheckingInputStream testee;

    PartChecker checker;

    InputStream inputStream;

    String checksum;

    String actualDataRead;

    IOException exception;

    int actualBytesRead;

    @Test
    public void testCanSuccessfullyVerifyIntegrity() throws Exception {
        givenPart("foobar");
        givenChecksum("w6uP8Tcg6K2QR905Rms8iXTlksL6OD1KOWBxTK7wxPI=");
        givenPartChecker();
        givenIntegrityCheckingInputStream();

        whenEntireStreamHasBeenRead();

        thenItShouldEqualOriginalPart("foobar");
    }

    @Test
    public void testExceptionIsThrownWhenIntegrityCheckFails() {
        givenPart("foobar");
        givenChecksum("dodgy-checksum");
        givenPartChecker();
        givenIntegrityCheckingInputStream();

        whenStreamIsReadWithExpectantFailure();

        thenExceptionShouldMatchExpected();
    }

    @Test
    public void testMinusOneReturnedWhenNoPartToRead() throws Exception {
        givenChecksum("dodgy-checksum");
        givenPartChecker();
        givenIntegrityCheckingInputStream();

        whenStreamIsReadAndThereIsNoPart();

        thenBytesReadShouldBeAsExpected(-1);
    }

    private void thenBytesReadShouldBeAsExpected(int expected) {
        MatcherAssert.assertThat(actualBytesRead, CoreMatchers.equalTo(expected));
    }

    private void thenExceptionShouldMatchExpected() {
        MatcherAssert.assertThat(
                exception.getMessage(), CoreMatchers.equalTo("Corrupted stream, verification of checksum failed"));
    }

    private void thenItShouldEqualOriginalPart(String expected) {
        MatcherAssert.assertThat(actualDataRead, CoreMatchers.equalTo(expected));
    }

    private void whenStreamIsReadWithExpectantFailure() {
        exception = Assertions.assertThrows(IOException.class, this::whenEntireStreamHasBeenRead);
    }

    private void whenStreamIsReadAndThereIsNoPart() throws IOException {
        actualBytesRead = testee.read();
    }

    private void whenEntireStreamHasBeenRead() throws Exception {
        int byteRead;
        StringBuilder sb = new StringBuilder();

        while ((byteRead = testee.read()) != -1) {
            sb.append(new String(new byte[] {(byte) byteRead}, StandardCharsets.UTF_8));
        }
        actualDataRead = sb.toString();
    }

    private void givenIntegrityCheckingInputStream() {
        testee = IntegrityCheckingInputStream.builder()
                .partChecker(checker)
                .part(inputStream)
                .checksum(checksum)
                .build();
    }

    private void givenPartChecker() {
        this.checker = PartChecker.builder().build();
    }

    private void givenChecksum(String checksum) {
        this.checksum = checksum;
    }

    private void givenPart(String data) {
        inputStream = new ByteArrayInputStream(data.getBytes());
    }
}
