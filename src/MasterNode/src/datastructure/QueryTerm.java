/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datastructure;

/**
 * Object QueryTerm contain A Term being queried in a list of multi queried terms
 * @author Hoang Le
 */
public class QueryTerm {
    public String strTerm;
    public int timeOccur;

    public QueryTerm(String _strTerm) {
        this.strTerm = _strTerm;
        this.timeOccur = 1;
    }    
}