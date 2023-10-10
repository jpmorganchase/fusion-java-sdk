package io.github.jpmorganchase.fusion.oauth.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BearerTokenTest {

    @Test
    public void testHasTokenExpiredWhenNotExpirable() {
        BearerToken token = BearerToken.of("my-token");
        assertThat(token.hasTokenExpired(System.currentTimeMillis()), is(false));
    }

    @Test
    public void testOfConstructsAsExpected() {
        String token = "my-token";
        long currentTimeInMillis = 1625666400000L;
        long expiresIn = 3600L;

        BearerToken bearerToken = BearerToken.of(token, currentTimeInMillis, expiresIn);

        assertNotNull(bearerToken);
        assertEquals(token, bearerToken.getToken());
        assertEquals(currentTimeInMillis + (expiresIn - 30) * 1000, bearerToken.getExpiry());
        assertTrue(bearerToken.isExpirable());
    }

    @Test
    public void testHasTokenExpiredWhenCanExpire() {
        String token = "my-token";
        long currentTimeInMillis = 1625666400000L;
        long expiresIn = 3600L;

        BearerToken bearerToken = BearerToken.of(token, currentTimeInMillis, expiresIn);

        long expiry = bearerToken.getExpiry();

        assertTrue(bearerToken.hasTokenExpired(expiry + 1));
        assertFalse(bearerToken.hasTokenExpired(expiry - 1));
    }
}
