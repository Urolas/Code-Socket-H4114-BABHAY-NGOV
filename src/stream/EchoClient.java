/**
 * EchoClient.java
 * @authors Annie ABHAY, Sophanna NGOV
 */
package stream;

import java.io.*;
import java.net.*;


/**
 * Represents a client's device, connected to the chat-room server
 */
public class EchoClient {


  /**
  *  main method
  *  accepts a connection, receives a message from client then sends an echo to the client
   * @param args includes the client's ip address and the server's port to connect to
  **/
    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        PrintStream socOut = null; //writing flow
        BufferedReader stdIn = null;
        BufferedReader socIn = null; //reading flow
        ClientReceiver cR = null; //read received messages

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
          System.exit(1);
        }

        try {

      	    // creation socket ==> connexion
      	    echoSocket = new Socket(args[0],new Integer(args[1]).intValue());
	        socIn = new BufferedReader( new InputStreamReader(echoSocket.getInputStream()));
	        socOut= new PrintStream(echoSocket.getOutputStream());
	        stdIn = new BufferedReader(new InputStreamReader(System.in));

            cR = new ClientReceiver(echoSocket,socIn,socOut);
            cR.start();

            //Ask for the client's username (no password for now, but it is easy to implement)
            System.out.print("Insert username :");
            String user;
            socOut.println(user = stdIn.readLine()); //send the username to the out flow
            System.out.println("Successfully connected. Welcome to chat "+user+" !");
            LogManager.findNameInFile(user);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to:"+ args[0]);
            System.exit(1);
        }

        // A loop that allows the device to write commands or to receive and read messages
        String line;
        while (true) {
        	line=stdIn.readLine();
        	if (line.equals(".")) break;
        	socOut.println(line);
            if(line.trim().equals("/quit")){
                break;
            }
        }
      socOut.close();
      socIn.close();
      stdIn.close();
      echoSocket.close();
    }


}


