
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author huytran
 */
public class InvertedListProcessor {
    private ArrayList<Node> TermArray = new ArrayList<Node>();
    
    public void createTermArray(String fileName) {
        //read input file, insert each line to TermArray list
        File file = new File(fileName);
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            String line = new String();
            while ((line = input.readLine()) != null) {
                String[] temp = line.split(":");
                //System.out.println(temp[0]+temp[2]);
                Node node1 = new Node(temp[0], Integer.parseInt(temp[1]), temp[2]);
                TermArray.add(node1);
            }
        } catch (Exception e) {
            System.out.println("Failed to load inverted list");
            System.exit(0);
        }
        System.out.println("Finish loading inverted list "+fileName);
    }
    
    public String searchTerm(String query){
        String[] term = query.split(" ");
        int LowerBound = 0;
        int UpperBound = TermArray.size()-1;
        String rst = "";
        for(int i=0; i< term.length; i++){
            String s = searchTermAux(term[i], LowerBound, UpperBound);
            if (!s.equals("")){
                rst = rst+term[i]+" "+s+"\n";
            }
        }
        return rst;
    }
    
    public String searchTermAux(String query, int LowerBound, int UpperBound){
        if(LowerBound > UpperBound){
            System.out.println(query+" not found");
            return "";
        }
        int Median = (LowerBound + UpperBound)/2;
        if(query.compareToIgnoreCase(TermArray.get(Median).term) == 0){
            return TermArray.get(Median).totalfreq+" "+TermArray.get(Median).val;
        } else if(query.compareToIgnoreCase(TermArray.get(Median).term) < 0){
            UpperBound = Median-1;
            return searchTermAux(query, LowerBound, UpperBound);
        } else {
            LowerBound = Median+1;
            return searchTermAux(query, LowerBound, UpperBound);
        }
    }
    
}
