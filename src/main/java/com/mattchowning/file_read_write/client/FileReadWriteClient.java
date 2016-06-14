package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.client.calls.*;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FileReadWriteClient {

    private OAuthToken oAuthToken;
    private Consumer<OAuthToken> setOAuthToken = token -> this.oAuthToken = token;

    void getFileContent(@NotNull Consumer<String> consumer) {
        Call<String> call = new GetFileCall(oAuthToken, this);
        call.execute(consumer);
    }

    void updateFileContent(@NotNull String newFileContent, @NotNull Consumer<String> consumer) {
        Call<String> call = new PostFileCall(oAuthToken, newFileContent, this);
        call.execute(consumer);
    }

    void getOAuthToken(@NotNull Consumer<OAuthToken> externalConsumer,
                       String username,
                       String password) {
        Call<OAuthToken> call = new GetOAuthCall(username, password);
        call.execute(setOAuthToken.andThen(externalConsumer));
    }

    public void refreshOAuthToken(@NotNull Consumer<OAuthToken> externalConsumer) {
        Call<OAuthToken> call = new RefreshOAuthCall(oAuthToken.refreshToken);
        call.execute(setOAuthToken.andThen(externalConsumer));
    }
}
