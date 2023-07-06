package io.github.jpmorganchase.fusion.api.stream;

import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("SameParameterValue")
class IntegrityCheckingInputStreamTest {

    List<GetPartResponse> responses = new ArrayList<>();
    IntegrityCheckingInputStream dais;

    int bytesToRead = 5;

    String actualDataRead;

    int readCounter = 0;

    Throwable throwable;

    @Nested
    class InputStreamReadToByteArray {
        @Test
        void canReadToByteArrayFromSingleGetPartResponse() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6 "
                    + System.lineSeparator() + "7,8,9");
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadToByteArrayIsCalledUntilEndOfStream();

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6 "
                    + System.lineSeparator() + "7,8,9");
            thenReadCounterShouldBeEqualTo(5);
        }

        @Test
        void canReadToByteArrayFromMultipleGetPartResponse() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadToByteArrayIsCalledUntilEndOfStream();

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator() + "10,11,12"
                    + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18" + System.lineSeparator()
                    + "19,20,21" + System.lineSeparator() + "22,23,24"
                    + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30" + System.lineSeparator()
                    + "31,32,33" + System.lineSeparator());
            thenReadCounterShouldBeEqualTo(20);
        }

        @Test
        void canReadToByteArrayFromMultipleGetPartResponseInOneOperation()
                throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();
            getGivenTheByteReadSizeSetTo(7092);

            // when
            whenReadToByteArrayIsCalledUntilEndOfStream();

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator() + "10,11,12"
                    + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18" + System.lineSeparator()
                    + "19,20,21" + System.lineSeparator() + "22,23,24"
                    + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30" + System.lineSeparator()
                    + "31,32,33" + System.lineSeparator());
            thenReadCounterShouldBeEqualTo(1);
        }

        @Test
        void canHandleRequestToReadWhenEndOfStreamReached() throws IOException, NoSuchAlgorithmException {
            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();
            getGivenTheByteReadSizeSetTo(7092);

            // when
            whenReadToByteArrayIsCalledUntilEndOfStream();

            // then
            thenCallToReadShouldReturnMinusOne();
        }

        @Test
        void canHandleIntegrityCheckFailure() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse(
                    "10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                            + System.lineSeparator() + "19,20,21" + System.lineSeparator(),
                    "bad-checksum");
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadToByteArrayIsCalledAndExceptionIsEncountered();

            // then
            thenExceptionClassShouldBe(IOException.class);
        }

        private void whenReadToByteArrayIsCalledAndExceptionIsEncountered() {
            throwable = Assertions.assertThrows(Exception.class, this::whenReadToByteArrayIsCalledUntilEndOfStream);
        }

        private void whenReadToByteArrayIsCalledUntilEndOfStream() throws IOException {
            byte[] readBytes = new byte[bytesToRead];
            int bytesRead = 0;
            StringBuilder sb = new StringBuilder();

            while ((bytesRead = dais.read(readBytes)) != -1) {
                sb.append(new String(readBytes, 0, bytesRead));
                readCounter += 1;
            }
            actualDataRead = sb.toString();
        }

        private void thenCallToReadShouldReturnMinusOne() throws IOException {
            MatcherAssert.assertThat(dais.read(new byte[1024]), Matchers.equalTo(-1));
        }
    }

    private void thenExceptionClassShouldBe(Class<IOException> expected) {
        MatcherAssert.assertThat(throwable.getClass(), Matchers.is(Matchers.equalTo(expected)));
    }

    @Nested
    class InputStreamRead {

        @Test
        void canReadToByteArrayFromSingleGetPartResponse() throws NoSuchAlgorithmException, IOException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6 "
                    + System.lineSeparator() + "7,8,9");
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadIsCalledUntilEndOfStream();

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6 "
                    + System.lineSeparator() + "7,8,9");
        }

        @Test
        void canReadToByteArrayFromMultipleGetPartResponse() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadIsCalledUntilEndOfStream();

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator() + "10,11,12"
                    + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18" + System.lineSeparator()
                    + "19,20,21" + System.lineSeparator() + "22,23,24"
                    + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30" + System.lineSeparator()
                    + "31,32,33" + System.lineSeparator());
        }

        @Test
        void canHandleRequestToReadWhenEndOfStreamReached() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadIsCalledUntilEndOfStream();

            // then
            thenCallToReadShouldReturnMinusOne();
        }

        @Test
        void canHandleRequestToCloseStream() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenCloseIsCalledOnTheStream();

            // then
            thenCallToReadShouldReturnMinusOne();
        }

        @Test
        void canHandleIntegrityCheckFailure() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse(
                    "22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                            + System.lineSeparator() + "31,32,33" + System.lineSeparator(),
                    "bad-checksum");
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadIsCalledAndExceptionIsEncountered();

            // then
            thenExceptionClassShouldBe(IOException.class);
        }

        private void whenReadIsCalledAndExceptionIsEncountered() {
            throwable = Assertions.assertThrows(Exception.class, this::whenReadIsCalledUntilEndOfStream);
        }

        private void thenCallToReadShouldReturnMinusOne() throws IOException {
            MatcherAssert.assertThat(dais.read(), Matchers.equalTo(-1));
        }

        private void whenReadIsCalledUntilEndOfStream() throws IOException {

            int byteRead;
            StringBuilder sb = new StringBuilder();

            while ((byteRead = dais.read()) != -1) {
                sb.append(new String(new byte[] {(byte) byteRead}));
            }
            actualDataRead = sb.toString();
        }

        private void whenCloseIsCalledOnTheStream() throws IOException {
            dais.close();
        }
    }

    @Nested
    class InputStreamReadToByteArrayWithLen {

        @Test
        void canReadToByteArrayFromSingleGetPartResponse() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6 "
                    + System.lineSeparator() + "7,8,9");
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadToByteArrayIsCalledUntilEndOfStream();

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6 "
                    + System.lineSeparator() + "7,8,9");
            thenReadCounterShouldBeEqualTo(5);
        }

        @Test
        void canReadToByteArrayFromMultipleGetPartResponse() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadToByteArrayIsCalledUntilEndOfStream();

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator() + "10,11,12"
                    + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18" + System.lineSeparator()
                    + "19,20,21" + System.lineSeparator() + "22,23,24"
                    + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30" + System.lineSeparator()
                    + "31,32,33" + System.lineSeparator());
            thenReadCounterShouldBeEqualTo(20);
        }

        @Test
        void canHandleRequestToReadWhenEndOfStreamReached() throws IOException, NoSuchAlgorithmException {
            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();
            getGivenTheByteReadSizeSetTo(7092);

            // when
            whenReadToByteArrayIsCalledUntilEndOfStream();

            // then
            thenCallToReadShouldReturnMinusOne();
        }

        @Test
        void canHandleRequestToReadIntoRange() throws IOException, NoSuchAlgorithmException {
            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse("22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                    + System.lineSeparator() + "31,32,33" + System.lineSeparator());
            givenTheDigestAwareInputStreamCreatedWithResponses();
            getGivenTheByteReadSizeSetTo(8);

            // when
            whenReadToByteArrayIsCalledForSpecificRange(4, 4);

            // then
            thenDataReadShouldBeEqualTo("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator() + "10,11,12"
                    + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18" + System.lineSeparator()
                    + "19,20,21" + System.lineSeparator() + "22,23,24"
                    + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30" + System.lineSeparator()
                    + "31,32,33" + System.lineSeparator());
        }

        @Test
        void canHandleIntegrityCheckFailure() throws IOException, NoSuchAlgorithmException {

            // given
            givenPartResponse("A,B,C" + System.lineSeparator() + "1,2,3" + System.lineSeparator() + "4,5,6"
                    + System.lineSeparator() + "7,8,9" + System.lineSeparator());
            givenPartResponse("10,11,12" + System.lineSeparator() + "13,14,15" + System.lineSeparator() + "16,17,18"
                    + System.lineSeparator() + "19,20,21" + System.lineSeparator());
            givenPartResponse(
                    "22,23,24" + System.lineSeparator() + "25,26,27" + System.lineSeparator() + "28,29,30"
                            + System.lineSeparator() + "31,32,33" + System.lineSeparator(),
                    "bad-checksum");
            givenTheDigestAwareInputStreamCreatedWithResponses();

            // when
            whenReadToByteArrayWithRangeIsCalledAndExceptionIsEncountered(2, 2);

            // then
            thenExceptionClassShouldBe(IOException.class);
        }

        private void whenReadToByteArrayWithRangeIsCalledAndExceptionIsEncountered(int startPos, int len) {
            throwable = Assertions.assertThrows(
                    Exception.class, () -> whenReadToByteArrayIsCalledForSpecificRange(startPos, len));
        }

        private void thenCallToReadShouldReturnMinusOne() throws IOException {
            MatcherAssert.assertThat(dais.read(new byte[1024], 0, 10), Matchers.equalTo(-1));
        }

        private void whenReadToByteArrayIsCalledForSpecificRange(int startPos, int len) throws IOException {
            byte[] readBytes = new byte[bytesToRead];
            int bytesRead = 0;
            StringBuilder sb = new StringBuilder();

            while ((bytesRead = dais.read(readBytes, startPos, len)) != -1) {
                sb.append(new String(readBytes, startPos, bytesRead));
                readCounter += 1;
            }
            actualDataRead = sb.toString();
        }

        private void whenReadToByteArrayIsCalledUntilEndOfStream() throws IOException {
            byte[] readBytes = new byte[bytesToRead];
            int bytesRead = 0;
            StringBuilder sb = new StringBuilder();

            while ((bytesRead = dais.read(readBytes, 0, readBytes.length)) != -1) {
                sb.append(new String(readBytes, 0, bytesRead));
                readCounter += 1;
            }
            actualDataRead = sb.toString();
        }
    }

    private void thenDataReadShouldBeEqualTo(String expected) {
        MatcherAssert.assertThat(actualDataRead, Matchers.is(Matchers.equalTo(expected)));
    }

    private void thenReadCounterShouldBeEqualTo(int expected) {
        MatcherAssert.assertThat(readCounter, Matchers.is(Matchers.equalTo(expected)));
    }

    private void getGivenTheByteReadSizeSetTo(int bytesToRead) {
        this.bytesToRead = bytesToRead;
    }

    private void givenTheDigestAwareInputStreamCreatedWithResponses() throws IOException {
        dais = IntegrityCheckingInputStream.builder().parts(responses).build();
    }

    private void givenPartResponse(String data) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data.getBytes());
        ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
        responses.add(GetPartResponse.builder()
                .content(is)
                .head(Head.builder()
                        .checksum(Base64.getEncoder().encodeToString(md.digest()))
                        .build())
                .build());
    }

    private void givenPartResponse(String data, String checksum) {
        ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
        responses.add(GetPartResponse.builder()
                .content(is)
                .head(Head.builder().checksum(checksum).build())
                .build());
    }
}
