package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.Scanner;

public class FileReadWriteClientCLI {

    private static final String LOGIN_LOGOUT = "l";
    private static final String GET_SELECTION = "g";
    private static final String POST_SELECTION = "p";
    private static final String EXIT_SELECTION = "e";

    private static FileReadWriteClient client;

    public static void main(String[] args) throws Exception {
        client = new FileReadWriteClient();
        getUserSelection();
    }

    private static void getUserSelection() {
        System.out.println();
        displayAuthMessage();
        switch (askForUserSelection()) {
            case LOGIN_LOGOUT:
                if (client.isAuthorized()) {
                    client.logout();
                    getUserSelection();
                } else {
                    login();
                }
                break;
            case GET_SELECTION:
                retrieveFile();
                break;
            case POST_SELECTION:
                updateFile();
                break;
            case EXIT_SELECTION:
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("Invalid selection.");
                getUserSelection();
        }
    }

    private static void login() {
        String username = getUserInput("username");
        String password = getUserInput("password");
        HandlerCallback<OAuthToken> callback = new HandlerCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
                getUserSelection();
            }

            @Override
            public void onError() {
                System.out.println("Unable to authenticate.");
                getUserSelection();
            }
        };
        client.getOAuthToken(callback, username, password);
    }

    private static void retrieveFile() {
        client.getFileContent(new HandlerCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String messageFormat = client.isAuthorized()
                                       ? "File content: %s"
                                       : "Encoded file content: %s";
                String message = String.format(messageFormat, result);
                System.out.println(message);
                getUserSelection();
            }

            @Override
            public void onError() {
                System.out.println("Error retrieving file");
                getUserSelection();
            }
        });
    }

    private static void updateFile() {
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
    }

    private static void displayAuthMessage() {
        String message;
        if (client.isAuthorized()) {
            message = "You are logged in.";
        } else {
            message = "You are not logged in.";
        }
        System.out.println(message);
    }

    private static String askForUserSelection() {
        Scanner scanner = new Scanner(System.in);
        String authAction = client.isAuthorized() ? "Logout" : "Login";
        String question = String.format("Would you like to %s, Get the file, Post changes to the file, or Exit?\n[ %s / %s / %s / %s ]",
                                        authAction,
                                        LOGIN_LOGOUT,
                                        GET_SELECTION,
                                        POST_SELECTION,
                                        EXIT_SELECTION);
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
