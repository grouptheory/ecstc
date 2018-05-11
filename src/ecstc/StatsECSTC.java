package ecstc;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

/**
 * A class to collect measurements of the aggregate statistics about
 * the vertices of the completions
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class StatsECSTC extends Stats {

    public StatsECSTC() { }

    //------------------------------------------------------------

    private final HashMap _c_rank_set = new HashMap();

    // notify CompletionStats that this is the start of a new completion's measure data
    public void beginCompletion() {
    }

    // save the measure of a vertex in current completion
    public void setCompletionMeasure(Vertex vquery_g, double val) {
	((LinkedList)_c_rank_set.get(vquery_g)).addLast(new Double(val));
    }

    // save the measure of a vertex in current completion
    public double getCompletionMeasure(Vertex vquery_g) {
	return ((Double)(((LinkedList)_c_rank_set.get(vquery_g))).getLast()).doubleValue();
    }

    // end of the current completion's measure data
    public void endCompletion() { 
    }

    //------------------------------------------------------------

    private final HashMap _r_meanrank_set = new HashMap();
    private final HashMap _r_stdrank_set = new HashMap();

    // constructor
    public void beginRDS(Graph g) {

        for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
            Vertex vquery_g = (Vertex)vit.next();
	    LinkedList list = (LinkedList)_c_rank_set.get(vquery_g);
	    if (list==null) {
		_c_rank_set.put(vquery_g, new LinkedList());
	    }
	    else {
		list.clear();
	    }

	}
    }
    
    // notify CompletionStats that this is the end of the consideration of the
    // current RDS tree's completions
    public void endRDS(Graph g, Measurement mtrue) {
        Set vset = g.getVertices();
        for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
            Vertex vquery_g = (Vertex)vit.next();
	    LinkedList list = (LinkedList)_c_rank_set.get(vquery_g);
	    if (list.size() > 0) {
		double mean = mean(list);
		double std = std(list, mean);

		// NORMALIZE STANDARD DEVIATIONS BY THE TRUE MEASURE
		// double trueval = mtrue.readValue(vquery_g);
		// std = 100.0 * (std/trueval);

		// NORMALIZE STANDARD DEVIATIONS BY THE MEAN OF THE ESTIMATES
		std = 100.0 * (std/mean);

		((LinkedList)_r_meanrank_set.get(vquery_g)).addLast(new Double(mean));
		((LinkedList)_r_stdrank_set.get(vquery_g)).addLast(new Double(std));
	    }
	}
    }

    //------------------------------------------------------------

    private final HashMap _g_meanmeanrank_set = new HashMap();
    private final HashMap _g_stdmeanrank_set = new HashMap();
    private final HashMap _g_meanstdrank_set = new HashMap();
    private final HashMap _g_stdstdrank_set = new HashMap();
    
    // constructor
    public void beginGraph(Graph g) {
        for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
            Vertex vquery_g = (Vertex)vit.next();
	    LinkedList list;

	    list = (LinkedList)_r_meanrank_set.get(vquery_g);
	    if (list==null) {
		_r_meanrank_set.put(vquery_g, new LinkedList());
	    }
	    else {
		list.clear();
	    }

	    list = (LinkedList)_r_stdrank_set.get(vquery_g);
	    if (list==null) {
		_r_stdrank_set.put(vquery_g, new LinkedList());
	    }
	    else {
		list.clear();
	    }

	    list = (LinkedList)_g_meanmeanrank_set.get(vquery_g);
	    if (list==null) _g_meanmeanrank_set.put(vquery_g, new LinkedList());
	    else list.clear();

	    list = (LinkedList)_g_stdmeanrank_set.get(vquery_g);
	    if (list==null) _g_stdmeanrank_set.put(vquery_g, new LinkedList());
	    else list.clear();

	    list = (LinkedList)_g_meanstdrank_set.get(vquery_g);
	    if (list==null) _g_meanstdrank_set.put(vquery_g, new LinkedList());
	    else list.clear();

	    list = (LinkedList)_g_stdstdrank_set.get(vquery_g);
	    if (list==null) _g_stdstdrank_set.put(vquery_g, new LinkedList());
	    else list.clear();
	}
    }

    public void endGraph(Graph g) {
        Set vset = g.getVertices();
        for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
            Vertex vquery_g = (Vertex)vit.next();
	    LinkedList list;

	    list = (LinkedList)_r_meanrank_set.get(vquery_g);
	    if (list.size() > 0) {
		double mean = mean(list);
		double std = std(list, mean);
		((LinkedList)_g_meanmeanrank_set.get(vquery_g)).addLast(new Double(mean));
		((LinkedList)_g_stdmeanrank_set.get(vquery_g)).addLast(new Double(std));
	    }

	    list = (LinkedList)_r_stdrank_set.get(vquery_g);
	    if (list.size() > 0) {
		double mean = mean(list);
		double std = std(list, mean);
		((LinkedList)_g_meanstdrank_set.get(vquery_g)).addLast(new Double(mean));
		((LinkedList)_g_stdstdrank_set.get(vquery_g)).addLast(new Double(std));
	    }
	}
    }

    //------------------------------------------------------------

    public double getMeanMean(Vertex vquery_g) throws Exception {
	LinkedList list = (LinkedList)_g_meanmeanrank_set.get(vquery_g);
	if (list.size() > 0) return ((Double)list.getFirst()).doubleValue();
	else throw new Exception("Not measured");
    }

    public double getStdMean(Vertex vquery_g) throws Exception {
	LinkedList list = (LinkedList)_g_stdmeanrank_set.get(vquery_g);
	if (list.size() > 0) return ((Double)list.getFirst()).doubleValue();
	else throw new Exception("Not measured");
    }

    public double getMeanStd(Vertex vquery_g) throws Exception  {
	LinkedList list = (LinkedList)_g_meanstdrank_set.get(vquery_g);
	if (list.size() > 0) return ((Double)list.getFirst()).doubleValue();
	else throw new Exception("Not measured");
    }

    public double getStdStd(Vertex vquery_g) throws Exception {
	LinkedList list = (LinkedList)_g_stdstdrank_set.get(vquery_g);
	if (list.size() > 0) return ((Double)list.getFirst()).doubleValue();
	else throw new Exception("Not measured");
    }

    //------------------------------------------------------------

    public double getMeasure(Vertex vquery_g) throws Exception {
	return getMeanMean(vquery_g); 
    }

    public void setMeasure(Vertex vquery_g, double val) {
	setCompletionMeasure(vquery_g, val);
    }

    //------------------------------------------------------------

    private double mean(LinkedList list) {
	double accum = 0.0;
	for (Iterator it=list.iterator(); it.hasNext();) {
	    Double item = (Double)it.next();
	    accum += item.doubleValue();
	}
	return accum/(double)list.size();
    }

    private double std(LinkedList list, double mean) {
	double accum = 0.0;
	for (Iterator it=list.iterator(); it.hasNext();) {
	    Double item = (Double)it.next();
	    double devFromMean = (item.doubleValue() - mean);
	    accum += (devFromMean * devFromMean);
	}
	return Math.sqrt(accum/(double)list.size());
    }
}


