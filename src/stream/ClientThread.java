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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
				  if(line.equals("/online")){
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
						  LogManager.writeOnUserLog(receiver.getUsername(), reformatMsg(message,username));
						  receiver.setLastMessageUsername(username);
						  socOut.println(reformatMsg(message,"You to "+receiver.getUsername()));
						  LogManager.writeOnUserLog(username,reformatMsg(message,"You to "+receiver.getUsername()));

					  //If the username isn't online
					  }else{
						  socOut.println("This username doesn't exist or isn't online");
					  }
				  }

				  //show log of the user
				  if(line.equals("/history")){
					  String history = LogManager.getHistory(username);
					  socOut.println(history);
				  }

				  if(line.startsWith("/group")){

					  //check all group the user is in
					  if(line.trim().equals("/group")){
						  String allGroup = LogManager.getUserGroups(username);
						  socOut.println(allGroup);
					  }

					  //check who is the owner of a group
					  else if(line.startsWith("/group members ")){
						  String groupName = line.split(" ")[2];
						  socOut.println(LogManager.getGroupMembers(groupName,username));

					  }

					  //create a new group : /group create GroupA
					  else if(line.startsWith("/group create ")){
						  String groupName = line.split(" ")[2];
						  if(LogManager.findGroupInFile(groupName,username)){
							  socOut.println("Group ["+groupName+"] created");
						  }else{
							  socOut.println("Group name already exist");
						  }
					  }

					  //add member to a group : /group add GroupA Sara Eric Louis
					  else if(line.startsWith("/group add ")) {

						  boolean succeed = true;
						  String groupName = line.split(" ")[2];
						  //Check if group exist
						  if (!LogManager.groupExist(groupName)) {
							  socOut.println("Group name doesn't exist");
						  } else {
							  if (!LogManager.isGroupOwner(groupName, username)) {
								  socOut.println("Error: You don't have the permission to add people to this group");

							  } else {
								  String[] addedPeople = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");

								  //Check if every username exist
								  for (String name : addedPeople) {
									  if (!LogManager.userExist(name)) {
										  socOut.println("Error: User doesn't exist");
										  succeed = false;
										  break;
									  }
								  }

								  if (succeed && LogManager.addPeopleToGroup(groupName, addedPeople)) {
									  socOut.println(Stream.of(addedPeople).collect(Collectors.joining(",")) + " added to group [" + groupName + "]");
								  }else{
									  socOut.println("Error: You can't add people who are already on the group");
								  }

							  }
						  }

					  }else if(line.startsWith("/group remove ")){

						  String groupName = line.split(" ")[2];
						  boolean succeed=true;

						  if (!LogManager.groupExist(groupName)) {
							  socOut.println("Error: Group name doesn't exist");
						  } else {
							  if (!LogManager.isGroupOwner(groupName, username)) {
								  socOut.println("Error: You don't have the permission to remove people from this group");

							  } else {
								  String[] removedPeople = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");

								  //Check if every username exist
								  for (String name : removedPeople) {
									  if (!LogManager.userExist(name)) {
										  socOut.println("User doesn't exist");
										  succeed = false;
										  break;
									  }
								  }

								  if (succeed && LogManager.removePeopleFromGroup(groupName, removedPeople)) {
									  socOut.println(Stream.of(removedPeople).collect(Collectors.joining(",")) + " removed from group [" + groupName + "]");
								  }else{
									  socOut.println("Error: You can't remove a person who doesn't belong to the group or yourself");
								  }

							  }
						  }


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


	public String getUsername() {
		return username;
	}

	public void setLastMessageUsername(String lastMessageUsername) {
		this.lastMessageUsername = lastMessageUsername;
	}
}

  
