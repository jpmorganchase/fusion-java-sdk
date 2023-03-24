package io.github.jpmorganchase.fusion.http;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class UserAgentGeneratorTest {

    @Test
    @Disabled(
            "This needs work since in it's current implementation the value depends entirely on the actual runtime of the test")
    void testMyUserAgent() {
        String agent = UserAgentGenerator.getUserAgentString(JdkClient.class);
        assertThat(agent, is(equalTo("fusion-java-sdk/0.0.0 (JdkClient) Java/1.8.0_362")));
    }
}
