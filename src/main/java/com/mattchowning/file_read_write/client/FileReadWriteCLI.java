package com.mattchowning.file_read_write.client;

import java.util.Scanner;

public class FileReadWriteCLI {

    private static final String GET_SELECTION = "g";
    private static final String POST_SELECTION = "p";
    private static final String EXIT_SELECTION = "e";

    private static final Scanner scanner = new Scanner(System.in);

    private static FileReadWriteClient client;

    public static void main(String[] args) throws Exception {
        client = new FileReadWriteClient();
        requestAuth();
    }

    private static void requestAuth() {
        String username = getUserInput("username");
        String password = getUserInput("password");
        client.retrieveOAuthToken(oAuthModel -> {
            if (oAuthModel == null) {
                System.out.println("Unable to authenticate. Try again.");
                requestAuth();
            } else {
                getUserSelection();
            }
        }, username, password);
    }

    private static void getUserSelection() {
        switch (askForUserSelection()) {
            case GET_SELECTION:
                client.retrieveFileContent(contents -> {
                    System.out.println("file contents: " + contents);
                    getUserSelection();
                });
                break;
            case POST_SELECTION:
                String newFileContent = askForUserRequestedFileContent();
                client.updateFileContent(newFileContent, updatedContent -> {
                    System.out.println("file contents: " + updatedContent);
                    getUserSelection();
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
        String question = String.format("Would you like to Get the file, Post changes to the file, or Exit [%s/%s/%s]?",
                                        GET_SELECTION,
                                        POST_SELECTION,
                                        EXIT_SELECTION);
        System.out.println();
        System.out.println(question);
        return scanner.nextLine();
    }

    private static String askForUserRequestedFileContent() {
        System.out.println("What file content would you like to post?");
        return scanner.nextLine();
    }

    private static String getUserInput(String inputDescription) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(String.format("Enter your %s: ", inputDescription));
        return scanner.nextLine();
    }
}
