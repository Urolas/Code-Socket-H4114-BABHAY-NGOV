package stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientReceiver extends Thread{
    private Socket clientSocket; //socket
    private BufferedReader socIn; //reader flow
    private PrintStream socOut; //writer flow

    public ClientReceiver(Socket s,BufferedReader socIn){

    }
}
