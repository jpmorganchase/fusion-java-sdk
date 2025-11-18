package io.github.jpmorganchase.fusion.digest;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("SameParameterValue")
class PartCheckerTest {

    PartChecker testee;
    ByteArrayInputStream bytesToVerify;

    IOException exception;

    @Test
    public void testCanUpdateVerifyDigest() throws Exception {

        // Given
        givenPartChecker();
        givenBytesToVerify("foobar");

        // When
        whenAllBytesHaveBeenReadByThePartChecker();

        // Then
        thenVerificationShouldCompleteSuccessfully("w6uP8Tcg6K2QR905Rms8iXTlksL6OD1KOWBxTK7wxPI=");
    }

    @Test
    public void testSkipChecksumSkipsVerificationEvenWhenChecksumIsWrong() throws Exception {

        // Given
        // skipChecksum = true, so verification should NO-OP
        testee = PartChecker.builder().skipChecksum(true).build();

        givenBytesToVerify("foobar");

        // When
        whenAllBytesHaveBeenReadByThePartChecker();

        // Then - even with wrong checksum, no exception should be thrown
        Assertions.assertDoesNotThrow(() -> testee.verify("dodgy-checksum"));
    }

    @Test
    public void testAlternateMessageDigestAlgoCanBeSet() throws Exception {

        // Given
        givenPartChecker("MD5");
        givenBytesToVerify("foobar");

        // When
        whenAllBytesHaveBeenReadByThePartChecker();

        // Then
        thenVerificationShouldCompleteSuccessfully("OFj2IjCsPJFfMAxmQxLGPw==");
    }

    @Test
    public void testInvalidVerificationChecksumThrowsException() throws Exception {

        // Given
        givenPartChecker();
        givenBytesToVerify("foobar");

        // When
        whenAllBytesHaveBeenReadByThePartChecker();

        // Then
        thenVerificationShouldThrowException("abc=");
    }

    @Test
    public void testInvalidDigestAlgoThrowsException() throws Exception {
        // Given
        givenPartChecker("FOO-BAR");
        givenBytesToVerify("foobar");

        // When
        whenAttemptIsMadeToUpdateExceptionShouldBeThrown();

        // Then
        thenExceptionShouldBeAsExpected();
    }

    private void thenExceptionShouldBeAsExpected() {
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.equalTo("Invalid digest algorithm provided"));
    }

    private void thenVerificationShouldCompleteSuccessfully(String expected) {
        Assertions.assertDoesNotThrow(() -> testee.verify(expected));
    }

    private void thenVerificationShouldThrowException(String expected) {
        assertThrows(IOException.class, () -> testee.verify(expected));
    }

    private void whenAllBytesHaveBeenReadByThePartChecker() throws Exception {
        int bytesRead;
        while ((bytesRead = bytesToVerify.read()) != -1) {
            testee.update(bytesRead);
        }
    }

    private void whenAttemptIsMadeToUpdateExceptionShouldBeThrown() {
        exception = assertThrows(IOException.class, () -> testee.update(bytesToVerify.read()));
    }

    private void givenBytesToVerify(String data) {
        bytesToVerify = new ByteArrayInputStream(data.getBytes());
    }

    private void givenPartChecker(String digestAlgo) throws IOException {
        testee = PartChecker.builder().digestAlgo(digestAlgo).build();
    }

    private void givenPartChecker() throws IOException {
        testee = PartChecker.builder().build();
    }
}
