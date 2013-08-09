/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package QueryProcessing;
import datastructure.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author huytran
 */
public class AndOrQueryProcessor {
    
     public ArrayList<DocTermList> processAndQuery(LinkedList<TermDocList> term_doc_list){
        //Initialize the 1st docTerm List
        ArrayList<DocTermList> list1 = new ArrayList<DocTermList>();
        if (term_doc_list.size() > 0) {
            for (int i = 0; i < term_doc_list.get(0).docList.size(); i++) {
                DocTermList docTermList1 = new DocTermList();
                ArrayList<TermInfo> terminfoList = new ArrayList<TermInfo>();
                TermInfo terminfo1 = new TermInfo();
                docTermList1.docID = term_doc_list.get(0).docList.get(i).docID;
                terminfo1.term = term_doc_list.get(0).term;
                terminfo1.freq = term_doc_list.get(0).docList.get(i).freq;
                terminfo1.totalfreq = term_doc_list.get(0).totalfreq;
                terminfoList.add(terminfo1);
                docTermList1.termList = terminfoList;
                list1.add(docTermList1); //list1 will be the result of 'and' everytime
            }

            //For each term
            for (int i = 1; i < term_doc_list.size(); i++) {
                ArrayList<DocTermList> tempList = new ArrayList<DocTermList>();
                //For each doc
                for (int j = 0, k = 0; (j < term_doc_list.get(i).docList.size() && k < list1.size());) {
                    if (term_doc_list.get(i).docList.get(j).docID.compareTo(list1.get(k).docID) < 0) {
                        j++;
                    } else if (term_doc_list.get(i).docList.get(j).docID.compareTo(list1.get(k).docID) > 0) {
                        k++;
                    } else {
                        DocTermList tempdocTermList = list1.get(k);
                        TermInfo tempTermInfo = new TermInfo();
                        tempTermInfo.term = term_doc_list.get(i).term;
                        tempTermInfo.freq = term_doc_list.get(i).docList.get(j).freq;
                        tempTermInfo.totalfreq = term_doc_list.get(i).totalfreq;
                        tempdocTermList.termList.add(tempTermInfo);
                        tempList.add(tempdocTermList);
                        j++;
                        k++;
                    }
                }
                list1 = tempList;

            }
        }
        
        //print docTerm List
//        for(int i=0; i<list1.size();i++){
//            System.out.print(list1.get(i).docID+" ");
//            for(int j=0; j<list1.get(i).termList.size();j++){
//                System.out.print(list1.get(i).termList.get(j).term+","+list1.get(i).termList.get(j).freq+" ");
//            }
//            System.out.println();
//        }      
        return list1;
    }
    
    public ArrayList<DocTermList> processOrQuery(LinkedList<TermDocList> term_doc_list) {
        
        ArrayList<DocTermList> list1 = new ArrayList<DocTermList>();
        if (term_doc_list.size() > 0) {
            for (int i = 0; i < term_doc_list.get(0).docList.size(); i++) {
                DocTermList docTermList1 = new DocTermList();
                ArrayList<TermInfo> terminfoList = new ArrayList<TermInfo>();
                TermInfo terminfo1 = new TermInfo();
                docTermList1.docID = term_doc_list.get(0).docList.get(i).docID;
                terminfo1.term = term_doc_list.get(0).term;
                terminfo1.freq = term_doc_list.get(0).docList.get(i).freq;
                terminfo1.totalfreq = term_doc_list.get(0).totalfreq;
                terminfoList.add(terminfo1);
                docTermList1.termList = terminfoList;
                list1.add(docTermList1); //list1 will be the result of 'and' everytime
            }

            //For each term
            for (int i = 1; i < term_doc_list.size(); i++) {
                ArrayList<DocTermList> tempList = new ArrayList<DocTermList>();
                //For each doc
                for (int j = 0, k = 0; (j < term_doc_list.get(i).docList.size() && k < list1.size());) {
                    if (term_doc_list.get(i).docList.get(j).docID.compareTo(list1.get(k).docID) < 0) {
                        DocTermList tempdocTermList = new DocTermList();
                        tempdocTermList.docID = term_doc_list.get(i).docList.get(j).docID;
                        tempdocTermList.termList = new ArrayList<TermInfo>();
                        TermInfo newtermInfo = new TermInfo();
                        newtermInfo.term = term_doc_list.get(i).term;
                        newtermInfo.freq = term_doc_list.get(i).docList.get(j).freq;
                        newtermInfo.totalfreq = term_doc_list.get(i).totalfreq;
                        tempdocTermList.termList.add(newtermInfo);
                        
                        tempList.add(tempdocTermList);
                        j++;
                    } else if (term_doc_list.get(i).docList.get(j).docID.compareTo(list1.get(k).docID) > 0) {
                        DocTermList tempdocTermList = new DocTermList();
                        tempdocTermList = list1.get(k);
                        tempList.add(tempdocTermList);
                        k++;
                    } else {
                        DocTermList tempdocTermList = list1.get(k);
                        TermInfo tempTermInfo = new TermInfo();
                        tempTermInfo.term = term_doc_list.get(i).term;
                        tempTermInfo.freq = term_doc_list.get(i).docList.get(j).freq;
                        tempTermInfo.totalfreq = term_doc_list.get(i).totalfreq;
                        tempdocTermList.termList.add(tempTermInfo);
                        tempList.add(tempdocTermList);
                        j++;
                        k++;
                    }
                }
                list1 = tempList;

            }
        }
        
        //print docTerm List
//        for(int i=0; i<list1.size();i++){
//            System.out.print(list1.get(i).docID+" ");
//            for(int j=0; j<list1.get(i).termList.size();j++){
//                System.out.print(list1.get(i).termList.get(j).term+","+list1.get(i).termList.get(j).freq+" ");
//            }
//            System.out.println();
//        }      
        return list1;
    }
}
