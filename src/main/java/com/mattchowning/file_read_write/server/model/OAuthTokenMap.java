package com.mattchowning.file_read_write.server.model;

import java.util.HashMap;
import java.util.Map;

public class OAuthTokenMap {

    private Map<String, OAuthToken> issuedTokens = new HashMap<>();
    private Map<String, OAuthToken> issuedRefreshTokens = new HashMap<>();

    public boolean addToken(OAuthToken newToken) {
        if (issuedTokens.containsKey(newToken.accessToken)) {
            System.err.println("Error: token already added to TokenMap");
            return false;
        } else {
            issuedTokens.put(newToken.accessToken, newToken);
            issuedRefreshTokens.put(newToken.refreshToken, newToken);
            return true;
        }
    }

    public OAuthToken removeToken(String refreshToken) {
        return removeToken(issuedRefreshTokens.get(refreshToken));
    }

    private OAuthToken removeToken(OAuthToken token) {
        issuedRefreshTokens.remove(token.refreshToken);
        return issuedTokens.remove(token.accessToken);
    }

    public OAuthToken getWithAccessToken(String accessToken) {
        return issuedTokens.get(accessToken);
    }

    public boolean containsAccessToken(String accessToken) {
        return issuedTokens.containsKey(accessToken);
    }

    public boolean containsRefreshToken(String refreshToken) {
        return issuedRefreshTokens.containsKey(refreshToken);
    }
}
