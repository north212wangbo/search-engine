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


public class TFIDFModel {
    
    public double min_relevant_rank = Double.MAX_VALUE;
    public double max_relevant_rank = 0;
    public double c = 0.5;//c is impact of relevant rank , (1-c) impact of page rank
    
    public TFIDFModel(String parameters){
        c  = Double.valueOf(parameters);
    }
    private double tfidf(DocTermList Dj, TermDocList term_doc_list){
        //DocTermList need store: total number of term in doc
        // get ti_in_Dj
        int ti_in_Dj = 0;
        for(int x=0; x<Dj.termList.size();x++){
            if(term_doc_list.term.compareTo(Dj.termList.get(x).term)==0){
                ti_in_Dj = Dj.termList.get(x).freq;
                break;
            }
        }        

        int Dj_length = ServerThread.doc_lengths[Integer.valueOf(Dj.docID)];
        double tf = (double)ti_in_Dj/Dj_length;//length of Dj = total number of terms in Dj
//        System.out.println("ti_in_Dj"+ti_in_Dj);
//        System.out.println("num_terms"+Dj_length);
        
        //Huy: get number document contains qterms        
        int numDocContain_qTerm=term_doc_list.totalfreq;
//        System.out.println("totalfreq"+term_doc_list.totalfreq);

        int D= ServerThread.collection_size;//Huy        
        double idf = Math.log((double)D/(double)numDocContain_qTerm);
        double relavantRank = tf*idf;
        
        return relavantRank;
    }
    
    public void apply_TFIDF_to_list_of_doc_term_lists(ArrayList<DocTermList> list_of_doc_term_lists, LinkedList<TermDocList> term_doc_lists){       
        for(int j=0; j<list_of_doc_term_lists.size(); j++){
            DocTermList Dj = list_of_doc_term_lists.get(j);
            double relevantRank = 0;
            for (int i = 0; i < term_doc_lists.size(); i++) {
                relevantRank += tfidf(Dj, term_doc_lists.get(i));
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
