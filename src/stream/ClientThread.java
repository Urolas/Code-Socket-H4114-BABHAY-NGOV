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


	private Socket clientSocket; //socket
	private BufferedReader socIn; //reader flow
	private PrintStream socOut; //writer flow

	private String username; //the client's username (because can't be stock in socket)
	private ClientThread lastContact=null;
	private String lastMessageUsername;

	//Constructor
	ClientThread(Socket s) {
		this.clientSocket = s;
		this.lastMessageUsername = "";
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

				  //send a private message to someone
				  if(line.startsWith("/msg ") || line.startsWith("/r ")){
					  String name = "";
					  String message = "";
					  if (line.startsWith("/msg ")){
						  name = line.split(" ")[1];
						  message = line.substring(line.indexOf(name) + name.length() + 1);
					  }else if (line.startsWith("/r ")){
						  name = lastMessageUsername;
						  message = line.substring(3);
					  }

					  ClientThread receiver = EchoServerMultiThreaded.getUserByUsername(name);
					  System.out.println(username+ " to "+ name+ " : \""+message+"\"");

					  //If they send a message to their own username
					  if(receiver!=null && receiver.clientSocket==clientSocket) {
						  socOut.println("You can't send a message to yourself!");

					  //If there's no problem
					  }else if(receiver!=null && receiver.clientSocket != null){
						  receiver.socOut.println(reformatMsg(message,username));
						  LogManager.writeOnUserLog(receiver.getUsername(), reformatMsgForLog(message,username));
						  receiver.setLastMessageUsername(username);
						  socOut.println(reformatMsg(message,"You to "+receiver.getUsername()));
						  LogManager.writeOnUserLog(username,reformatMsgForLog(message,"You to "+receiver.getUsername()));

					  //If the username isn't online
					  }else{
						  socOut.println("This username doesn't exist or isn't online");
					  }
				  }



				  //leave the chat
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
		return (ANSI_BLUE+" ["+hour+":"+minute+"] "+ username + ANSI_RESET + " : "+line);
  	}

	public String reformatMsgForLog(String line, String username){
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		return (" ["+hour+":"+minute+"] "+ username + " : "+line);
	}

	public String getUsername() {
		return username;
	}

	public void setLastMessageUsername(String lastMessageUsername) {
		this.lastMessageUsername = lastMessageUsername;
	}
}

  
