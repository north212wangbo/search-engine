/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datastructure;

/**
 *
 * @author huytran
 */
public class OutputDoc {
    public int docid;
    public String title = null;
    public String url = null;
    public double relevant_score;
    public double page_rank;
    //todo: cached version
    
    public OutputDoc(int docid, String title, String url, double relevant_score, double page_rank){
        this.docid = docid;
        this.title = title;
        this.url = url;
        this.relevant_score = relevant_score;
        this.page_rank = page_rank;
    }
}
