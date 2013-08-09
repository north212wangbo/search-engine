/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bowang
 */

public class Node{
    String term;
    String val; //doc list stored in a String (docID, freq)
    int totalfreq;
    
    public Node(String term, int totalfreq, String val){
        this.term = term;
        this.val = val;
        this.totalfreq = totalfreq;
    }
}
