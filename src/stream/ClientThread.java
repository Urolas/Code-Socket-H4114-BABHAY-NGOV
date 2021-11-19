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
import java.time.LocalDateTime;

public class ClientThread
	extends Thread {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_OFF = "\033[0m";

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
				  System.out.println(reformatMsg(line,username)); //print the message on EchoServer

				  // /show online command to show everybody connected on the server
				  if(line.equals("/show all")){
					  socOut.println("Users online: ");
					  List<ClientThread> listClients =  EchoServerMultiThreaded.getClientThreadList();
					  for (ClientThread c : listClients){
						  String username = c.getUsername();
						  socOut.println(username + " " + clientSocket.getInetAddress());
					  }
				  }

				  if(line.startsWith("/msg ")){
					  String name = line.split(" ")[1];
					  String message = line.substring(line.indexOf(name) + name.length() + 1);
					  ClientThread receiver = EchoServerMultiThreaded.getUserByUsername(name);
					  System.out.println(username+ " to "+ name+ " : \""+message+"\"");

					  if(receiver.clientSocket==clientSocket) {
						  socOut.println("You can't send a message to yourself!");
					  }else if(receiver.clientSocket != null){
						  receiver.socOut.println(reformatMsg(message,username));
						  socOut.println(reformatMsg(message,"You to "+receiver.getUsername()));
					  }else{
						  socOut.println("This username doesn't exist or isn't online");
					  }
				  }

				  if(line.equals("/quit")){
					  EchoServerMultiThreaded.removeClientFromThreadList(this);
					  return;
				  }



			  }
    	} catch (Exception e) {
			  System.err.println("Error in EchoServer:" + e);
        }
       }

   	public String reformatMsg(String line, String username){
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		return (ANSI_BLUE+" ["+hour+":"+minute+"] "+ username + ANSI_OFF + " : "+line);
  	}

	public String getUsername() {
		return username;
	}
  
  }

  
