# Fusion Java SDK
A Java SDK for the Fusion platform API

Fusion by J.P. Morgan is a cloud-native data platform for institutional investors, providing end-to-end data management, analytics, and reporting solutions across the investment lifecycle. The platform allows clients to seamlessly integrate and combine data from multiple sources into a single data model that delivers the benefits and scale and reduces costs, along with the ability to more easily unlock timely analysis and insights. Fusion's open data architecture supports flexible distribution, including partnerships with cloud and data providers, all managed by J.P. Morgan data experts.

For more information, please visit [fusion.jpmorgan.com](https://fusion.jpmorgan.com/)

## Usage

***

:warning: This SDK is undergoing active development towards a stable release. While the version remains < 1.0.0 there may be some changes to the public API. We will endeavour to avoid that or keep to a minimum where possible

***
### Acquiring

The Fusion SDK is published to Maven Central and can be retrieved from there using standard dependency resolution tools. For example, with Maven, add this to the dependencies in your POM:

```xml    
  <dependency>
    <groupId>io.github.jpmorganchase.fusion</groupId>
    <artifactId>fusion-sdk</artifactId>
    <version>0.0.1</version>
  </dependency>
```

### Getting started

#### Imports

```java
import io.github.jpmorganchase.fusion.Fusion;
```

#### Creating the Fusion object

Once you have the dependency added to your project and imports configured, you will need an instance of the Fusion class to interact with the API. This will be the primary way to use Fusion from your own code. To create an instance, use the builder from the class. Examples below show different ways to intialise the Fusion object depending on your authentication mechanism.

##### With an OAUth client ID and secret

https://github.com/jpmorganchase/fusion-java-sdk/blob/3300f986bb55bf70ed0e36953c9834b8caa9960d/src/test/java/io/github/jpmorganchase/fusion/example/FusionInstanceCreationExamples.java#L35-L37

This will configure the SDK to retrieve a bearer token from an OAuth server using the supplied parameters:

* _CLIENT_ID_ - A valid OAuth client identifier
* _CLIENT_SECRET_ - A valid OAuth client secret
* _RESOURCE_ - The OAUth audience
* _AUTH_SERVER_URL_ - URL for the OAuth authentication server

When configured in this way, the SDK will retrieve the token from the OAuth server prior to the first call you make to Fusion. On each subsequent call the same token will be re-used until it is close to expiry, at which point the SDK will retrieve a new token. Use this option if you want the SDK to manage the tokens on your behalf.

##### Loading the OAuth configuration from a file

https://github.com/jpmorganchase/fusion-java-sdk/blob/3300f986bb55bf70ed0e36953c9834b8caa9960d/src/test/java/io/github/jpmorganchase/fusion/example/FusionInstanceCreationExamples.java#L43-L45

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

https://github.com/jpmorganchase/fusion-java-sdk/blob/3300f986bb55bf70ed0e36953c9834b8caa9960d/src/test/java/io/github/jpmorganchase/fusion/example/FusionInstanceCreationExamples.java#L28-L30

Here _BEARER_TOKEN_ is the String value of a bearer token you have retrieved which provides access to the Fusion API. You can use this mechanism in cases where you already have a means to retrieve the token and would prefer to manage that within your application than having the SDK manage that on your behalf.

Note than when your token has expired, you will need to pass a new token to the Fusion object by calling _updateBearerToken_, passing the new value. 

#### Using the SDK

Once you have initialised the Fusion object, you can interact with it to retrieve metadata or download distribution files for any datasets that you need.

Examples (to follow):

1. Download some metadata
2. Download as a file
3. Download as a stream

#### Logging

The Fusion SDK makes log calls to the SLF4J API. If you wish to see the logging you must configure an SLF4J implementation for your application. See the [SLF4J manual](https://www.slf4j.org/manual.html#swapping) for details.

#### Exception Handling

All exceptions thrown from calls to the Fusion object are runtime exceptions. These are documented in the Javadoc for the class itself. Runtime exceptions are used in place of checked exception in order to provide flexibility to users to handle exceptions within the most appropriate layer of your application, without requiring catching, wrapping and rethrowing.