package io.github.jpmorganchase.fusion.api.request;

import java.io.InputStream;
import java.net.URL;
import lombok.SneakyThrows;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@SuppressWarnings("SameParameterValue")
class UploadRequestTest {

    private URL fileResource;
    private UploadRequest ur;
    private String catalog;
    private String dataset;
    private String apiPath;
    private String createdDate;
    private String fromDate;
    private String toDate;
    private int maxSinglePartFileSize;

    @Test
    public void shouldBuildFromFile() {

        givenResource("/io/github/jpmorganchase/fusion/api/large-upload-test.csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenAPIPath("https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
        givenCreatedDate("20230522");
        givenFromDate("20230520");
        givenToDate("20230521");
        givenMaxSinglePartFileSize(25);

        whenUploadRequestBuildIsCalledForFileResource();

        thenCatalogShouldEqual("common");
        thenDatasetShouldEqual("API_TEST");
        thenCreatedDateShouldEqual("20230522");
        thenFromDateShouldEqual("20230520");
        thenToDateShouldEqual("20230521");
        thenIsFromStreamShouldEqual(false);
        thenAPIPathShouldEqual(
                "https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
    }

    @Test
    public void shouldBuildFromStream() {

        givenResource("/io/github/jpmorganchase/fusion/api/large-upload-test.csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenAPIPath("https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
        givenCreatedDate("20230522");
        givenFromDate("20230520");
        givenToDate("20230521");
        givenMaxSinglePartFileSize(25);

        whenUploadRequestBuildIsCalledForStream();

        thenCatalogShouldEqual("common");
        thenDatasetShouldEqual("API_TEST");
        thenCreatedDateShouldEqual("20230522");
        thenFromDateShouldEqual("20230520");
        thenToDateShouldEqual("20230521");
        thenIsFromStreamShouldEqual(true);
        thenAPIPathShouldEqual(
                "https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
    }

    @Test
    public void shouldIdentifyAsMultipartUploadCandidateWhenFromFile() {

        givenResource("/io/github/jpmorganchase/fusion/api/large-upload-test.csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenAPIPath("https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
        givenCreatedDate("20230522");
        givenFromDate("20230520");
        givenToDate("20230521");
        givenMaxSinglePartFileSize(25);

        whenUploadRequestBuildIsCalledForFileResource();

        thenIsMultipartUploadCandidateShouldEqual(true);
    }

    @Test
    public void shouldIdentifyAsMultipartUploadCandidateWhenFromStream() {

        givenResource("/io/github/jpmorganchase/fusion/api/large-upload-test.csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenAPIPath("https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
        givenCreatedDate("20230522");
        givenFromDate("20230520");
        givenToDate("20230521");
        givenMaxSinglePartFileSize(25);

        whenUploadRequestBuildIsCalledForStream();

        thenIsMultipartUploadCandidateShouldEqual(true);
    }

    @Test
    public void shouldIdentifyAsSinglePartUploadCandidateWhenFromFile() {

        givenResource("/io/github/jpmorganchase/fusion/api/large-upload-test.csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenAPIPath("https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
        givenCreatedDate("20230522");
        givenFromDate("20230520");
        givenToDate("20230521");
        givenMaxSinglePartFileSize(26);

        whenUploadRequestBuildIsCalledForFileResource();

        thenIsMultipartUploadCandidateShouldEqual(false);
    }

    @Test
    public void shouldIdentifyAsSinglePartUploadCandidateWhenFromStream() {

        givenResource("/io/github/jpmorganchase/fusion/api/large-upload-test.csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenAPIPath("https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
        givenCreatedDate("20230522");
        givenFromDate("20230520");
        givenToDate("20230521");
        givenMaxSinglePartFileSize(26);

        whenUploadRequestBuildIsCalledForStream();

        thenIsMultipartUploadCandidateShouldEqual(false);
    }

    @Test
    @SneakyThrows
    public void shouldBuildFromStreamAndWholeFileShouldBeAvailable() {

        givenResource("/io/github/jpmorganchase/fusion/api/large-upload-test.csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenAPIPath("https://fusion.com/v1/catalogs/common/datset/API_TEST/datasetseries/20230522/distributions/csv");
        givenCreatedDate("20230522");
        givenFromDate("20230520");
        givenToDate("20230521");
        givenMaxSinglePartFileSize(25);

        whenUploadRequestBuildIsCalledForStream();

        thenEntireFileShouldBeAvailable(27000006);
    }

    @SneakyThrows
    private void thenEntireFileShouldBeAvailable(int expected) {
        InputStream is = ur.getData();

        byte[] buffer = new byte[1024 * 1024];
        int totalBytes = 0;
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            totalBytes += bytesRead;
        }

        MatcherAssert.assertThat(totalBytes, Matchers.is(Matchers.equalTo(expected)));
    }

    private void thenIsMultipartUploadCandidateShouldEqual(boolean expected) {
        MatcherAssert.assertThat(ur.isMultiPartUploadCandidate(), Matchers.is(Matchers.equalTo(expected)));
    }

    private void whenUploadRequestBuildIsCalledForFileResource() {
        ur = UploadRequest.builder()
                .fromFile(fileResource.getPath())
                .maxSinglePartFileSize(maxSinglePartFileSize)
                .catalog(this.catalog)
                .dataset(this.dataset)
                .apiPath(this.apiPath)
                .createdDate(this.createdDate)
                .fromDate(this.fromDate)
                .toDate(this.toDate)
                .build();
    }

    @SneakyThrows
    private void whenUploadRequestBuildIsCalledForStream() {
        ur = UploadRequest.builder()
                .fromStream(fileResource.openStream())
                .maxSinglePartFileSize(maxSinglePartFileSize)
                .catalog(this.catalog)
                .dataset(this.dataset)
                .apiPath(this.apiPath)
                .createdDate(this.createdDate)
                .fromDate(this.fromDate)
                .toDate(this.toDate)
                .build();
    }

    private void givenToDate(String toDate) {
        this.toDate = toDate;
    }

    private void givenFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    private void givenCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    private void givenAPIPath(String apiPath) {
        this.apiPath = apiPath;
    }

    private void givenDataset(String dataset) {
        this.dataset = dataset;
    }

    private void givenCatalog(String catalog) {
        this.catalog = catalog;
    }

    private void givenMaxSinglePartFileSize(int maxSinglePartFileSize) {
        this.maxSinglePartFileSize = maxSinglePartFileSize;
    }

    private void thenAPIPathShouldEqual(String expected) {
        MatcherAssert.assertThat(ur.getApiPath(), Matchers.is(Matchers.equalTo(expected)));
    }

    private void thenIsFromStreamShouldEqual(boolean expected) {
        MatcherAssert.assertThat(ur.isFromStream(), Matchers.is(expected));
    }

    private void thenToDateShouldEqual(String expected) {
        MatcherAssert.assertThat(ur.getToDate(), Matchers.is(Matchers.equalTo(expected)));
    }

    private void thenFromDateShouldEqual(String expected) {
        MatcherAssert.assertThat(ur.getFromDate(), Matchers.is(Matchers.equalTo(expected)));
    }

    private void thenCreatedDateShouldEqual(String expected) {
        MatcherAssert.assertThat(ur.getCreatedDate(), Matchers.is(Matchers.equalTo(expected)));
    }

    private void thenDatasetShouldEqual(String expected) {
        MatcherAssert.assertThat(ur.getDataset(), Matchers.is(Matchers.equalTo(expected)));
    }

    private void thenCatalogShouldEqual(String expected) {
        MatcherAssert.assertThat(ur.getCatalog(), Matchers.is(Matchers.equalTo(expected)));
    }

    private void givenResource(String fileName) {
        fileResource = UploadRequestTest.class.getResource(fileName);
    }
}
