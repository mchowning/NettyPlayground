package com.mattchowning.file_read_write.server.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class OAuthTokenMap {

    private Set<OAuthToken> issuedTokens = new HashSet<>();

    public boolean add(OAuthToken oAuthToken) {
        if (contains(oAuthToken)) {
            System.err.println("Error: token already added to TokenMap");
            return false;
        } else {
            issuedTokens.add(oAuthToken);
            return true;
        }
    }

    public boolean contains(OAuthToken token) {
        return issuedTokens.stream()
                           .anyMatch(issuedToken -> token == issuedToken);
    }

    public boolean containsAccessToken(String accessToken) {
        return issuedTokens.stream()
                           .anyMatch(issuedToken -> issuedToken.accessToken.equals(accessToken));
    }

    public boolean containsRefreshToken(String refreshToken) {
        return issuedTokens.stream()
                           .anyMatch(issuedToken -> issuedToken.refreshToken.equals(refreshToken));
    }

    public OAuthToken getWithAccessToken(String accessToken) {
        return getFirstMatch(issuedToken -> issuedToken.accessToken.equals(accessToken));
    }

    public OAuthToken getWithRefreshToken(String refreshToken) {
        return getFirstMatch(issuedToken -> issuedToken.refreshToken.equals(refreshToken));
    }

    private OAuthToken getFirstMatch(Predicate<OAuthToken> predicate) {
        return issuedTokens.stream()
                           .filter(predicate)
                           .findFirst()
                           .get();
    }

    public boolean removeToken(OAuthToken token) {
        return issuedTokens.remove(token);
    }
}
