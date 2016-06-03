package com.mattchowning.file_read_write_server;

public class Error {

    private String error;
    private String error_description;

    public Error() {}

    public Error(String errorString) {
        error = errorString;
    }

    public Error(String errorString, String errrorDescription) {
        error = errorString;
        error_description = errrorDescription;
    }
}
