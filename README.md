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

Once you have the dependency added to your project and imports configured, you will need an instance of the Fusion class to interact with the API. This will be the primary way to use Fusion from your own code. To create an instance, use the builder from the class. Examples below:

https://github.com/jpmorganchase/fusion-java-sdk/blob/91e72612ddd499009841b83951f5c9eb7ce58941/src/test/java/io/github/jpmorganchase/fusion/example/FusionInstanceCreationExamples.java#L26

2. Example with credential file
3. Example with embedded credential
etc

From there you can interact with the Fusion object to retrieve metadata or download distribution files for any datasets that you need.

Examples:

1. Download some metadata
2. Download as a file
3. Download as a stream
