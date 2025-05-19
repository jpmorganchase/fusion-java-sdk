package io.github.jpmorganchase.fusion.serializing;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.github.jpmorganchase.fusion.model.AlternativeId;
import io.github.jpmorganchase.fusion.model.DataNodeId;
import io.github.jpmorganchase.fusion.model.Domain;
import io.github.jpmorganchase.fusion.model.Report;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class GsonAPIRequestSerializerReportTest {

    @Test
    public void testReportSerializesCorrectly() {
        // Given
        Report report = Report.builder()
                .name("Sample Report")
                .tierType("Report")
                .lob("LOB")
                .dataNodeId(new DataNodeId("id", "name", "type"))
                .alternativeId(new AlternativeId(new Domain("id", "name"), "value"))
                .varArg("title", "Report Title")
                .varArg("description", "Report Description")
                .varArg("category", "Report Category")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(report);

        // Then
        String expected = loadTestResource("report-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testReportWithoutVarArgsSerializesCorrectly() {
        // Given
        Report report = Report.builder()
                .name("Sample Report")
                .tierType("Report")
                .lob("LOB")
                .dataNodeId(new DataNodeId("id", "name", "type"))
                .alternativeId(new AlternativeId(new Domain("id", "name"), "value"))
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(report);

        // Then
        String expected = loadTestResource("report-no-varags-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testReportWithNullValues() {
        // Given
        Report report = Report.builder()
                .name("Sample Report")
                .tierType(null)
                .lob(null)
                .dataNodeId(new DataNodeId("id", "name", null))
                .alternativeId(new AlternativeId(new Domain("id", null), "value"))
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(report);

        // Then
        String expected = loadTestResource("report-with-nulls-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testReportWithSpecialCharacters() {
        // Given
        Report report = Report.builder()
                .name("Sample Report")
                .tierType("Special characters: \"quotes\", \n newlines, and \u2022 bullets")
                .lob("LOB")
                .dataNodeId(new DataNodeId("id", "name", "type"))
                .alternativeId(new AlternativeId(new Domain("id", "name"), "value"))
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(report);

        // Then
        String expected = loadTestResource("report-special-characters.json");
        assertThat(actual, is(equalTo(expected)));
    }

    private static String loadTestResource(String resourceName) {
        URL url = GsonAPIRequestSerializer.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
