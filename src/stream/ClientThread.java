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
import java.util.List;

public class ClientThread
	extends Thread {


	private Socket clientSocket;
	private List<ClientThread> clientThreadList = new ArrayList<>();
	private PrintStream socOut;
	
	ClientThread(Socket s,List<ClientThread> listClient) {

		this.clientSocket = s;
		this.clientThreadList = listClient;

	}


 	/**
  	* receives a request from client then sends an echo to the client
  	**/
	public void run() {
    	  try {
    		BufferedReader socIn = null;
    		socIn = new BufferedReader(
    			new InputStreamReader(clientSocket.getInputStream()));    
    		this.socOut = new PrintStream(clientSocket.getOutputStream());
    		while (true) {
    		  String line = socIn.readLine();
			  String lineWithIp = clientSocket.getInetAddress()+": "+line;
			  for( ClientThread otherClient: clientThreadList){
				  if(otherClient.getClientSocket()!=this.getClientSocket()) {
					  otherClient.getSocOut().println(lineWithIp);
				  }
			  }

    		  socOut.println(line);
			  System.out.println(lineWithIp);
    		}
    	} catch (Exception e) {
        	System.err.println("Error in EchoServer:" + e); 
        }
       }

	public void addClientToList(ClientThread newClient) {
		this.clientThreadList.add(newClient);
	}


	public Socket getClientSocket() {
		return clientSocket;
	}

	public PrintStream getSocOut() {
		return socOut;
	}

  
  }

  
