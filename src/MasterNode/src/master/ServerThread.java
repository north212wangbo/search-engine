package master;


import java.io.BufferedReader;
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
public class ServerThread {

    public static int PORT = 40000;    
    private static ServerSocket server = null;    
    public static String slave_ip[] = {"localhost","localhost","localhost"}; 
    public static int slave_port[] = {60000,60002,60004}; 
    public static String [] start_terms = {"0","expir","phrase"};
    public static String [] end_terms = {"expion","phrasal","zweiten"};
    
    public static int collection_size = 200000;
  
    public static double [] page_rank = new double[collection_size];
    public static double min_page_rank = Double.MAX_VALUE;
    public static double max_page_rank = 0;
    
    public static int [] doc_lengths = new int[collection_size];
    public static double avg_doc_length; //calculated in loadDocLengths
    public static String [] doc_titles = new String[collection_size];
    public static String [] doc_urls = new String[collection_size];
    public static String [] stop_list = new String[80];
    
    public static String collection_path;
    
    
    public static void main(String[] args) {
        String pagerank_file_name=null;
        String doclength_file_name=null;
        String doctitle_file_name = null; 
        String docurl_file_name = null; 
        String stoplist_file_name = null; 
        if (args.length==9) {
            for (int i=0;i<3;i++){//3 is the number of slaves
                String [] slave_address = args[i].split(",");
                slave_ip[i] = slave_address[0];
                slave_port[i] = Integer.valueOf(slave_address[1]);
            }
            pagerank_file_name = args[3];//file_name of page rank
            doclength_file_name = args[4];//file_name of documents' lengths
            doctitle_file_name = args[5];//file_name of documents' titles
            docurl_file_name = args[6];//file_name of documents' urls
            stoplist_file_name = args[7];//file_name of stoplist
            collection_path = args[8];
        }
       
        else{
            System.out.println("Error!Usage: java ServerThread ip,port ip,port ip,port pagerank_file doclength_file doctile_file docurl_file");
            System.exit(0);
        }
        
        //load pagerank
        loadPageRank(pagerank_file_name);
        
        //load docs' length
        loadDocLengths(doclength_file_name);
        
        //load docs' titles
        loadDocTitles(doctitle_file_name);
        
        //load docs' urls
        loadDocURLs(docurl_file_name);
        
        //load stop list
        loadStopList(stoplist_file_name);
        
        //start server thread waiting at PORT for incomming request from user
        try{
            server = new ServerSocket(PORT);           
        }
        catch(IOException e){
    	  System.out.println("Can not use port " + PORT + ": " + e.getMessage()); 
        }
        
        while (true){
            try {
                System.out.println("Waiting for incoming connection request..."); 
                createThread(server.accept());
            } catch (IOException ex) {
                System.out.println("Can not accept this request: " + ex);
            }
        }
    }
    
    private static void loadPageRank(String file_name){
        try {
            FileReader fr;
            fr = new FileReader(file_name);
            BufferedReader file_input = new BufferedReader(fr);
            String line;
            int count =0;
            while ((line = file_input.readLine()) != null) {
                    String rank = line.split("\t")[1];
                    page_rank[count] = Double.valueOf(rank);
                    
                    if (page_rank[count] < min_page_rank)
                        min_page_rank = page_rank[count];
                    if (page_rank[count] > max_page_rank)
                        max_page_rank = page_rank[count];
                    
                    count++;
            }
            fr.close();       
            System.out.println("finished load "+page_rank.length+" page ranks!");
        } 
        catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private static void loadDocLengths(String file_name){
         try {
            FileReader fr;
            fr = new FileReader(file_name);
            BufferedReader file_input = new BufferedReader(fr);
            String line;
            int count =0;
            int total = 0;
            while ((line = file_input.readLine()) != null) {
                doc_lengths[count] = Integer.valueOf(line);
                total += doc_lengths[count];
                count++;
            }
            fr.close();        
            avg_doc_length = (double)total/collection_size;
            
            System.out.println("finished load "+ doc_lengths.length+ " documents' lengths!");          
            //System.out.println("avg length: "+ (double)total/collection_size);
        } 
        catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
     private static void loadDocTitles(String file_name){
         try {
            FileReader fr;
            fr = new FileReader(file_name);
            BufferedReader file_input = new BufferedReader(fr);
            String line;
            int count =0;
            while ((line = file_input.readLine()) != null) {
                doc_titles[count] = line;
                count++;
            }
            fr.close();                    
            System.out.println("finished load " +doc_titles.length +" documents' titles!");          
        } 
        catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
     
     private static void loadDocURLs(String file_name){
         try {
            FileReader fr;
            fr = new FileReader(file_name);
            BufferedReader file_input = new BufferedReader(fr);
            String line;
            int count =0;
            while ((line = file_input.readLine()) != null) {
                doc_urls[count] = line.split("\t")[1];
                count++;
            }
            fr.close();                    
            System.out.println("finished load "+doc_urls.length +" documents' URLs!");          
        } 
        catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
     
     private static void loadStopList(String file_name){
         try {
            FileReader fr;
            fr = new FileReader(file_name);
            BufferedReader file_input = new BufferedReader(fr);
            String line;
            int count =0;
            while ((line = file_input.readLine()) != null) {
                stop_list[count] = line;
                count++;
            }
            fr.close();                    
            System.out.println("finished load "+stop_list.length +" terms in stop list!");          
        } 
        catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    //create QueryHandlerThread when there is a request from an user
    public static void createThread(Socket socket){
        QueryHandlerThread query_handler = new QueryHandlerThread(socket);
        query_handler.start();           
    }
    
}
