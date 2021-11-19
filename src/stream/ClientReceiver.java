package stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ClientReceiver extends Thread{
    private Socket echoSocket; //socket
    private BufferedReader socIn; //reader flow
    private PrintStream socOut; //writer flow

    public ClientReceiver(Socket s,BufferedReader socIn,PrintStream socOut){
        this.echoSocket = s;
        this.socIn = socIn;
        this.socOut = socOut;
    }
    public void run() {
        try {
            while(true){
                System.out.println(socIn.readLine());
            }
        }catch(Exception e){
            System.err.println("Error in Client Receiver:" + e);
        }
    }
}
