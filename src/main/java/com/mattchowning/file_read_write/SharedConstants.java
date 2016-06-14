package com.mattchowning.file_read_write;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.Charset;

import io.netty.util.CharsetUtil;

public class SharedConstants {

    public static final String OAUTH_PATH = "/oauth";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    public static final String PASSWORD_KEY = "password";
    public static final String USERNAME_KEY = "username";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final Charset RESPONSE_CHARSET = CharsetUtil.UTF_8;
    public static final String FILE_HOST = "localhost";
    public static final int FILE_PORT = 8081;
    public static final String OAUTH_HOST = FILE_HOST;
    public static final int OAUTH_PORT = 8080;

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
}
