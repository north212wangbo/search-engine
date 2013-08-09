/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package QueryProcessing;

import datastructure.DocTermList;
import datastructure.QueryTerm;
import datastructure.TermDocList;
import java.util.ArrayList;
import java.util.LinkedList;
import master.ServerThread;

public class BM25Model {
    private double K1 = 1.2;
    private double K2 = 200;
    private double b = 0.75; 
    public double c = 0.5;//c is impact of relevant rank , (1-c) impact of page rank
    public double min_relevant_rank = Double.MAX_VALUE;
    public double max_relevant_rank = 0;
    
    public BM25Model(String parameters){
        String []s = parameters.split(" ");
        c  = Double.valueOf(s[0]);
        K1 = Double.valueOf(s[1]);
        K2 = Double.valueOf(s[2]);
        b  = Double.valueOf(s[3]);       
    }
    
    // function to apply page ranking model: model BM25
    // input: ArrayList of doc_term_list, 
    // output: ranked ArrayList of doc_term_list
    public void apply_BM25_to_list_of_doc_term_lists(ArrayList<DocTermList> list_of_doc_term_lists, LinkedList<TermDocList> term_doc_list, LinkedList<QueryTerm> query_terms_list){                      
        // TODO: need to preproces these:
        int D = ServerThread.collection_size; // Huy : function to get number of Doc in collection, pre-stored in object
        
        // loop through all found documents
        for(int j=0; j<list_of_doc_term_lists.size(); j++){
            DocTermList Dj = list_of_doc_term_lists.get(j);
            
            // loop through number of different terms in query Q
            double relevantRank=0;
            for (int i=0; i < query_terms_list.size(); i++) {
                String curTerm = query_terms_list.get(i).strTerm;
                
                //*** FIRST factor
                // get D_q_i
                int D_q_i=0;
                for(int x=0;x<term_doc_list.size();x++){
                    if(curTerm.compareTo(term_doc_list.get(x).term)==0){
                        D_q_i = term_doc_list.get(x).docList.size();
                    }
                }                
                double FIRST = Math.log((D-D_q_i+0.5)/(D_q_i+0.5));
                
                //***SECOND factor
                // get f_qi_Dj
                double f_qi_Dj=0;
                for(int x =0; x<Dj.termList.size();x++){
                    if(curTerm.compareTo(Dj.termList.get(x).term)==0){
                        f_qi_Dj = Dj.termList.get(x).freq;
                    }
                }            
                // get lamba
                int Dj_length = ServerThread.doc_lengths[Integer.valueOf(Dj.docID)];
                double avdl = ServerThread.avg_doc_length;
                double lamda = K1*((1-b)+b*Dj_length/avdl); // Huy : done INFO length of doc Dj and avdl
                double SECOND = ((K1+1)*f_qi_Dj)/(lamda + f_qi_Dj);
                
                //***THIRD factor
                // get f_qi_Q
                int f_qi_Q = 0;
                for(int x = 0;x<query_terms_list.size();x++){
                    if(curTerm.compareTo(query_terms_list.get(x).strTerm)==0){
                        f_qi_Q = query_terms_list.get(x).timeOccur;
                        break;
                    }
                }
                double THIRD = ((K2+1)*f_qi_Q)/(K2+f_qi_Q);  
                
                relevantRank += FIRST*SECOND*THIRD;
            }
            
            list_of_doc_term_lists.get(j).relevant_rank = relevantRank;
            
            //find max and min of relevant score for normalization
            if (relevantRank < min_relevant_rank){
                min_relevant_rank = relevantRank;
            }
            if (relevantRank > max_relevant_rank){
                max_relevant_rank = relevantRank;
            }
        }
    }
}
