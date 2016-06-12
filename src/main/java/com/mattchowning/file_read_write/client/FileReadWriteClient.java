package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.client.calls.*;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FileReadWriteClient {

    private OAuthToken oAuthToken;

    void retrieveFileContent(@NotNull Consumer<String> consumer) {
        Call<String> call = new GetFileCall(oAuthToken, this);
        call.execute(consumer);
    }

    void updateFileContent(@NotNull String newFileContent, @NotNull Consumer<String> consumer) {
        Call<String> call = new PostFileCall(oAuthToken, newFileContent, this);
        call.execute(consumer);
    }

    void retrieveOAuthToken(Consumer<OAuthToken> consumer,
                            String username,
                            String password) {
        Call<OAuthToken> call = new GetOAuthCall(username, password);
        call.execute(oAuthToken -> {
            this.oAuthToken = oAuthToken;
            consumer.accept(oAuthToken);
        });
    }

    public void refreshOAuthToken(Consumer<OAuthToken> consumer) {
        Call<OAuthToken> call = new RefreshOAuthTokenCall(oAuthToken.refreshToken);
        call.execute(oAuthToken -> {
            this.oAuthToken = oAuthToken;
            consumer.accept(oAuthToken);
        });
    }
}
