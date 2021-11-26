/**
 * ClientReceiver.java
 * @authors Annie Abhay, Sophanna NGOV
 */

package stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Receive and read every incoming messages
 */
public class ClientReceiver extends Thread{
    private Socket echoSocket; //socket
    private BufferedReader socIn; //reader flow
    private PrintStream socOut; //writer flow

    /**
     * ClientReceiver's Constructor
     * @param s the socket of the client
     * @param socIn their flow's reader (to read the incoming)
     * @param socOut their flow's writer (to write/show the message on the user's device)
     */
    public ClientReceiver(Socket s,BufferedReader socIn,PrintStream socOut){
        this.echoSocket = s;
        this.socIn = socIn;
        this.socOut = socOut;
    }

    /**
     * Starts the ClientReceiver
     */
    public void run() {
        try {
            while(true){
                String line = socIn.readLine();
                System.out.println(line);
            }
        }catch(Exception e){
                System.out.println("Disconnected");
        }
    }

}
