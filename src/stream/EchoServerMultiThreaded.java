/***
 * EchoServer.java
 * @authors Annie Abhay, Sophanna NGOV
 */

package stream;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages the chat-room server while handling multiple client threads
 */
public class EchoServerMultiThreaded  {


	private static List<ClientThread> clientThreadList = new ArrayList<>();

 	/**
  	* Main method. Runs the server and wait for a new client to connect to the port
	 * @param args contains the number of the port the socket will pass through
  	**/
	 public static void main(String args[]){

        ServerSocket listenSocket;
        
		if (args.length != 1) {
			  System.out.println("Usage: java EchoServer <EchoServer port>");
			  System.exit(1);
		}
		try {
			listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
			System.out.println("Server ready...");
			while (true) {
				Socket clientSocket = listenSocket.accept();
				ClientThread ct = new ClientThread(clientSocket);
				ct.start();
				System.out.println("Connexion from: " + clientSocket.getInetAddress());
				clientThreadList.add(ct);
			}
			} catch (Exception e) {;
				System.err.println("Error in EchoServer:" + e);
			}
	 }

	/**
	 * Find the list containing the ClientThread of all the connected users
	 * @return the list of all online users
	 */
	public static List<ClientThread> getClientThreadList() {
		return clientThreadList;
	}

	/**
	 * Search for the client thread that matches the username
	 * @param username
	 * @return the ClientThread of this username. return null if this username isn't online
	 */
	public static ClientThread getUserByUsername(String username){

		 for (ClientThread client : clientThreadList){
			 if(client.getUsername().equals(username)){
				 return client;
			 }
		 }
		 return null;
	}

	/**
	 * When the client disconnects with /quit, remove their name from the client list
	 * @param ct the ClientThread of the user who will disconnect
	 */
	public static void removeClientFromThreadList(ClientThread ct){
		clientThreadList.removeIf((ClientThread removableClient) -> removableClient == ct);
	}
}

  
