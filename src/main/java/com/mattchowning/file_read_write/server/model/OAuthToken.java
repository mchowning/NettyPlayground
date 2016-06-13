package com.mattchowning.file_read_write.server.model;

import com.mattchowning.file_read_write.server.TokenGenerator;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;

import io.netty.util.CharsetUtil;

public class OAuthToken {

    public static final long TOKEN_DURATION_IN_SECONDS = 10;

    private static final String PROPER_TOKEN_TYPE = "Bearer";

    public final String accessToken;
    public final String refreshToken;
    public final String tokenType;
    public final Long expiresIn = TOKEN_DURATION_IN_SECONDS;
    public final Long generatedTime;

    public static OAuthToken generateNew() {
        String tokenValue = TokenGenerator.generateNew();
        String refreshTokenValue = TokenGenerator.generateNew();
        return new OAuthToken(tokenValue, refreshTokenValue);
    }

    private OAuthToken(String accessToken, String refreshToken) {
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

    public static OAuthToken fromEncodedAuthorizationHeader(String encodedAuthorizationHeader) {
        String tokenType = getTokenType(encodedAuthorizationHeader);
        String token = getAccessToken(encodedAuthorizationHeader);
        return new OAuthToken(token, tokenType);
    }

    public boolean hasValidTokenType() {
        return PROPER_TOKEN_TYPE.equals(tokenType);
    }

    public String getEncodedAuthorizationHeader() {
        byte[] encodedTokenBytes = Base64.getEncoder().encode(accessToken.getBytes());
        return String.format("%s %s", tokenType, new String(encodedTokenBytes, CharsetUtil.UTF_8));
    }

    @NotNull
    private static String getAccessToken(@NotNull String authorizationHeader) {
        String[] authHeaderArray = authorizationHeader.split("\\s");
        if (authHeaderArray.length < 2) {
            return "";
        } else {
            String encodedToken = authHeaderArray[1];
            byte[] decodedTokenBytes = Base64.getDecoder().decode(encodedToken);
            return new String(decodedTokenBytes, CharsetUtil.UTF_8);
        }
    }

    @NotNull
    private static String getTokenType(@NotNull String encodedAuthorizationHeader) {
        String[] authHeaderElements = encodedAuthorizationHeader.split("\\s");
        return authHeaderElements[0];
    }
}
