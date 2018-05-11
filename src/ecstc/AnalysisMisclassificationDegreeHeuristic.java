package ecstc;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A class to collect measurements of the graph's vertices and
 * aggregate statistics about the vertices of the completions
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class AnalysisMisclassificationDegreeHeuristic extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisMisclassificationDegreeHeuristic();
	}
	return _instance;
    }
    private AnalysisMisclassificationDegreeHeuristic() {}

    String getName() { return "MISCLASS_DEGREE_HEURISTIC"; }

    double compute(Graph g, StatsTrue st, StatsECSTC se) {
	int bad = 0;
	int total = 0;
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    
	    for (Iterator vit2= g.getVertices().iterator(); vit2.hasNext(); ) {
		// for each vertex in G
		Vertex vquery_g2 = (Vertex)vit2.next();
		
		if (vquery_g == vquery_g2) continue;
		
		try {
		    double skew =
			(st.getMeasure(vquery_g) - st.getMeasure(vquery_g2)) * 
			(( vquery_g.inDegree()+ vquery_g.outDegree()) - ( vquery_g2.inDegree()+ vquery_g2.outDegree()));
		    if (skew < 0) bad++;
		    total++;
		}
		catch (Exception ex) {
		}
	    }
	}
	if (total==0) return -1.0;
	else return 100.0*(double)bad/(double)total;
    }
}
