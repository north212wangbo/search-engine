/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datastructure;

import java.util.LinkedList;

/**
 *
 * @author huytran
 */

public class Output {
    public double handling_time;
    public int number_docs;
    public LinkedList<OutputDoc> output_doc_list = null;
    
    public Output(double handling_time, int number_docs){
        this.handling_time = handling_time;
        this.number_docs = number_docs;
        this.output_doc_list = new LinkedList<OutputDoc>();
    }
}
