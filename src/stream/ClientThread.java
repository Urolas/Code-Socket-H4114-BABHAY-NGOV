/***
 * ClientThread.java
 * @authors Annie ABHAY, Sophanna NGOV
 */

package stream;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages a client thread : read and write messages and run commands
 */
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
	private String line;

	private String username; //the client's username (because can't be stock in socket)
	private ClientThread lastContact=null;
	private String lastMessageUsername;
	private boolean connectingToGroup = false;
	private String connectedGroup = null;
	private List<ClientThread> groupMembers = new ArrayList<>();

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

				  line = socIn.readLine();
				  System.out.println(reformatMsg(line,username)); //print the message on EchoServer

				  // /show online command to show everybody connected on the server
				  if(line.equals("/online")){
					  typeOnline();
				  }

				  //if connected to group
				  else if(connectingToGroup && !line.equals("/group disconnect") && !line.equals("/help")){
					  typeMsgInGroup();
				  }

				  //send a private message to someone
				  else if(line.startsWith("/msg ") || line.startsWith("/r ")){
					  typeMsg();
				  }

				  //show log of the user
				  else if(line.equals("/history")){
					  typeHistory();
				  }

				  else if(line.startsWith("/group")){

					  //check all group the user is in
					  if(line.trim().equals("/group")){
						  typeGroup();
					  }

					  //create a new group : /group create GroupA
					  else if(line.startsWith("/group create ")){
						  typeGroupCreate();
					  }

					  //add member to a group : /group add GroupA Sara Eric Louis
					  else if(line.startsWith("/group add ")) {
						  typeGroupAddMember();


					  //remove member from a group : /group remove GroupeA Bob
					  }else if(line.startsWith("/group remove ")) {
						  typeGroupRemoveMember();

					  }else if(line.startsWith("/group delete ")){
						  typeGroupDelete();

					  //leave a group : /group leave GroupA
					  }else if(line.startsWith("/group leave ")){
						  typeGroupLeave();
					  }

					  //leave a group : /group connect GroupA
					  else if(line.startsWith("/group connect")){
						   typeGroupConnect();

					  }

					  //disconnect from current group : /group disconnect
					  else if(line.startsWith("/group disconnect") && connectingToGroup){
						  typeGroupDisconnect();
					  }

				  }

				  else if(line.equals("/help")){
					  typeHelp();
				  }

				  //leave the chat
				  else if(line.equals("/quit")){
					  typeQuit();
				  }

				  else{
					  socOut.println("Your command doesn't make sense. Check /help for more details");
				  }


			  }
    	} catch (Exception e) {
			  System.err.println("Error in EchoServer:" + e);
        }
       }

	/**
	 * Reformat the message adding the username who sent it, time and color: for private messages
	 * @param line the message to send
	 * @param username the username of the message's writer
	 * @return the reformatted string
	 */
   	public String reformatMsg(String line, String username){
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		return (ANSI_BLUE+" ["+hour+":"+minute+"] "+ username + ANSI_RESET + " : "+line);
  	}

	/**
	 * Reformat the message adding the username who sent it, time and color: for the group chat
	 * @param line the message to send
	 * @param username the username of the message's writer
	 * @return the reformatted string
	 */
	public String reformatMsgGroup(String line, String username){
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		return (ANSI_PURPLE+"["+connectedGroup+"]"+" ["+hour+":"+minute+"] "+ username + ANSI_RESET + " : "+line);
	}

	/**
	 * Reformat the message adding the username who sent it, time and color: for the log/history
	 * @param line the message to send
	 * @param username the username of the message's writer
	 * @return the reformatted string
	 */
	public String reformatMsgLog(String line, String username){
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		return (ANSI_GREEN+" ["+hour+":"+minute+"] "+ username + ANSI_RESET + " : "+line);
	}


	public String getUsername() {
		return username;
	}

	public void setLastMessageUsername(String lastMessageUsername) {
		this.lastMessageUsername = lastMessageUsername;
	}

	/**
	 * When the user types /online, show every users online
	 */
	public void typeOnline(){

		socOut.println("Users online: ");
		List<ClientThread> listClients =  EchoServerMultiThreaded.getClientThreadList();
		for (ClientThread c : listClients){
			String username = c.getUsername();
			socOut.println(username + " " + clientSocket.getInetAddress());
		}
	}

	/**
	 * When the user types something while being connected to the group, send the message to everybody in that group
	 */
	public void typeMsgInGroup(){

		if(line.startsWith("/msg ")||line.startsWith("/r ")){
			typeMsg();
		}else if(line.equals("/help")) {
			typeHelp();
		}else if(line.equals("/history")){
			typeHistoryGroup();
		}else if(line.startsWith("/")){
			socOut.println("Warning: you can't type commands other than /msg, /r, /history, /group disconnect and /help inside a group chat");
		}

		//Send msg to yourself and log
		socOut.println(reformatMsgGroup(line,"You"));
		LogManager.writeOnGroupLog(connectedGroup, reformatMsgLog(line,username));

		//send message to anybody connected
		for (ClientThread member : groupMembers){
			member.socOut.println(reformatMsgGroup(line,username));
		}


	}

	/**
	 * When the user types /msg or /r, send a private message
	 */
	public void typeMsg(){

		if(line.split(" ").length<3){
			socOut.println("Your command doesn't make sense. Check /help for more details");
			return;
		}

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
			socOut.println("Error: You can't send a message to yourself!");

			//If there's no problem
		}else if(receiver!=null && receiver.clientSocket != null){
			receiver.socOut.println(reformatMsg(message,username));
			LogManager.writeOnUserLog(receiver.getUsername(), reformatMsgLog(message,username));
			receiver.setLastMessageUsername(username);
			socOut.println(reformatMsg(message,"You to "+receiver.getUsername()));
			LogManager.writeOnUserLog(username,reformatMsgLog(message,"You to "+receiver.getUsername()));

			//If the username isn't online
		}else{
			socOut.println("Error: This username doesn't exist or isn't online");
		}
	}

	/**
	 * When the user types /history, show their personal log
	 */
	public void typeHistory(){
		String history = LogManager.getHistory(username);
		socOut.println(history);
	}

	/**
	 * When the user types /history while being connected to a group, show the group's log
	 */
	public void typeHistoryGroup(){
		String history = LogManager.getHistoryGroup(connectedGroup);
		socOut.println(history);
	}

	/**
	 * When the user types /group, show the details of all groups this user belong to
	 */
	public void typeGroup(){
		String[] allGroup = LogManager.getUserGroups(username);
		socOut.println("-------------------------");
		for (int i=0; i<allGroup.length; i=i+2){
			String groupName = allGroup[i];
			socOut.println("["+groupName+"]");
			addGroupMembers(groupName,Integer.valueOf(allGroup[i+1]));
			socOut.println("-------------------------");
		}
	}

	/**
	 * Show the names of the group's members and the online ones
	 * @param groupName
	 * @param groupNum
	 */
	public void addGroupMembers(String groupName, int groupNum){
		String msg=LogManager.getGroupMembers(groupName,username);
		List<String> connected = connectedUsersInGroup(groupName, username);
		String connectedMembers = connected.stream()
				.map(n -> String.valueOf(n))
				.collect(Collectors.joining(", "));
		if(msg == null){
			socOut.println("Error: The group name does not exist or you do not belong to this group");
		}else{
			socOut.println(groupNum+" members :"+msg);
			socOut.println(connected.size()+" currently in group chat: "+connectedMembers);
		}
	}

	/**
	 * When the user types /group create, create a new group
	 */
	public void typeGroupCreate(){
		if(line.split(" ").length<3){
			socOut.println("Your command doesn't make sense. Check /help for more details");
			return;
		}
		String groupName = line.split(" ")[2];
		if(LogManager.findGroupInFile(groupName,username)){
			socOut.println("Group ["+groupName+"] created");
		}else{
			socOut.println("Error: Group name already exist");
		}
	}

	/**
	 * When the user types /group add, add a member to the mentioned group (if they are the group's owner)
	 */
	public void typeGroupAddMember(){
		if(line.split(" ").length<3){
			socOut.println("Your command doesn't make sense. Check /help for more details");
			return;
		}
		String groupName = line.split(" ")[2];

		//Check if group exist
		if (!LogManager.groupExist(groupName)) {
			socOut.println("Error: Group name doesn't exist");
			return;
		} else {
			if (!LogManager.isGroupOwner(groupName, username)) {
				socOut.println("Error: You don't have the permission to add people to this group");
				return;

			} else {
				String[] addedPeople = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");

				//Check if every username exist
				for (String name : addedPeople) {
					if (!LogManager.userExist(name)) {
						socOut.println("Error: At least one of the username doesn't exist");
						return;
					}
				}

				if (LogManager.addPeopleToGroup(groupName, addedPeople)) {
					socOut.println(Stream.of(addedPeople).collect(Collectors.joining(",")) + " added to group [" + groupName + "]");
				}else{
					socOut.println("Error: You can't add people who are already on the group");
					return;
				}

			}
		}
	}

	/**
	 * When the user types /group remove , remove a member form that group (if the user's the group' owner)
	 */
	public void typeGroupRemoveMember(){
		if(line.split(" ").length<3){
			socOut.println("Your command doesn't make sense. Check /help for more details");
			return;
		}
		String groupName = line.split(" ")[2];

		if (!LogManager.groupExist(groupName)) {
			socOut.println("Error: Group name doesn't exist");
		} else {
			if (!LogManager.isGroupOwner(groupName, username)) {
				socOut.println("Error: You don't have the permission to remove people from this group");
				return;

			} else {
				String[] removedPeople = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");

				//Check if every username exist
				for (String name : removedPeople) {
					if (!LogManager.userExist(name)) {
						socOut.println("User doesn't exist");
						return;
					}
				}

				if (LogManager.removePeopleFromGroup(groupName, removedPeople)) {
					socOut.println(Stream.of(removedPeople).collect(Collectors.joining(",")) + " removed from group [" + groupName + "]");
				}else{
					socOut.println("Error: You can't remove a person who doesn't belong to the group or yourself");
					return;
				}

			}
		}
	}

	/**
	 * When a user types /group leave, remove them from the mentioned group
	 */
	public void typeGroupLeave(){
		if(line.split(" ").length<3){
			socOut.println("Your command doesn't make sense. Check /help for more details");
			return;
		}
		String groupName = line.split(" ")[2];
		if (!LogManager.groupExist(groupName)) {
			socOut.println("Error: Group name doesn't exist");
		} else {

			if(LogManager.leaveGroup(groupName,username)){
				socOut.println("You just left the group ["+groupName+"]");
			}else{
				socOut.println("Error: You can't leave a group if you're the owner or if you don't belong to the group");
			}
		}
	}

	/**
	 * When the user types /group connect, make them enter a group chat
	 */
	public void typeGroupConnect(){

		if(line.split(" ").length<3){
			socOut.println("Your command doesn't make sense. Check /help for more details");
			return;
		}

		if(connectingToGroup){
			socOut.println("Disconnected from group ["+connectedGroup+"]");

			//send message to anybody connected
			for (ClientThread member : groupMembers){
				member.socOut.println(username+" just disconnected from the groupChat");
			}
		}


		String groupName = line.split(" ")[2];

		if (!LogManager.groupExist(groupName)) {
			socOut.println("Error: Group name doesn't exist");
			return;
		}else{
			connectedGroup = groupName;
			String[] members = LogManager.getGroupMembers(groupName, username).split(", ");
			members[0] = members[0].substring(0,members[0].length()-7);
			if(!Arrays.asList(members).contains(username)){
				socOut.println("Error: You don't belong to this group");
				return;
			}

			//Add connected members in group
			for (String name : members){
				ClientThread thread = EchoServerMultiThreaded.getUserByUsername(name);
				if(thread!=null && thread!=this && thread.connectingToGroup && thread.connectedGroup.equals(groupName)){
					groupMembers.add(thread);
				}
			}

		}
		socOut.println("Connected to group ["+groupName+"]");
		for (ClientThread member : groupMembers){
			member.socOut.println(username+" has joined from the groupChat");
		}

		connectingToGroup = true;
	}

	/**
	 * When the user types /group disconnect, make them leave/disconnect from the current groupchat
	 */
	public void typeGroupDisconnect(){
		if(connectingToGroup) {
			socOut.println("Disconnected from group [" + connectedGroup + "]");

			//send message to anybody connected
			for (ClientThread member : groupMembers) {
				member.socOut.println(username + " just disconnected from the groupChat");
			}
			connectingToGroup = false;
		}else{
			socOut.println("Error: You aren't currently connected to any group in the first place");
		}
	}

	/**
	 * When the user types /quit, force shut down their connexion (stop the EchoClient)
	 */
	public void typeQuit(){
		EchoServerMultiThreaded.removeClientFromThreadList(this);
		return;
	}

	/**
	 * When the user types /group delete, delete the mentioned group (if the user's the group' owner)
	 */
	public void typeGroupDelete() {

		if(line.split(" ").length<3){
			socOut.println("Your command doesn't make sense. Check /help for more details");
			return;
		}
		String groupName = line.split(" ")[2];

		if (!LogManager.groupExist(groupName)) {
			socOut.println("Error: Group name doesn't exist");
			return;
		} else {
			if (!LogManager.isGroupOwner(groupName, username)) {
				socOut.println("Error: You don't have the permission to delete this group");
				return;

			} else {

				if (LogManager.deleteGroup(groupName)) {
					socOut.println("Group ["+groupName+ "] deleted");
				}else{
					socOut.println("Error: An error occurs while deleting a group");
				}

			}
		}
	}

	/**
	 * When the user types /help, show them the tutorial
	 */
	public void typeHelp(){

		socOut.println("/online                                              : show users who are currently online");
		socOut.println("/msg <receiverUsername> <message>                    : send a private message to a user");
		socOut.println("/r <message>                                         : send a private message to the last user who sent you a message");
		socOut.println("/history                                             : show your log's history or your group's history if you're connected to a group");
		socOut.println("/group                                               : show details of all groups you belong to");
		socOut.println("/group create <groupName>                            : create a new group");
		socOut.println("/group delete <groupName>                            : delete a group if you're the group owner");
		socOut.println("/group add <groupName> <username1> <username2> ...   : add members to a group if you're the group owner");
		socOut.println("/group remove <groupName> <username1> <username2>... : remove members to a group if you're the group owner");
		socOut.println("/group leave <groupName>                             : leave a group if you're not the group owner");
		socOut.println("/group connect <groupName>                           : enter a group chat");
		socOut.println("/group disconnect                                    : disconnect from the current group chat");
		socOut.println("/quit                                                : quit the chat application");

	}

	/**
	 * Find all users that are in the mentioned group and are currently online
	 * @param groupName
	 * @param username
	 * @return the online users who belong to the group
	 */
	public List<String> connectedUsersInGroup(String groupName,String username){

		List<String> users = new ArrayList<>();
		String[] members = LogManager.getGroupMembers(groupName, username).split(", ");
		members[0] = members[0].substring(0,members[0].length()-7);

		for (String name : members){
			ClientThread thread = EchoServerMultiThreaded.getUserByUsername(name);
			if(thread!=null && thread.connectingToGroup && thread.connectedGroup.equals(groupName)){
				users.add(thread.username);
			}
		}
		return users;
	}



}

  
