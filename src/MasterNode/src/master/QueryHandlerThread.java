package master;


import QueryProcessing.*;
import com.google.gson.Gson;
import datastructure.DocInfo;
import datastructure.DocTermList;
import datastructure.Output;
import datastructure.OutputDoc;
import datastructure.QueryTerm;
import datastructure.TermDocList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author huytran
 */
public class QueryHandlerThread extends Thread{
    private Socket socket = null;
    private BufferedReader streamIn = null; //input stream
    private PrintWriter streamOut = null; //output stream
    private LinkedList<String> list  = null;//list of input received from slaves
    private LinkedList<TermListRetrieverThread> slave_client_threads  = null;
    private int num_slave_client_threads = 3;
    
    private String [] original_query = null;//array of term
    private LinkedList<String> stemmed_query = null;//list of stemmed term
    private LinkedList<String> slave_queries = null;//list of queries for slaves
    
    private LinkedList<TermDocList> term_doc_list =null;
    
    private LinkedList<QueryTerm> query_terms_list = null; // Hoang Added, list of single query term and its frequency
    
    private String query_type = "";//AND or OR 
    private String model = "";//TFIDF ro BM25
    private String parameters = "";//depend on the model
    private int start_index = 0;//will be updated from user message
    private int end_index = 10;//will be updated from user message
    
    private long start_time = System.currentTimeMillis();//the time of starting hanling query
    
    public QueryHandlerThread(Socket socket) {
        System.out.println("connected with " + socket.getInetAddress());
        this.socket = socket;
        this.list = new LinkedList<String>();
        
        this.stemmed_query = new LinkedList<String>();
        
        this.query_terms_list = new LinkedList<QueryTerm>();
        
        this.slave_client_threads = new LinkedList<TermListRetrieverThread>();
        slave_queries = new LinkedList<String>();
        
        for (int i =0;i<num_slave_client_threads;i++){
            slave_queries.add("");
        }
        
        try {
            streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            streamOut = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException ex) {
            Logger.getLogger(QueryHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        String message = "";
        try {
            message = streamIn.readLine();  
        } catch (IOException ex) {
            Logger.getLogger(QueryHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        parseMessage(message); 
        //terminate
        terminate();
    }
    
    public void parseMessage(String message){
        System.out.println("message: "+message);
        String[] s = message.split(":");
        String message_type = s[0];
        if (message_type.compareTo("query") == 0){
            query_type = s[1];
            model = s[2];
            parameters = s[3];
            start_index = Integer.valueOf(s[4]);
            end_index = Integer.valueOf(s[5]);
            processQuery(s[6]);
        }
        else if (message_type.compareTo("cache") == 0){//get cached
            int docid = Integer.valueOf(s[1]);
            getCache(docid);
        }            
    }
    
    //get content of doc in the collection, and send back to the client
    public void getCache(int docid){
        try {
            FileReader fr;
            String file_name = ServerThread.collection_path+docid+".html";
            fr = new FileReader(file_name);
            BufferedReader file_input = new BufferedReader(fr);
            String line;
            String file_content="";
            while ((line = file_input.readLine()) != null) {
                file_content += line;
            }
            fr.close();                  
            send(file_content);
            //System.out.println(file_content);          
        } 
        catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Could not access file: "+docid);
        } 
    }
    
    //parse,stem queries
    public void preprocessQuery(String query){
        //parse query
        original_query = query.split("[ | \\s . ; : ` , ? ' \" - > < { } ( ) \\[ \\] ( ) \\/ \\\\ ]+");
        //stem terms and remove stop list in query
        
        for (String term:original_query){
            String stemmed_term = stem(term.toLowerCase());
            int i;
            for (i=0; i<ServerThread.stop_list.length;i++){
                if (stemmed_term.compareTo(ServerThread.stop_list[i]) ==0)
                    break;
            }
            if (i == ServerThread.stop_list.length)
                stemmed_query.add(stemmed_term);
        }
        
        //create a list of query term, each element : term + number of occurence in query
        makeQueryTermList();
        //System.out.println("stemmed_query "+ stemmed_query.size());
        //remove duplicate terms from stemmed_query
        LinkedList<String> non_duplicate_stemmed_query = new LinkedList<String>();
        for (int i=0;i<stemmed_query.size();i++){
            String stemmed_term = stemmed_query.get(i);
            
            if (!stemmed_term.equals("")){
               
                for (int j=i+1;j<stemmed_query.size();j++){
                    if (stemmed_term.equals(stemmed_query.get(j))){
                        stemmed_query.set(j, "");
                    }
                }
                non_duplicate_stemmed_query.add(stemmed_term);
            }          
        }
        stemmed_query = non_duplicate_stemmed_query;
        System.out.println("stemmed query"+stemmed_query);
    }
    
    //split query into queries for slaves
    public void splitQuery(){
        for (int i=0;i<stemmed_query.size();i++){
            String stemmed_term = stemmed_query.get(i);
            for (int j=0;j<num_slave_client_threads;j++){
                if (stemmed_term.compareTo(ServerThread.start_terms[j]) >= 0 && stemmed_term.compareTo(ServerThread.end_terms[j]) <= 0){
                    slave_queries.set(j, slave_queries.get(j)+" "+stemmed_term);
                    break;
                }
            }
        }
    }
    
    public void processQuery(String query) {
        System.out.println("received query: " + query);
        //parse and stem terms and remove stop list in query
        preprocessQuery(query);
        //split stemmed queries into sub_queries for slaves
        splitQuery();
        
        //send query to slave nodes
        for (int i=0;i<num_slave_client_threads;i++){
            if (slave_queries.get(i).equals("") == false){//only send non-empty queries to slaves
                TermListRetrieverThread slave_client_thread = new TermListRetrieverThread(ServerThread.slave_ip[i],ServerThread.slave_port[i],slave_queries.get(i),list);
                slave_client_threads.add(slave_client_thread);
                slave_client_thread.start();
            }
        }
        
        //wait for receiving lists from slave nodes
        for (int i=0;i<slave_client_threads.size();i++){
            try {
                slave_client_threads.get(i).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(QueryHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //generate TermDocList from list of inputs received from slaves
        term_doc_list = generateTermDocList(list);

        //processquery
        AndOrQueryProcessor and_or_query_processor = new AndOrQueryProcessor();
        ArrayList<DocTermList> doc_term_lists = new ArrayList<DocTermList>();
        //System.out.println("query type: "+query_type);
        if (query_type.equals("AND")){
            if (stemmed_query.size() == term_doc_list.size()) {//only perform and query if number of terms received from slaves is equal to the number of term in stemmed query
                //System.out.println("process AND query");
                doc_term_lists = and_or_query_processor.processAndQuery(term_doc_list);
            }
        }
        else{
            doc_term_lists = and_or_query_processor.processOrQuery(term_doc_list);
        }
  
        //send result back to the php thread 
        if (doc_term_lists.size()>0){
            //process model
            if (model.equals("BM25")){
                BM25Model bm25_model = new BM25Model(parameters);
                bm25_model.apply_BM25_to_list_of_doc_term_lists(doc_term_lists, term_doc_list, query_terms_list);
                //TODO: combine page ranking with and result from a model
                double norm_log_page_rank;
                double norm_relevant_rank;
                //final score = C*norm(relevant_rank) + (1-C)*norm(log(page_rank))   
                for (int i=0;i<doc_term_lists.size();i++){
                    norm_relevant_rank = normalize(doc_term_lists.get(i).relevant_rank, bm25_model.min_relevant_rank, bm25_model.max_relevant_rank);
                    norm_log_page_rank = normalize(Math.log(ServerThread.page_rank[doc_term_lists.get(i).getDocID()]), Math.log(ServerThread.min_page_rank), Math.log(ServerThread.max_page_rank));
                    doc_term_lists.get(i).final_score = bm25_model.c*norm_relevant_rank + (1-bm25_model.c)*norm_log_page_rank;
                }
                
            }
            else if (model.equals("TFIDF")){
                TFIDFModel tfidf_model = new TFIDFModel(parameters);
                System.out.println("C : "+ tfidf_model.c);
                tfidf_model.apply_TFIDF_to_list_of_doc_term_lists(doc_term_lists, term_doc_list);
                //TODO: combine page ranking with and result from a model
                double norm_log_page_rank;
                double norm_relevant_rank;
                //final score = C*norm(relevant_rank) + (1-C)*norm(log(page_rank))   
                for (int i=0;i<doc_term_lists.size();i++){
                    norm_relevant_rank = normalize(doc_term_lists.get(i).relevant_rank, tfidf_model.min_relevant_rank, tfidf_model.max_relevant_rank);
                    norm_log_page_rank = normalize(Math.log(ServerThread.page_rank[doc_term_lists.get(i).getDocID()]), Math.log(ServerThread.min_page_rank), Math.log(ServerThread.max_page_rank));
                    doc_term_lists.get(i).final_score = tfidf_model.c*norm_relevant_rank + (1-tfidf_model.c)*norm_log_page_rank;
                }
            }
            else if (model.equals("LANGUAGE")){//Language model
                LanguageModel language_model = new LanguageModel(parameters);
                language_model.apply_queryLikelyhood_to_list_of_doc_term_lists(doc_term_lists);
            }
            
            //TODO: sort final result
            Collections.sort(doc_term_lists);
        }
        //prepare and send final output
        String final_result = prepareOutput(doc_term_lists);
        send(final_result);    
        System.out.println("You should received results by now!");
        
    }
    
    //normalize relevant score
    private double normalize(double x, double x_min, double x_max){
        if (x_max == x_min)
            return 1;//if range is 0, all have equal values 
        return (x-x_min)/(x_max-x_min);
    }
    
    
    //prepare output to send to an user
    public String prepareOutput(ArrayList<DocTermList> doc_term_lists){
        double handling_time = (double)(System.currentTimeMillis() - start_time)/1000;        
        Output output = new Output(handling_time, doc_term_lists.size());
        
        for(int i=start_index; i<Math.min(end_index,doc_term_lists.size());i++){
            int doc_id = Integer.valueOf(doc_term_lists.get(i).docID);
            String title = ServerThread.doc_titles[doc_id];
            String url = ServerThread.doc_urls[doc_id];
            double page_rank = ServerThread.page_rank[doc_id];
            double relevant_score;
            if (model.equals("LANGUAGE")){
                relevant_score = doc_term_lists.get(i).final_score;
            }
            else {
                relevant_score = doc_term_lists.get(i).relevant_rank;
            }
            
                        
            OutputDoc output_doc = new OutputDoc(doc_id, title, url, relevant_score, page_rank);
            output.output_doc_list.add(output_doc);
        }   
        Gson gson = new Gson();
        String final_result = gson.toJson(output);
        //System.out.println(final_result);
        return final_result;
    }
    
    // create a list of query term, each element : term + number of occurence in query
    public void makeQueryTermList() {
        for (int i = 0; i < this.stemmed_query.size(); i++) {
            boolean added = false;
            String curTerm = this.stemmed_query.get(i);
            for (int j = 0; j < this.query_terms_list.size(); j++) {
                if (this.query_terms_list.get(j).strTerm.compareTo(curTerm) == 0) {
                    this.query_terms_list.get(j).timeOccur++;
                    added = true;
                    break;
                }
            }
            if (added == false) {
                this.query_terms_list.add(new QueryTerm(curTerm));
            }
        }
    }
    
    //convert list of strings received from slaves into TermDocList 
    private LinkedList<TermDocList> generateTermDocList(LinkedList<String> list){  
        
        LinkedList<TermDocList> merge_list = new LinkedList<TermDocList>();
        //extract a string from a slave node
        for (int i = 0; i < list.size(); i++) {
            String[] termList = list.get(i).split("\n");
//            System.out.println("termList: "+list.get(i)); 
     
            //extract a string lead by a term
            for (int j = 0; j < termList.length; j++) {
                //process the string, store in the termInfo, insert into termInfo list
                TermDocList jtermDocList = new TermDocList();
                String[] temp = termList[j].split(" ");
//                System.out.println("temp length: "+temp.length); 
                jtermDocList.term = temp[0];

                jtermDocList.totalfreq = Integer.parseInt(temp[1]);
                jtermDocList.docList = new ArrayList<DocInfo>();
                //convert temp[2] to an arraylist
                String regular = "(\\w{6},\\w+)";
                Pattern p = Pattern.compile(regular);
                Matcher m = p.matcher(temp[2]);
                while (m.find()) {
                    String[] docTemp = m.group(1).split(",");
                    DocInfo newdocInfo = new DocInfo(docTemp[0], Integer.parseInt(docTemp[1]));
                    jtermDocList.docList.add(newdocInfo);
                }
                merge_list.add(jtermDocList);
            }
        }
        return merge_list;
    }     
    
    // stemming, use porter stemming algorithm
    public static String stem(String word){
        Stemmer s = new Stemmer();
        s.add(word.toCharArray(), word.length());
        s.stem();
        return s.toString();
    }
    
    //send response result to master
    public void send(String result) {
        streamOut.println(result);
        //streamOut.flush();

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
