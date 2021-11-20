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
                String line = socIn.readLine();
                System.out.println(line);
            }
        }catch(Exception e){
                System.out.println("Disconnected");
        }
    }

}
