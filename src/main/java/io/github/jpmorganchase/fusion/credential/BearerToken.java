package io.github.jpmorganchase.fusion.credential;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Value
@AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class BearerToken {
    private final String token;
    private final long expiry;
    private final Instant expiryTime;
    private final boolean isExpirable;

    public static BearerToken of(OAuthServerResponse oAuthServerResponse, long currentTimeInMillis){
        Instant now = Instant.ofEpochMilli(currentTimeInMillis);
        Instant expiryTime = now.plusSeconds(oAuthServerResponse.getExpiresIn() - 30);
        long expiry = expiryTime.toEpochMilli();

        return new BearerToken(oAuthServerResponse.getAccessToken(), expiry, expiryTime, true);
    }

    public static BearerToken of(String token){
        Instant expiry = Instant.MAX;
        return new BearerToken(token, expiry.toEpochMilli(), expiry, false);
    }

    public boolean hasTokenExpired(long currentTimeMillis) {
        return isExpirable() && expiry < currentTimeMillis;
    }

    public ZonedDateTime getPrettyExpiryTime(){
        return expiryTime.atZone(ZoneId.systemDefault());
    }

}
