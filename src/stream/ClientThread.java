/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

package stream;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientThread
	extends Thread {
	
	private Socket clientSocket;
	
	ClientThread(Socket s) {
		this.clientSocket = s;
	}

 	/**
  	* receives a request from client then sends an echo to the client
  	**/
	public void run() {
    	  try {
			  BufferedReader socIn = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
			  PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
			  while (true) {
				  String line = socIn.readLine();
				  socOut.println(line);
				  System.out.println(line);

				  // /show online command to show everybody connected on the server
				  if(line.equals("/show online")){
					  System.out.println("Users online: ");
					  List<ClientThread> listClients =  EchoServerMultiThreaded.getClientThreadList();
					  for (ClientThread c : listClients){
						  String username = c.clientSocket.getInetAddress().toString();
						  System.out.println(username);
					  }
				  }
			  }
    	} catch (Exception e) {
			  System.err.println("Error in EchoServer:" + e);
        }
       }
  
  }

  
