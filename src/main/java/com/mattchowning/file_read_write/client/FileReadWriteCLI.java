package com.mattchowning.file_read_write.client;

import java.util.Scanner;

public class FileReadWriteCLI {

    private static final String GET_SELECTION = "g";
    private static final String POST_SELECTION = "p";
    private static final String EXIT_SELECTION = "e";

    private static final Scanner scanner = new Scanner(System.in);

    private static FileReadWriteClient client;

    public static void main(String[] args) throws Exception {
        client = new FileReadWriteClient(FileReadWriteCLI::getUserSelection);
        getUserSelection();
    }

    private static void getUserSelection() {
        switch (askForUserSelection()) {
            case GET_SELECTION:
                System.out.println("Get action selected.");
                client.retrieveFileContent();
                break;
            case POST_SELECTION:
                String newFileContent = askForUserRequestedFileContent();
                client.updateFileContent(newFileContent);
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
        System.out.println("Post action selected.");
        System.out.println("What file content would you like to post?");
        return scanner.nextLine();
    }

}
