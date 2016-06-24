package com.mattchowning.file_read_write.client.handler;

public interface HandlerCallback<T> {
    void onSuccess(T result);
    void onError();
}
