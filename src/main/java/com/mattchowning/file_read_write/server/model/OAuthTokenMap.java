package com.mattchowning.file_read_write.server.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class OAuthTokenMap {

    private Set<OAuthToken> issuedTokens = new HashSet<>();

    public boolean add(OAuthToken oAuthToken) {
        boolean alreadyContainsToken = issuedTokens.stream()
                                                   .anyMatch(issuedToken -> oAuthToken == issuedToken);
        if (alreadyContainsToken) {
            System.err.println("Error: token already added to TokenMap");
            return false;
        } else {
            issuedTokens.add(oAuthToken);
            return true;
        }
    }

    public boolean containsAccessToken(OAuthToken oAuthToken) {
        return issuedTokens.stream()
                           .anyMatch(issuedToken -> issuedToken.getAccessToken().equals(oAuthToken.getAccessToken()));
    }

    public boolean containsRefreshToken(String refreshToken) {
        return issuedTokens.stream()
                           .anyMatch(issuedToken -> issuedToken.getRefreshToken().equals(refreshToken));
    }

    public OAuthToken getWithAccessToken(String accessToken) {
        return getFirstMatch(issuedToken -> issuedToken.getAccessToken().equals(accessToken));
    }

    public OAuthToken getWithRefreshToken(String refreshToken) {
        return getFirstMatch(issuedToken -> issuedToken.getRefreshToken().equals(refreshToken));
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
