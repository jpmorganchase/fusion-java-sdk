# Fusion Java SDK

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.jpmorganchase.fusion/fusion-sdk)](https://github.com/jpmorganchase/fusion-java-sdk/releases)
[![Java Docs](https://javadoc.io/badge2/io.github.jpmorganchase.fusion/fusion-sdk/javadoc.svg)](https://javadoc.io/doc/io.github.jpmorganchase.fusion/fusion-sdk)
[![Workflow](https://github.com/jpmorganchase/fusion-java-sdk/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/jpmorganchase/fusion-java-sdk/actions/workflows/build.yml)

A Java SDK for the Fusion platform API

Fusion by J.P. Morgan is a cloud-native data platform for institutional investors, providing end-to-end data management, analytics, and reporting solutions across the investment lifecycle. The platform allows clients to seamlessly integrate and combine data from multiple sources into a single data model that delivers the benefits and scale and reduces costs, along with the ability to more easily unlock timely analysis and insights. Fusion's open data architecture supports flexible distribution, including partnerships with cloud and data providers, all managed by J.P. Morgan data experts.

For more information, please visit [fusion.jpmorgan.com](https://fusion.jpmorgan.com/)

## Usage

### Acquiring

The Fusion SDK is published to Maven Central and can be retrieved using standard dependency resolution tools:

#### Apache Maven

```xml    
  <dependency>
    <groupId>io.github.jpmorganchase.fusion</groupId>
    <artifactId>fusion-sdk</artifactId>
    <version>${fusion.version}</version>
  </dependency>
```

#### Gradle

```
implementation 'io.github.jpmorganchase.fusion:fusion-sdk:${fusion.version}'
```

### Getting started

#### Imports

```java
import io.github.jpmorganchase.fusion.Fusion;
```

#### Creating the Fusion object

Once you have the dependency added to your project and imports configured, you will need an instance of the Fusion class to interact with the API. This will be the primary way to use Fusion from your own code. To create an instance, use the builder from the class. Examples below show different ways to intialise the Fusion object depending on your authentication mechanism.

##### With an OAUth client ID and secret

```java
Fusion fusion = Fusion.builder()
                        .secretBasedCredentials(CLIENT_ID, CLIENT_SECRET, RESOURCE, AUTH_SERVER_URL)
                        .build();
```

This will configure the SDK to retrieve a bearer token from an OAuth server using the supplied parameters:

* _CLIENT_ID_ - A valid OAuth client identifier
* _CLIENT_SECRET_ - A valid OAuth client secret
* _RESOURCE_ - The OAUth audience
* _AUTH_SERVER_URL_ - URL for the OAuth authentication server

When configured in this way, the SDK will retrieve the token from the OAuth server prior to the first call you make to Fusion. On each subsequent call the same token will be re-used until it is close to expiry, at which point the SDK will retrieve a new token. Use this option if you want the SDK to manage the tokens on your behalf.

##### Loading the OAuth configuration from a file

```java
Fusion fusion = Fusion.builder().configuration(FusionConfiguration.builder()
            .credentialsPath(CREDENTIALS_FILE_PATH)
            .build())
        .build();
```

This will configure the SDK to retrieve a bearer token from an OAuth server using configuration details stored in a file at the supplied path _CREDENTIAL_FILE_PATH_

Configuration files are JSON in the following format:

```json
{
  "resource": "JPMC:URI:RS-12345-App-ENV",
  "client_secret": "aClientSecret",
  "auth_url": "https://authserver.domain.com/as/token.oauth2",
  "client_id": "aClientId"
}
```

Where:

* _resource_ - The OAUth audience
* _client_secret_ - A valid OAuth client secret
* _auth_url_ - URL for the OAuth authentication server
* _client_id_ - A valid OAuth client identifier

Similar to the above option, this will configure the SDK to manage the tokens on your behalf. Use this option if you want the OAuth configuration to be stored on the local filesystem.


##### With a pre-existing bearer token

```java
Fusion fusion = Fusion.builder().bearerToken(BEARER_TOKEN).build();
```

Here _BEARER_TOKEN_ is the String value of a bearer token you have retrieved which provides access to the Fusion API. You can use this mechanism in cases where you already have a means to retrieve the token and would prefer to manage that within your application than having the SDK manage that on your behalf.

Note than when your token has expired, you will need to pass a new token to the Fusion object by calling _updateBearerToken_, passing the new value.

##### Overriding attributes using FusionConfiguration

```java
Fusion fusion = Fusion.builder().configuration(FusionConfiguration.builder()
         .build())
        .build();
```

* _rootURL_ - Defines the fusion root url to be used with api interactions.  Defaults to "https://fusion-api.jpmorgan.com/fusion/v1/".
* _credentialsPath_ - Defines the path to the credentials file for auth/authz. Defaults to "config/client_credentials.json".
* _defaultCatalog_ - Set the default catalog to be used with simplified API calls. Defaults to "common"
* _downloadPath_ - Configures the path where distributions should be downloaded to. Defaults to "downloads"
* _singlePartUploadSizeLimit_ - Max size in MB of data allowed for a single part upload.  if 32MB was the max size then 32 would be provided. Defaults to 50.
* _uploadPartSize_ - Upload part chunk size. If a value such as 8MB is required, then client would set this value to 8.  Defaults to 16MB.
* _uploadThreadPoolSize_ - Size of Thread-Pool to be used for uploading chunks of a multipart file. Defaults to number of available processors.
* _downloadThreadPoolSize_ - Size of Thread-Pool to be used for uploading chunks of a multipart file. Defaults to number of available processors.
* _digestAlgorithm_ - Digest algorithm used by fusion to verify the integrity of upload/downloads. Defaults to SHA-256.

#### Using the SDK

Once you have initialised the Fusion object, you can interact with it to retrieve metadata or download distribution files for any datasets that you need.

##### Examples:

1. List catalogs
```java
Map<String, Catalog> catalogs = fusion.listCatalogs();
```
2. List datasets
```java
Map<String, Dataset> datasets = fusion.listDatasets("my-catalog");
```
3. Download some dataset metadata
```java
Map<String, Attribute> attributes = fusion.listAttributes("my-catalog", "my-dataset");
```
4. List the series members available in the dataset
```java
Map<String, DatasetSeries> members = fusion.listDatasetMembers("my-catalog", "my-dataset");
```
5. List the distributions available in the dataset member
```java
Map<String, Distribution> distributions = fusion.listDistributions("my-catalog", "my-dataset", "my-series-member");
```
6. Download as a file
```java
fusion.download("my-catalog", "my-dataset", "my-series-member", "csv", "/downloads/distributions");
```
7. Download as a stream
```java
InputStream is = fusion.downloadStream("my-catalog", "my-dataset", "my-series-member", "csv");
```

#### Logging

The Fusion SDK makes log calls to the SLF4J API. If you wish to see the logging you must configure an SLF4J implementation for your application. See the [SLF4J manual](https://www.slf4j.org/manual.html#swapping) for details.

#### Exception Handling

All exceptions thrown from calls to the Fusion object are runtime exceptions. These are documented in the Javadoc for the class itself. Runtime exceptions are used in place of checked exception in order to provide flexibility to users to handle exceptions within the most appropriate layer of your application, without requiring catching, wrapping and rethrowing.
