package io.github.jpmorganchase.fusion.pact.util;

import static au.com.dius.pact.consumer.dsl.PactDslJsonRootValue.stringType;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import lombok.SneakyThrows;

/**
 * These bodies exist to support the tests contained in {@link io.github.jpmorganchase.fusion.pact.FusionApiConsumerPactTest}.
 * It should be noted that any change to the bodies defined could have an adverse effect on the corresponding tests.
 */
public class BodyBuilders {

    private BodyBuilders() {}

    public static DslPart catalogs() {
        return header("catalogs/", "A list of available catalogs, catalogs", "catalogs", "Catalogs")
                .eachLike("resources")
                .stringType("@id", "common")
                .stringType("description", "A catalog of common data")
                .stringType("identifier", "common")
                .stringType("title", "Common data catalog")
                .closeArray();
    }

    public static DslPart catalogResources() {
        return header("common", "A catalog of common data", "common", "Common data catalog")
                .minArrayLike("resources", 1)
                .stringType("@id", "datasets/")
                .stringType(
                        "description", "A list of available datasets (for access or download in one or more formats)")
                .stringType("identifier", "datasets")
                .stringType("title", "Datasets")
                .closeArray();
    }

    public static DslPart products() {
        return header("products/", "A list of available products", "products", "Products")
                .minArrayLike("resources", 1)
                .minArrayLike("category", 0, stringType())
                .integerType("datasetCount", 18)
                .minArrayLike("deliveryChannel", 0, stringType())
                .stringType("description", "MSCI ESG Description")
                .stringType("identifier", "ESG00008")
                .booleanType("isActive", true)
                .minArrayLike("maintainer", 0, stringType())
                .stringType("publisher", "J.P. Morgan")
                .minArrayLike("region", 0, stringType())
                .date("releaseDate", "yyyy-MM-dd", Date.from(Instant.now()))
                .stringType("shortAbstract", "A robust ESG integration tool")
                .stringType("status", "Available")
                .minArrayLike("subCategory", 0, stringType())
                .minArrayLike("tag", 0, stringType())
                .stringType("theme", "ESG")
                .stringType("title", "MSCI ESG Ratings")
                .booleanType("isRestricted", true)
                .stringType("@id", "ESG00008/")
                .closeArray();
    }

    public static DslPart datasets() {
        return header("datasets/", "A list of available datasets", "datasets", "Datasets")
                .minArrayLike("resources", 1)
                .minArrayLike("category", 0, stringType())
                .date("createdDate", "yyyy-MM-dd", Date.from(Instant.now()))
                .stringType("description", "Premium, volatility, and greeks for EUR")
                .stringType("frequency", "Daily")
                .stringType("identifier", "GFI_OP_CF")
                .booleanType("isThirdPartyData", false)
                .booleanType("isInternalOnlyDataset", false)
                .stringType("language", "English")
                .stringType("maintainer", "J.P. Morgan DM Rates Research")
                .date("modifiedDate", "yyyy-MM-dd", Date.from(Instant.now()))
                .stringType("publisher", "J.P. Morgan")
                .minArrayLike("region", 0, stringType())
                .minArrayLike("source", 0, stringType())
                .minArrayLike("subCategory", 0, stringType())
                .stringType("title", "Swaptions Caps & Floors")
                .minArrayLike("tag", 0, stringType())
                .booleanType("isRestricted", false)
                .booleanType("isRawData", false)
                .booleanType("hasSample", false)
                .stringType("@id", "GFI_OP_CF/")
                .closeArray();
    }

    public static DslPart datasetResource() {
        return header("GFI_OP_CF/", "Premium, volatility, and greeks for EUR", "GFI_OP_CF", "Swaptions Caps & Floors")
                .minArrayLike("resources", 1)
                .stringType("@id", "datasetseries/")
                .stringType("description", "A list of available datasetseries of a dataset")
                .stringType("identifier", "datasetseries")
                .stringType("title", "Datasetseries")
                .closeArray()
                .asBody()
                .minArrayLike("category", 0, stringType())
                .date("createdDate", "yyyy-MM-dd", Date.from(Instant.now()))
                .stringType("frequency", "Daily")
                .booleanType("isThirdPartyData", false)
                .booleanType("isInternalOnlyDataset", false)
                .stringType("language", "English")
                .stringType("maintainer", "J.P. Morgan DM Rates Research")
                .date("modifiedDate", "yyyy-MM-dd", Date.from(Instant.now()))
                .stringType("publisher", "J.P. Morgan")
                .minArrayLike("region", 0, stringType())
                .minArrayLike("source", 0, stringType())
                .minArrayLike("subCategory", 0, stringType())
                .minArrayLike("tag", 0, stringType())
                .booleanType("isRestricted", false)
                .booleanType("isRawData", false)
                .booleanType("hasSample", false);
    }

    public static DslPart datasetMembers() {

        return header(
                        "datasetseries/",
                        "A list of available datasetseries of a dataset",
                        "datasetseries",
                        "DatasetSeries")
                .minArrayLike("resources", 1)
                .date("createdDate", "yyyy-MM-dd", asDate("2023-03-19"))
                .date("fromDate", "yyyy-MM-dd", asDate("2023-03-18"))
                .date("toDate", "yyyy-MM-dd", asDate("2023-03-17"))
                .stringType("identifier", "20230319")
                .stringType("@id", "20230319/")
                .closeArray();
    }

    public static DslPart datasetMemberResources() {

        PactDslJsonBody b = new PactDslJsonBody();
        return b.object("@context")
                .stringType("@vocab", "https://www.w3.org/ns/dcat3.jsondld")
                .stringType("@base", "https://fusion-api.test.aws.jpmchase.net/v1")
                .closeObject()
                .asBody()
                .stringType("@id", "20230319/")
                .stringType("identifier", "20230319")
                .minArrayLike("resources", 1)
                .stringType("@id", "distributions/")
                .stringType("description", "A list of available distributions")
                .stringType("identifier", "distributions")
                .stringType("title", "Distributions")
                .closeArray()
                .asBody()
                .date("createdDate", "yyyy-MM-dd", asDate("2023-03-19"))
                .date("fromDate", "yyyy-MM-dd", asDate("2023-03-18"))
                .date("toDate", "yyyy-MM-dd", asDate("2023-03-17"));
    }

    public static DslPart attributes() {
        return header("attributes/", "A list of available attributes", "attributes", "Attributes")
                .minArrayLike("resources", 1)
                .stringType("identifier", "A")
                .stringType("id", "4000003")
                .stringType("source", "Data Query")
                .stringType("dataType", "String")
                .stringType("description", "Description for attribute A")
                .integerType("index", 1)
                .booleanType("isDatasetKey", false)
                .stringType("sourceFieldId", "a_source_field")
                .stringType("title", "A")
                .closeArray();
    }

    public static DslPart distributions() {
        return header("distributions/", "A list of available distributions", "distributions", "Distributions")
                .minArrayLike("resources", 1)
                .stringType("description", "Snapshot data, in tabular, csv format")
                .stringType("identifier", "csv")
                .stringType("@id", "csv/")
                .stringType("title", "CSV")
                .stringType("fileExtension", ".csv")
                .stringType("mediaType", "text/csv; header=present; charset=utf-8")
                .closeArray();
    }

    @SneakyThrows
    private static Date asDate(String example) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(example);
    }

    private static PactDslJsonBody header(String id, String desc, String identifier, String title) {
        PactDslJsonBody b = new PactDslJsonBody();
        return b.object("@context")
                .stringType("@vocab", "https://www.w3.org/ns/dcat3.jsondld")
                .stringType("@base", "https://fusion-api.test.aws.jpmchase.net/v1")
                .closeObject()
                .asBody()
                .stringType("@id", id)
                .stringType("description", desc)
                .stringType("identifier", identifier)
                .stringType("Title", title);
    }
}
