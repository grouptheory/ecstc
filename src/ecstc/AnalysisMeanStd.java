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
public class AnalysisMeanStd extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisMeanStd();
	}
	return _instance;
    }
    private AnalysisMeanStd() {}

    String getName() { return "MEAN_STD"; }

    double compute(Graph g, StatsTrue st, StatsECSTC se) {

        Set vset = g.getVertices();

	// double slope = AnalysisSlope.instance().compute(g, st, se);

	double sum=0.0;
	double ct=0.0;
        for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
            Vertex vquery_g = (Vertex)vit.next();
	    try {
		double std=se.getMeanStd(vquery_g);
		sum+=std;
		ct+=1.0;
            }
            catch(Exception ex) {
            }
        }

        return sum/ct;
    }
}
