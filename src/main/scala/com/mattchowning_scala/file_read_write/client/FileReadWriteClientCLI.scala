package com.mattchowning_scala.file_read_write.client

import java.util.Scanner

object FileReadWriteClientCLI {

  private val GET_SELECTION: String = "g"
  private val POST_SELECTION: String = "p"
  private val EXIT_SELECTION: String = "e"

  @throws(classOf[Exception])
  def main(args: Array[String]) {
    val client: FileReadWriteClient = new FileReadWriteClient
    requestAuth(client)
  }

  private def requestAuth(client: FileReadWriteClient) {
    val username: String = getUserInput("username")
    val password: String = getUserInput("password")
    client.retrieveOAuthToken(oAuthModel => {
      if (oAuthModel == null) {
        System.out.println("Unable to authenticate. Try again.")
        requestAuth(client)
      } else {
        retrieveUserSelection(client)
      }
    }, username, password)
  }

  private def retrieveUserSelection(client: FileReadWriteClient) {
    askForUserSelection match {
      case GET_SELECTION =>
        client.retrieveFileContent(contents => {
          System.out.println("file contents: " + contents)
          retrieveUserSelection(client)
        })
      case POST_SELECTION =>
        val newFileContent: String = askForUserRequestedFileContent
        client.updateFileContent(newFileContent, updatedContent => {
          System.out.println("file contents: " + updatedContent)
          retrieveUserSelection(client)
        })
      case EXIT_SELECTION =>
        System.out.println("Exiting...")
      case _ =>
        System.out.println("Invalid selection.")
        retrieveUserSelection(client)
    }
  }

  private def askForUserSelection: String = {
    val scanner: Scanner = new Scanner(System.in)
    val question: String = String.format("Would you like to Get the file, Post changes to the file, or Exit [%s/%s/%s]?", GET_SELECTION, POST_SELECTION, EXIT_SELECTION)
    System.out.println()
    System.out.println(question)
    scanner.nextLine
  }

  private def askForUserRequestedFileContent: String = {
    val scanner: Scanner = new Scanner(System.in)
    System.out.println("What file content would you like to post?")
    scanner.nextLine
  }

  private def getUserInput(inputDescription: String): String = {
    val scanner: Scanner = new Scanner(System.in)
    System.out.print(String.format("Enter your %s: ", inputDescription))
    scanner.nextLine
  }
}