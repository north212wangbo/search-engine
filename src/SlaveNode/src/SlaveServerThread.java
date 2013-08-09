
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
public class SlaveServerThread extends Thread{

    private static ServerSocket server = null;
    private static int port = 50000;
    private static String file_name = null;
    private static InvertedListProcessor inverted_list_processor = new InvertedListProcessor();

    public static void main(String[] args) {
        
        if (args.length>0 && args[0] != null && args[1] != null) {
            port = Integer.valueOf(args[0]);
            file_name = args[1];
        }
       
        try{
            server = new ServerSocket(port);
            
        }
        catch(IOException e){
    	  System.out.println("Can not use port " + port + ": " + e.getMessage()); 
        }
        
        System.out.println("slave runs at host: "+ server.getInetAddress() + " port: "+ port);

               
        //load inverted list
        inverted_list_processor.createTermArray(file_name);
        
        
        while (true){
            try {
                System.out.println("Waiting for incoming connection request..."); 
                createThread(server.accept());
            } catch (IOException ex) {
                System.out.println("Can not accept this request: " + ex);
            }
        }
    }
    
    public static void createThread(Socket socket){
        QueryHandlerThread query_handler = new QueryHandlerThread(socket, inverted_list_processor);
        query_handler.run();
            
    }
}
