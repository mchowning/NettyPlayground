package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.client.calls.Call;
import com.mattchowning.file_read_write.client.calls.GetFileCall;
import com.mattchowning.file_read_write.client.calls.GetOAuthCall;
import com.mattchowning.file_read_write.client.calls.PostFileCall;
import com.mattchowning.file_read_write.server.model.OAuthModel;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FileReadWriteClient {

    private OAuthModel oAuthModel;

    void retrieveFileContent(@NotNull Consumer<String> consumer) {
        Call<String> call = new GetFileCall(oAuthModel);
        call.execute(consumer);
    }

    void updateFileContent(@NotNull String newFileContent, @NotNull Consumer<String> consumer) {
        Call<String> call = new PostFileCall(oAuthModel, newFileContent);
        call.execute(consumer);
    }

    void retrieveOAuthToken(Consumer<OAuthModel> consumer,
                            String username,
                            String password) {
        Call<OAuthModel> call = new GetOAuthCall(username, password);
        call.execute(oAuthModel -> {
            this.oAuthModel = oAuthModel;
            consumer.accept(oAuthModel);
        });
    }
}
