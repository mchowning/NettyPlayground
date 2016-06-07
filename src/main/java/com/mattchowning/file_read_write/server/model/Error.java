package com.mattchowning.file_read_write.server.model;

public class Error {

    public final String error;
    public final String errorDescription;

    public Error(String errorString, String errrorDescription) {
        error = errorString;
        errorDescription = errrorDescription;
    }
}
