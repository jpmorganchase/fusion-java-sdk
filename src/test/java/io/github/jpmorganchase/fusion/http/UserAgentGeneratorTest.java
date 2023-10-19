package io.github.jpmorganchase.fusion.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class UserAgentGeneratorTest {

    @Test
    void testMyUserAgent() {
        String agent = UserAgentGenerator.getUserAgentString(JdkClient.class);
        String javaVersion = System.getProperty("java.version");
        assertThat(agent, is(equalTo("fusion-java-sdk/UNPACKAGED (JdkClient) Java/" + javaVersion)));
    }
}
