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
public class AnalysisMisclassification extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisMisclassification();
	}
	return _instance;
    }
    private AnalysisMisclassification() {}

    String getName() { return "MISCLASS"; }

    double compute(Graph g, StatsTrue st, StatsECSTC se) {
	int bad = 0;
	int badmag1 = 0;
	int badmag2 = 0;
	int badsign = 0;
	int total = 0;
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    
	    for (Iterator vit2= g.getVertices().iterator(); vit2.hasNext(); ) {
		// for each vertex in G
		Vertex vquery_g2 = (Vertex)vit2.next();
		
		if (vquery_g == vquery_g2) continue;
		
		try {
		    double truediff = (st.getMeasure(vquery_g) - st.getMeasure(vquery_g2));
		    double estdiff = (se.getMeanMean(vquery_g) - se.getMeanMean(vquery_g2));
		    double truediffabs = Math.abs(truediff);
		    double estdiffabs = Math.abs(estdiff);
		    boolean truediffbig = (truediffabs/Math.max(st.getMeasure(vquery_g),st.getMeasure(vquery_g2))) > 0.00;
		    boolean estdiffbig = (estdiffabs/Math.max(se.getMeanMean(vquery_g),se.getMeanMean(vquery_g2))) > 0.05;
		    
		    if (!truediffbig) truediff=0.0;
		    if (!estdiffbig) estdiff=0.0;
		    
		    if (!truediffbig && estdiffbig) {
			bad++;
			badmag1++;
		    }
		    if (truediffbig && !estdiffbig) {
			bad++;
			badmag2++;
		    }
		    if (truediffbig && estdiffbig) {
			double skew = truediff*estdiff;
			if (skew<0) {
			    badsign++;
			    bad++;
			}
		    }

		    total++;
		}
		catch (Exception ex) {
		}
	    }
	}

	System.out.println("badmag1 "+badmag1+"badmag2 "+badmag2+", badsign "+badsign);

	if (total==0) return -1.0;
	else return 100.0*(double)bad/(double)total;
    }
}
