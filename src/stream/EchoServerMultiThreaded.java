/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */

package stream;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class EchoServerMultiThreaded  {
  
 	/**
  	* main method
  	* 
  	**/
       public static void main(String args[]){ 
        ServerSocket listenSocket;
		List<ClientThread> clientThreadList = new ArrayList<>();
        
  	if (args.length != 1) {
          System.out.println("Usage: java EchoServer <EchoServer port>");
          System.exit(1);
  	}
	try {
		listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
		System.out.println("Server ready..."); 
		while (true) {
			Socket clientSocket = listenSocket.accept();
			System.out.println("Connexion from:" + clientSocket.getInetAddress());
			ClientThread ct = new ClientThread(clientSocket,clientThreadList);
			ct.start();
			/*for (ClientThread otherClient : clientThreadList){
				otherClient.addClientToList(ct);
			}*/
			clientThreadList.add(ct);
		}
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
      }
  }

  
