package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.Scanner;

public class FileReadWriteClientCLI {

    private static final String GET_SELECTION = "g";
    private static final String POST_SELECTION = "p";
    private static final String EXIT_SELECTION = "e";

    private static FileReadWriteClient client;

    public static void main(String[] args) throws Exception {
        client = new FileReadWriteClient();
        requestAuth();
    }

    private static void requestAuth() {
        String username = getUserInput("username");
        String password = getUserInput("password");
        HandlerCallback<OAuthToken> callback = new HandlerCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
                getUserSelection();
            }

            @Override
            public void onError() {
                System.out.println("Unable to authenticate. Try again.");
                requestAuth();
            }
        };
        client.getOAuthToken(callback, username, password);
    }

    private static void getUserSelection() {
        switch (askForUserSelection()) {
            case GET_SELECTION:
                client.getFileContent(new HandlerCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println("File contents: " + result);
                        getUserSelection();
                    }

                    @Override
                    public void onError() {
                        System.out.println("Error retrieving file");
                        getUserSelection();
                    }
                });
                break;
            case POST_SELECTION:
                String newFileContent = askForUserRequestedFileContent();
                client.updateFileContent(newFileContent, new HandlerCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println("file contents: " + result);
                        getUserSelection();
                    }

                    @Override
                    public void onError() {
                        System.out.println("Error updating file.");
                        getUserSelection();
                    }
                });
                break;
            case EXIT_SELECTION:
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("Invalid selection.");
                getUserSelection();
        }
    }

    private static String askForUserSelection() {
        Scanner scanner = new Scanner(System.in);
        String question = String.format("Would you like to Get the file, Post changes to the file, or Exit [%s/%s/%s]?",
                                        GET_SELECTION,
                                        POST_SELECTION,
                                        EXIT_SELECTION);
        System.out.println();
        System.out.println(question);
        return scanner.nextLine();
    }

    private static String askForUserRequestedFileContent() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("What file content would you like to post?");
        return scanner.nextLine();
    }

    private static String getUserInput(String inputDescription) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(String.format("Enter your %s: ", inputDescription));
        return scanner.nextLine();
    }
}
