package ecstc;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

/**
 * A class to collect measurements of the graph's vertices 
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class StatsTrue extends Stats {

    // map from G vertices to their measures
    private HashMap _g_raw_rank = new HashMap();

    // constructor
    public StatsTrue() {
    }

    // initialize the data structures for a given vertex
    public void setGraphMeasure(Vertex vquery_g, double val1) {
	_g_raw_rank.put(vquery_g, new Double(val1));
    }

    // get the measure of a vertex in G
    public double getGraphMeasure(Vertex vquery_g) {	
	Double grank = (Double)_g_raw_rank.get(vquery_g);
	return grank.doubleValue();
    }

    //------------------------------------------------------------

    public double getMeasure(Vertex vquery_g) { 
	return getGraphMeasure(vquery_g); 
    }

    public void setMeasure(Vertex vquery_g, double val) {
	setGraphMeasure(vquery_g, val);
    }
}
