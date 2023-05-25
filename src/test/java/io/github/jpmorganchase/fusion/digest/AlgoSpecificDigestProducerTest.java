package io.github.jpmorganchase.fusion.digest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.ApiInputValidationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AlgoSpecificDigestProducerTest {

    private AlgoSpecificDigestProducer algoSpecificDigestProducer;

    private ByteArrayInputStream inputData;

    private DigestDescriptor digestDescriptor;

    @Test
    public void testValidDescriptorReturnedForSha256Algo() {
        givenSha256SpecificDigestProducer();
        givenInputData("A,B,C".getBytes());
        whenExecuteIsCalled();
        thenDescriptorShouldHaveExpectedDigest("KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=");
        thenDescriptorShouldHaveExpectedSize(5);
        thenDescriptorShouldHaveExpectedContent("A,B,C".getBytes());
    }

    @Test
    public void testSha256IsDefaultedWhenNoAlgoSpecifiedInBuilder() {
        givenDigestProducerWithNoAlgoSpecified();
        givenInputData("A,B,C".getBytes());
        whenExecuteIsCalled();
        thenDescriptorShouldHaveExpectedDigest("KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=");
    }

    @Test
    public void testValidDescriptorReturnedWhenAlternateAlgoSpecified() {
        givenMd5SpecificDigestProducer();
        givenInputData("A,B,C".getBytes());
        whenExecuteIsCalled();
        thenDescriptorShouldHaveExpectedDigest("zgS+Eiblb0jaVbbBMNRblA==");
        thenDescriptorShouldHaveExpectedSize(5);
        thenDescriptorShouldHaveExpectedContent("A,B,C".getBytes());
    }

    @Test
    public void testBehaviourWhenEmptyInputDataProvided() {
        givenSha256SpecificDigestProducer();
        givenInputData(new byte[0]);
        whenExecuteIsCalled();
        thenDescriptorShouldHaveExpectedDigest("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
        thenDescriptorShouldHaveExpectedSize(0);
        thenDescriptorShouldHaveExpectedContent(new byte[0]);
    }

    @Test
    public void testBehaviourWhenNullInputDataProvided() {
        givenSha256SpecificDigestProducer();
        whenExecuteIsCalledWithNullDataThenExceptionShouldBeThrown();
    }

    @Test
    public void testBehaviourWhenInvalidAlgoSpecified() {
        givenDigestProducerForAlgo("abc-512");
        givenInputData("A,B,C".getBytes());
        whenExecuteIsCalledThenExceptionShouldBeThrown(NoSuchAlgorithmException.class);
    }

    private void thenDescriptorShouldHaveExpectedContent(byte[] content) {
        assertThat(digestDescriptor.getContent(), is(equalTo(content)));
    }

    private void thenDescriptorShouldHaveExpectedSize(int size) {
        assertThat(digestDescriptor.getSize(), is(equalTo(size)));
    }

    private void thenDescriptorShouldHaveExpectedDigest(String digest) {
        assertThat(digestDescriptor.getChecksum(), is(equalTo(digest)));
    }

    private void givenSha256SpecificDigestProducer() {
        algoSpecificDigestProducer =
                AlgoSpecificDigestProducer.builder().sha256().build();
    }

    private void givenDigestProducerForAlgo(String algo) {
        algoSpecificDigestProducer =
                AlgoSpecificDigestProducer.builder().digestAlgorithm(algo).build();
    }

    private void givenMd5SpecificDigestProducer() {
        algoSpecificDigestProducer =
                AlgoSpecificDigestProducer.builder().digestAlgorithm("md5").build();
    }

    private void givenDigestProducerWithNoAlgoSpecified() {
        algoSpecificDigestProducer = AlgoSpecificDigestProducer.builder().build();
    }

    private void whenExecuteIsCalled() {
        digestDescriptor = algoSpecificDigestProducer.execute(inputData);
    }

    private <T extends Throwable> void whenExecuteIsCalledThenExceptionShouldBeThrown(Class<T> expectedType) {
        Assertions.assertThrows(expectedType, () -> algoSpecificDigestProducer.execute(inputData));
    }

    private void whenExecuteIsCalledWithNullDataThenExceptionShouldBeThrown() {
        Assertions.assertThrows(
                ApiInputValidationException.class, () -> algoSpecificDigestProducer.execute((InputStream) null));
    }

    private void givenInputData(byte[] inputData) {
        this.inputData = new ByteArrayInputStream(inputData);
    }
}
