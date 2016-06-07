package com.mattchowning.file_read_write.server.model;

import java.util.Base64;

import io.netty.util.CharsetUtil;

public class OAuthModel {

    private static final String PROPER_TOKEN_TYPE = "Bearer";

    public final String accessToken;
    public final String tokenType;

    public OAuthModel(String accessToken) {
        this(accessToken, PROPER_TOKEN_TYPE);
    }

    public OAuthModel(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public static OAuthModel fromEncodedAuthorizationHeader(String encodedAuthorizationHeader) {
        String tokenType = getTokenType(encodedAuthorizationHeader);
        String token = getAccessToken(encodedAuthorizationHeader);
        return new OAuthModel(token, tokenType);
    }

    public boolean hasValidTokenType() {
        return PROPER_TOKEN_TYPE.equals(tokenType);
    }

    public String getEncodedAuthorizationHeader() {
        // FIXME why is UTF-8 right here?
        byte[] encodedTokenBytes = Base64.getEncoder().encode(accessToken.getBytes());
        return String.format("%s %s", tokenType, new String(encodedTokenBytes, CharsetUtil.UTF_8));
    }

    public static String getAccessToken(String authorizationHeader) {
        String[] authHeaderArray = authorizationHeader.split("\\s");
        String encodedToken = authHeaderArray[1]; // TODO catch exception?
        byte[] decodedTokenBytes = Base64.getDecoder().decode(encodedToken);
        return new String(decodedTokenBytes, CharsetUtil.UTF_8);
    }

    private static String getTokenType(String encodedAuthorizationHeader) {
        String[] authHeaderElements = encodedAuthorizationHeader.split("\\s");
        return authHeaderElements[0]; // TODO catch exception?
    }
}
