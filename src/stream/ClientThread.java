/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
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
				  if(connectingToGroup && !line.equals("/group disconnect")){
					  typeMsgInGroup();
				  }

				  //send a private message to someone
				  if(line.startsWith("/msg ") || line.startsWith("/r ")){
					  typeMsg();
				  }

				  //show log of the user
				  if(line.equals("/history")){
					  typeHistory();
				  }

				  if(line.startsWith("/group")){

					  //check all group the user is in
					  if(line.trim().equals("/group")){
						  typeGroup();
					  }

					  //check the members of a group
					  else if(line.startsWith("/group members ")){
						  typeGroupMembers();
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
					  else if(line.startsWith("/group enter")){
						   typeGroupConnect();

					  }

					  //disconnect from current group : /group disconnect
					  else if(line.startsWith("/group disconnect") && connectingToGroup){
						  typeGroupDisconnect();
					  }

				  }
				  

				  //leave the chat
				  if(line.equals("/quit")){
					  typeQuit();
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

	public String reformatMsgGroup(String line, String username){
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		return ("["+connectedGroup+"]"+ANSI_YELLOW+" ["+hour+":"+minute+"] "+ username + ANSI_RESET + " : "+line);
	}

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

	public void typeOnline(){

		socOut.println("Users online: ");
		List<ClientThread> listClients =  EchoServerMultiThreaded.getClientThreadList();
		for (ClientThread c : listClients){
			String username = c.getUsername();
			socOut.println(username + " " + clientSocket.getInetAddress());
		}
	}

	public void typeMsgInGroup(){

		//Send msg to yourself and log
		socOut.println(reformatMsgGroup(line,"You"));
		LogManager.writeOnGroupLog(connectedGroup, reformatMsgGroup(line,username));

		//send message to anybody connected
		for (ClientThread member : groupMembers){
			member.socOut.println(reformatMsgGroup(line,username));
		}

	}

	public void typeMsg(){

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
			LogManager.writeOnUserLog(receiver.getUsername(), reformatMsgLog(message,username));
			receiver.setLastMessageUsername(username);
			socOut.println(reformatMsg(message,"You to "+receiver.getUsername()));
			LogManager.writeOnUserLog(username,reformatMsgLog(message,"You to "+receiver.getUsername()));

			//If the username isn't online
		}else{
			socOut.println("This username doesn't exist or isn't online");
		}
	}

	public void typeHistory(){
		String history = LogManager.getHistory(username);
		socOut.println(history);
	}

	public void typeGroup(){
		String allGroup = LogManager.getUserGroups(username);
		socOut.println(allGroup);
	}

	public void typeGroupMembers(){
		String groupName = line.split(" ")[2];
		String msg=LogManager.getGroupMembers(groupName,username);
		if(msg == null){
			socOut.println("The group name does not exist or you do not belong to this group");
		}else{
			socOut.println(msg);
		}
	}

	public void typeGroupCreate(){
		String groupName = line.split(" ")[2];
		if(LogManager.findGroupInFile(groupName,username)){
			socOut.println("Group ["+groupName+"] created");
		}else{
			socOut.println("Group name already exist");
		}
	}

	public void typeGroupAddMember(){
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
	}

	public void typeGroupRemoveMember(){
		String groupName = line.split(" ")[2];
		boolean succeed=true;

		if (!LogManager.groupExist(groupName)) {
			socOut.println("Error: Group name doesn't exist");
		} else {
			if (!LogManager.isGroupOwner(groupName, username)) {
				socOut.println("Error: You don't have the permission to remove people from this group");

			} else {
				String[] removedPeople = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");
				System.out.println("wanna remove"+removedPeople[0]+" from"+groupName);

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

	public void typeGroupLeave(){
		String groupName = line.split(" ")[2];
		if (!LogManager.groupExist(groupName)) {
			socOut.println("Group name doesn't exist");
		} else {

			if(LogManager.leaveGroup(groupName,username)){
				socOut.println("You just left the group ["+groupName+"]");
			}else{
				socOut.println("Error: You can't leave a group if you're the owner or if you don't belong to the group");
			}
		}
	}

	public void typeGroupConnect(){
		if(connectingToGroup){
			socOut.println("Disconnected from group ["+connectedGroup+"]");

			//send message to anybody connected
			for (ClientThread member : groupMembers){
				member.socOut.println(username+" just disconnected from the groupChat");
			}
		}

		String groupName = line.split(" ")[2];
		boolean succeed = true;

		if (!LogManager.groupExist(groupName)) {
			socOut.println("Group name doesn't exist");
		}else{
			connectedGroup = groupName;
			String[] members = LogManager.getGroupMembers(groupName, username).split(", ");
			members[0] = members[0].substring(0,members[0].length()-7);
			if(!Arrays.asList(members).contains(username)){
				socOut.println("You don't belong to this group");
				succeed = false;
			}

			if(succeed){
				for (String name : members){
					ClientThread thread = EchoServerMultiThreaded.getUserByUsername(name);
					if(thread!=null && thread!=this && thread.connectingToGroup && thread.connectedGroup.equals(groupName)){
						groupMembers.add(thread);
					}
				}
			}
		}
		socOut.println("Connected to group ["+groupName+"]");
		for (ClientThread member : groupMembers){
			member.socOut.println(username+" has joined from the groupChat");
		}

		connectingToGroup = true;
	}

	public void typeGroupDisconnect(){
		if(connectingToGroup){
			socOut.println("Disconnected from group ["+connectedGroup+"]");

			//send message to anybody connected
			for (ClientThread member : groupMembers){
				member.socOut.println(username+" just disconnected from the groupChat");
			}
		}

		String groupName = line.split(" ")[2];
		boolean succeed = true;

		if (!LogManager.groupExist(groupName)) {
			socOut.println("Group name doesn't exist");
		}else{
			connectedGroup = groupName;
			String[] members = LogManager.getGroupMembers(groupName, username).split(", ");
			members[0] = members[0].substring(0,members[0].length()-7);
			if(!Arrays.asList(members).contains(username)){
				socOut.println("You don't belong to this group");
				succeed = false;
			}

			if(succeed){
				for (String name : members){
					ClientThread thread = EchoServerMultiThreaded.getUserByUsername(name);
					if(thread!=null && thread!=this){
						groupMembers.add(thread);
					}
				}
			}
		}
		socOut.println("Connected to group ["+groupName+"]");
		for (ClientThread member : groupMembers){
			member.socOut.println(username+" has joined from the groupChat");
		}

		connectingToGroup = true;
	}

	public void typeQuit(){
		EchoServerMultiThreaded.removeClientFromThreadList(this);
		return;
	}

	public void typeGroupDelete(){

		String groupName = line.split(" ")[2];
		boolean succeed=true;

		if (!LogManager.groupExist(groupName)) {
			socOut.println("Error: Group name doesn't exist");
		} else {
			if (!LogManager.isGroupOwner(groupName, username)) {
				socOut.println("Error: You don't have the permission to delete this group");

			} else {


				if (succeed && LogManager.deleteGroup(groupName)) {
					socOut.println("Group ["+groupName+ "] deleted");
				}else{
					socOut.println("Error: An error occurs while deleting a group");
				}

			}
		}


	}

}

  
