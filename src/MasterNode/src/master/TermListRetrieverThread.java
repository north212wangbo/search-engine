package master;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author huytran
 */
public class TermListRetrieverThread extends Thread {

    private Socket socket = null;
    private DataInputStream streamIn = null;//input stream 
    private DataOutputStream streamOut = null;//output stream 
    private String query;
    private LinkedList<String> list;
    private int max_length_recieved_string = 65535/3;

    public TermListRetrieverThread(String serverName, int serverPort, String query,LinkedList<String> list) {
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected!");

            this.query = query;
            this.list = list;
            
            streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        } catch (UnknownHostException ex) {
            Logger.getLogger(TermListRetrieverThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TermListRetrieverThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String input = "";
        try {
            streamOut.writeUTF(query);//read input from client, send to slave
            streamOut.flush();
            //read input sent from a slave
            while(true){
                String s = streamIn.readUTF();
                input += s;
                if (s.length()< max_length_recieved_string) {                    
                    break;
                }                  
            }
            //System.out.println("list: "+input); 
            if (!input.equals("")){//if the input received from the slave is not empty then add it into the list of strings
                synchronized(list){
                    list.add(input);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TermListRetrieverThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(list);
        terminateThread();       
    }
    
    public void terminateThread() {
        try {
            if (streamIn != null) {
                streamIn.close();
            }
            if (streamOut != null) {
                streamOut.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}