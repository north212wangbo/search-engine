/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datastructure;

import java.util.ArrayList;

/**
 *
 * @author bowang
 */
public class DocTermList implements Comparable<DocTermList> {
    public String docID;
    //public double page_rank;
    public double relevant_rank;    // Hoang: just ADDED, to store relavant rank 
    public double final_score;//combined of (page_rank, and relevant rank) or score from language model
    public double num_terms;        // Hoang: just ADDED, number of all terms (count duplicated) in doc
    public ArrayList<TermInfo> termList;

    @Override
    public int compareTo(DocTermList doc_term_list) {//for descending order
        if (this.final_score - doc_term_list.final_score >0)
            return -1;
        else if (this.final_score - doc_term_list.final_score < 0)
            return 1;
        else
            return 0;
    }
    
    public int getDocID(){
        return Integer.valueOf(docID);
    }
    
    
}
