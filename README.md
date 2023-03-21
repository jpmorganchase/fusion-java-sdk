# Fusion Java SDK
A Java SDK for the Fusion platform API

Fusion by J.P. Morgan is a cloud-native data platform for institutional investors, providing end-to-end data management, analytics, and reporting solutions across the investment lifecycle. The platform allows clients to seamlessly integrate and combine data from multiple sources into a single data model that delivers the benefits and scale and reduces costs, along with the ability to more easily unlock timely analysis and insights. Fusion's open data architecture supports flexible distribution, including partnerships with cloud and data providers, all managed by J.P. Morgan data experts.

For more information, please visit fusion.jpmorgan.com

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

##### With a pre-existing bearer token

https://github.com/jpmorganchase/fusion-java-sdk/blob/1273ab6a7ebb8ab2ab5f5a3143d095a316ac7d58/src/test/java/io/github/jpmorganchase/fusion/example/FusionInstanceCreationExamples.java#L28-L30

Here _BEARER_TOKEN_ is the String value of a bearer token you have retrieved which provides access to the Fusion API. You can use this mechanism in cases where you already have a means to retrieve the token and would prefer to manage that within your application than having the SDK manage that on your behalf. 

#### With an OAUth client ID and secret

https://github.com/jpmorganchase/fusion-java-sdk/blob/1273ab6a7ebb8ab2ab5f5a3143d095a316ac7d58/src/test/java/io/github/jpmorganchase/fusion/example/FusionInstanceCreationExamples.java#L35-L37

This will configure the SDK to retrieve a bearer token from an OAuth server using the supplied parameters:

* _CLIENT_ID_ - A valid OAuth client identifier
* _CLIENT_SECRET_ - A valid OAuth client secret
* _RESOURCE_ - The OAUth audience
* _AUTH_SERVER_URL_ - URL for the OAuth authentication server

When configured in this way, the SDK will retrieve the token from the OAuth server prior to the first call you make to Fusion. On each subsequent call the same token will be re-used until it is close to expiry, at which point the SDK will retrieve a new token. Use this option if you want the SDK to manage the tokens on your behalf.

#### Loading the OAuth configuration from a file

#### Using the SDK

ONce you have initialised the Fusion object, you can interact with it to retrieve metadata or download distribution files for any datasets that you need.

Examples:

1. Download some metadata
2. Download as a file
3. Download as a stream
