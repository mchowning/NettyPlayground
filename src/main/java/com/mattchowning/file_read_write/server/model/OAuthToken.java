package com.mattchowning.file_read_write.server.model;

import com.mattchowning.file_read_write.server.TokenGenerator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;

import io.netty.util.CharsetUtil;

public class OAuthToken {

    public static final long TOKEN_DURATION_IN_SECONDS = 7;  // small value for testing purposes

    private static final String PROPER_TOKEN_TYPE = "Bearer";

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final Long expiresIn = TOKEN_DURATION_IN_SECONDS;
    private final Long generatedTime;

    public OAuthToken() {
        this(TokenGenerator.generateNew(), TokenGenerator.generateNew());
    }

    public OAuthToken(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, PROPER_TOKEN_TYPE);
    }

    private OAuthToken(String accessToken, String refreshToken, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.generatedTime = getCurrentTime();
    }

    private long getCurrentTime() {
        return ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond();
    }

    public boolean isExpired() {
        return getCurrentTime() - generatedTime > TOKEN_DURATION_IN_SECONDS;
    }

    public boolean hasValidTokenType() {
        return PROPER_TOKEN_TYPE.equals(tokenType);
    }

    public String getEncodedAuthorizationHeader() {
        byte[] encodedTokenBytes = Base64.getEncoder().encode(accessToken.getBytes());
        return String.format("%s %s", tokenType, new String(encodedTokenBytes, CharsetUtil.UTF_8));
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public Long getGeneratedTime() {
        return generatedTime;
    }
}
