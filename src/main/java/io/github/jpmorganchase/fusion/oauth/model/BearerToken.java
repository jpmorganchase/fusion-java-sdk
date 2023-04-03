package io.github.jpmorganchase.fusion.oauth.model;

import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.retriever.OAuthServerResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class BearerToken {
    private final String token;
    private final Long expiry;
    private final Instant expiryTime;
    private final boolean isExpirable;

    public static BearerToken of(BearerTokenCredentials credentials) {
        return BearerToken.of(credentials.getBearerToken());
    }

    public static BearerToken of(String token) {
        return new BearerToken(token, null, null, false);
    }

    public static BearerToken of(OAuthServerResponse oAuthServerResponse, long currentTimeInMillis) {
        return BearerToken.of(
                oAuthServerResponse.getAccessToken(), oAuthServerResponse.getExpiresIn(), currentTimeInMillis);
    }

    public static BearerToken of(String token, long currentTimeInMillis, long expiresIn) {
        Instant now = Instant.ofEpochMilli(currentTimeInMillis);
        Instant expiryTime = now.plusSeconds(expiresIn - 30);
        long expiry = expiryTime.toEpochMilli();

        return new BearerToken(token, expiry, expiryTime, true);
    }

    public boolean hasTokenExpired(long currentTimeMillis) {
        return isExpirable() && expiry < currentTimeMillis;
    }

    public ZonedDateTime getPrettyExpiryTime() {
        return expiryTime.atZone(ZoneId.systemDefault());
    }
}
