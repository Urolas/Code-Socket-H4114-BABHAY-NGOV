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
	
	private Socket clientSocket; //socket
	private BufferedReader socIn; //reader flow
	private PrintStream socOut; //writer flow

	private String username; //the client's username (because can't be stock in socket)
	ClientThread(Socket s) {
		this.clientSocket = s;
	}

 	/**
  	* receives a request from client then sends an echo to the client
  	**/
	public void run() {
    	  try {
			  socIn = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
			  socOut = new PrintStream(clientSocket.getOutputStream());

			  username = socIn.readLine().trim(); //get the first line of the flow: the client's username

			  while (true) {
				  String line = socIn.readLine();
				  socOut.println(line);
				  System.out.println(reformatMsg(line)); //print the message on EchoServer

				  // /show online command to show everybody connected on the server
				  if(line.equals("/show online")){
					  System.out.println("Users online: ");
					  List<ClientThread> listClients =  EchoServerMultiThreaded.getClientThreadList();
					  for (ClientThread c : listClients){
						  String username = c.getUsername();
						  System.out.println(username + " " + clientSocket.getInetAddress());
					  }
				  }

				  if(line.startsWith("/msg ")){
					  String name = line.split(" ")[1];
					  String message = line.substring(line.indexOf(name) + name.length() + 1);
					  ClientThread receiver = EchoServerMultiThreaded.getUserByUsername(name);
					  System.out.println(username+ " to "+ name+ " : \""+message+"\"");

					  if(receiver!= null) {
						  receiver.socOut.println(reformatMsg(message));
					  }else{
						  socOut.println("This username doesn't exist or isn't online");
					  }

				  }



			  }
    	} catch (Exception e) {
			  System.err.println("Error in EchoServer:" + e);
        }
       }

   	public String reformatMsg(String line){
		return (username + " : \" "+line+" \"");
  	}

	public String getUsername() {
		return username;
	}
  
  }

  
