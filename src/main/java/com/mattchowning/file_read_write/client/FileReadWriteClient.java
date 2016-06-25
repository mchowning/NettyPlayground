package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.client.calls.*;
import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

public class FileReadWriteClient {

    private OAuthToken oAuthToken;

    void logout() {
        oAuthToken = null;
    }
    boolean isAuthorized() {
        return oAuthToken != null;
    }

    void getFileContent(HandlerCallback<String> callback) {
        Call<String> call = new GetFileCall(oAuthToken, this, callback);
        call.execute();
    }

    void updateFileContent(@NotNull String newFileContent, HandlerCallback<String> callback) {
        Call<String> call = new PostFileCall(oAuthToken, newFileContent, this, callback);
        call.execute();
    }

    void getOAuthToken(HandlerCallback<OAuthToken> callback, String username, String password) {
        Call<OAuthToken> call = new GetOAuthCall(username,
                                                 password,
                                                 setOAuthTokenWithCallback(callback));
        call.execute();
    }

    public void refreshOAuthToken(HandlerCallback<OAuthToken> callback) {
        Call<OAuthToken> call = new RefreshOAuthCall(oAuthToken.getRefreshToken(),
                                                     setOAuthTokenWithCallback(callback));
        call.execute();
    }

    private HandlerCallback<OAuthToken> setOAuthTokenWithCallback(HandlerCallback<OAuthToken> originatingCallback) {
        return new HandlerCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
                FileReadWriteClient.this.oAuthToken = result;
                originatingCallback.onSuccess(result);

            }

            @Override
            public void onError() {
                originatingCallback.onError();
            }
        };
    }
}
