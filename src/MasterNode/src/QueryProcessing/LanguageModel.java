/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package QueryProcessing;

import datastructure.DocTermList;
import datastructure.QueryTerm;
import datastructure.TermDocList;
import datastructure.TermInfo;
import java.util.ArrayList;
import java.util.LinkedList;
import master.ServerThread;

/**
 *
 * @author bowang
 */
public class LanguageModel {
    private double total_num_terms_collection = 253498000;
    private double Lambda;;
    
    
    public LanguageModel(String parameters){
        Lambda = Double.parseDouble(parameters);
    }
    
    private double queryLikelyhood_for_Dj(DocTermList Dj){
        double term_score = 0;
        double final_score = 0;      
        int Dj_length = ServerThread.doc_lengths[Integer.valueOf(Dj.docID)];
        double Dj_pagerank = ServerThread.page_rank[Dj.getDocID()];
        
        for(int i=0; i<Dj.termList.size(); i++){
            term_score += Math.log((1-Lambda)*Dj.termList.get(i).freq/Dj_length + Lambda*Dj.termList.get(i).totalfreq/total_num_terms_collection);
        }
        final_score = term_score+Math.log(Dj_pagerank/ServerThread.max_page_rank); //need normalize
//        System.out.println("page rank"+Dj_pagerank);
//        System.out.println("max page rank"+ServerThread.max_page_rank);
        return final_score;
    }
    
    public void apply_queryLikelyhood_to_list_of_doc_term_lists(ArrayList<DocTermList> list_of_doc_term_lists){
        for(int j=0; j<list_of_doc_term_lists.size(); j++){
            DocTermList Dj = list_of_doc_term_lists.get(j);
            double final_score = queryLikelyhood_for_Dj(Dj);
            list_of_doc_term_lists.get(j).final_score = final_score;
            
        }
                    
    }
}
