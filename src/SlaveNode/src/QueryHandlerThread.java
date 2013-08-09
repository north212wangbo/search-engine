
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
public class QueryHandlerThread extends Thread {

    private Socket socket = null;
    private DataInputStream streamIn = null; //input stream
    private DataOutputStream streamOut = null; //output stream
    private InvertedListProcessor inverted_list_processor = null;
    
    private int max_length_output_string = 65535/3;
    
    public QueryHandlerThread(Socket socket, InvertedListProcessor inverted_list_processor) {
        System.out.println("connected with " + socket.getInetAddress());
        this.socket = socket;
        this.inverted_list_processor = inverted_list_processor;
        
        try {
            streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(QueryHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String query = "";
        try {
            query = streamIn.readUTF();
        } catch (IOException ex) {
            Logger.getLogger(QueryHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        processQuery(query);
    }

    public void processQuery(String query) {
        System.out.println("received query: " + query);
        
        String result = inverted_list_processor.searchTerm(query);
        //System.out.println(rst);
        send(result);        
        //send("slave "+ socket.getInetAddress()+":"+socket.getPort()+" output "+query);
        terminate();
    }

    //send response result to master
    public void send(String result) {
        String sub_string;
        int start_index = 0;
        int result_length = result.length();
        int end_index;
        try {
            if ("".equals(result)){//if the term is not found in any doctument, then return empty string
                streamOut.writeUTF("");
                streamOut.flush();
            }
            else {               
                while (start_index < result_length){
                    //writeUTF just allow max_length_output_string to send each time, so we send multiple substrings having max_length_output_string
                    end_index = Math.min(result_length,start_index+max_length_output_string);
                    sub_string = result.substring(start_index, end_index);
                    streamOut.writeUTF(sub_string);
                    streamOut.flush();
                    start_index = end_index;
                    //System.out.println(sub_string);
                }
            }
        } catch (IOException e) {
            System.out.println("Can not use output stream : " + e.getMessage());
        }
    }

    public void terminate() {
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
        } catch (IOException ex) {
            Logger.getLogger(QueryHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
