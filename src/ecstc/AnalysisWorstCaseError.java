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
public class AnalysisWorstCaseError extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisWorstCaseError();
	}
	return _instance;
    }
    private AnalysisWorstCaseError() {}

    String getName() { return "WORST_CASE_ERROR"; }

    double compute(Graph g, StatsTrue st, StatsECSTC se) {

        Set vset = g.getVertices();
	double max=Double.MIN_VALUE;
        for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
            Vertex vquery_g = (Vertex)vit.next();
	    try {
		double ye=se.getMeanMean(vquery_g);
		double yt=st.getMeasure(vquery_g);
		double diff=100.0*Math.abs(yt-ye)/yt;
		if (diff>max) max=diff;
            }
            catch(Exception ex) {
            }
        }

        return max;
    }
}
