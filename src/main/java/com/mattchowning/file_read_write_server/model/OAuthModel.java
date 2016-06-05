package com.mattchowning.file_read_write_server.model;

import java.util.Base64;

import io.netty.util.CharsetUtil;

public class OAuthModel {

    private static final String PROPER_TOKEN_TYPE = "Bearer";

    public final String access_token;
    public final String token_type;

    public OAuthModel(String access_token) {
        this(access_token, PROPER_TOKEN_TYPE);
    }

    public OAuthModel(String accessToken, String tokenType) {
        this.access_token = accessToken;
        this.token_type = tokenType;
    }

    public static OAuthModel fromEncodedAuthorizationHeader(String encodedAuthorizationHeader) {
        String tokenType = getTokenType(encodedAuthorizationHeader);
        String token = getAccessToken(encodedAuthorizationHeader);
        return new OAuthModel(token, tokenType);
    }

    public boolean hasValidTokenType() {
        return PROPER_TOKEN_TYPE.equals(token_type);
    }

    public String getEncodedAuthorizationHeader() {
        // FIXME why is UTF-8 right here?
        byte[] encodedTokenBytes = Base64.getEncoder().encode(access_token.getBytes());
        return String.format("%s %s", token_type, new String(encodedTokenBytes, CharsetUtil.UTF_8));
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
