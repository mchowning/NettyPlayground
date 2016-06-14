package com.mattchowning.file_read_write.server.model;

public class HttpError {

    public final String error;
    public final String errorDescription;

    public HttpError(String errorString, String errrorDescription) {
        error = errorString;
        errorDescription = errrorDescription;
    }
}
