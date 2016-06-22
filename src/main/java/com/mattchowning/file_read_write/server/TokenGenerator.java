package com.mattchowning.file_read_write.server;

import java.security.SecureRandom;

public class TokenGenerator {

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int TOKEN_LENGTH = 20;
    private static final SecureRandom RND = new SecureRandom();

    public static String generateNew(){
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            char newChar = AB.charAt(RND.nextInt(AB.length()));
            sb.append(newChar);
        }
        return sb.toString();
    }
}
