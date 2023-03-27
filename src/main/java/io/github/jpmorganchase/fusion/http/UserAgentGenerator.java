package io.github.jpmorganchase.fusion.http;

public class UserAgentGenerator {

    private static final String PRODUCT_NAME = "fusion-java-sdk";
    private static final String PRODUCT_VERSION =
            UserAgentGenerator.class.getPackage().getImplementationVersion();

    public static String getUserAgentString(Class<?> clientClass) {
        String productVersion = PRODUCT_VERSION == null ? "UNKNOWN" : PRODUCT_VERSION;
        String version = System.getProperty("java.version");
        return String.format("%s/%s (%s) Java/%s", PRODUCT_NAME, productVersion, clientClass.getSimpleName(), version);
    }
}
