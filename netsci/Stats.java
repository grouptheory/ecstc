package netsci;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

/**
 * A class to collect measurements of the graph's vertices and
 * aggregate statistics about the vertices of the completions
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Stats {

    // map from G vertices to their measures
    private HashMap _g_rank = new HashMap();
    // map from completion vertices to their accumulated measures
    private HashMap _c_rank_accum = new HashMap();
    // map from completion vertices to number of measurements
    private HashMap _c_rank_ct = new HashMap();

    // initialize the data structures for a given vertex
    public void initialize(Vertex vquery_g, double val1) {
	_g_rank.put(vquery_g, new Double(val1));
	_c_rank_accum.put(vquery_g, new Double(0.0));
	_c_rank_ct.put(vquery_g, new Double(0.0));
    }

    // accumulate the measure of a vertex in the completion
    public void accum(Vertex vquery_g, double val3) {	
	_c_rank_accum.put(vquery_g, new Double(((Double)_c_rank_accum.get(vquery_g)).doubleValue()+val3));
	_c_rank_ct.put(vquery_g, new Double(((Double)_c_rank_ct.get(vquery_g)).doubleValue()+1.0));
    }

    // get the measure of a vertex in G
    public double get_grank(Vertex vquery_g) {	
	Double grank = (Double)_g_rank.get(vquery_g);
	return grank.doubleValue();
    }

    // get the number of measures taken for a vertex (in the completions)
    public double get_count(Vertex vquery_g) {
	Double ctr = (Double)_c_rank_ct.get(vquery_g);
	return ctr.doubleValue();
    }

    // get the mean measures taken for a vertex (in the completions)
    public double get_crank(Vertex vquery_g) throws Exception {
	double ctr =  get_count(vquery_g);
	if (ctr == 0.0) {
	    throw new Exception("No data about this vertex...");
	}
	Double crank = (Double)_c_rank_accum.get(vquery_g);
	return crank.doubleValue()/ctr;
    }


    // If I were to rank the vertices of the completion(s) by their
    // G-rank, and then ask for the cutoff value of G-rank which would
    // separate the top cutoff% from the rest, what would this cutoff
    // value be?
    public double get_grank_threshold(double cutoff, Graph g) {

	double grank_threshold = -1.0;

	TreeMap rank2list = new TreeMap();
	Set vset = g.getVertices();
	int n = 0;

	for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    // if we don't have completion data skip it
	    if (get_count(vquery_g)<1.0) {
		continue;
	    }
	    n++;

	    double sval = get_grank(vquery_g);
	    Double score = new Double(sval);
	    LinkedList lst = (LinkedList)rank2list.get(score);
	    if (lst==null) {
		lst = new LinkedList();
		rank2list.put(score,lst);
		// System.out.println("new list for score="+score);
	    }
	    lst.addLast(vquery_g);
	}

	int n_cutoff = (int)((double)n*cutoff);

	//System.out.println("n="+n);
	//System.out.println("n_cutoff="+n_cutoff);

	int ct=0;
	for (Iterator vit= rank2list.entrySet().iterator(); vit.hasNext(); ) {
	    Map.Entry ent = (Map.Entry)vit.next();
	    Double score = (Double)ent.getKey();
	    LinkedList lst = (LinkedList)ent.getValue();
	    ct += lst.size();

	    // System.out.println("ct="+ct+", score="+score);

	    if (ct > n_cutoff) {
		grank_threshold = score.doubleValue();
		break;
	    }
	}

	return grank_threshold;
    }

    public double get_crank_threshold(double cutoff, Graph g) {

	double crank_threshold = -1.0;

	TreeMap rank2list = new TreeMap();
	Set vset = g.getVertices();
	int n = 0;

	for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    // if we don't have completion data skip it
	    if (get_count(vquery_g)<1.0) {
		continue;
	    }
	    n++;

	    double sval;
	    try {
		sval = get_crank(vquery_g);
	    }
	    catch (Exception ex) {
		// no data on this vertex
		continue;
	    }
	    Double score = new Double(sval);

	    LinkedList lst = (LinkedList)rank2list.get(score);
	    if (lst==null) {
		lst = new LinkedList();
		rank2list.put(score,lst);
		// System.out.println("new list for score="+score);
	    }
	    lst.addLast(vquery_g);
	}

	int n_cutoff = (int)((double)n*cutoff);

	//System.out.println("n="+n);
	//System.out.println("n_cutoff="+n_cutoff);

	int ct=0;
	for (Iterator vit= rank2list.entrySet().iterator(); vit.hasNext(); ) {
	    Map.Entry ent = (Map.Entry)vit.next();
	    Double score = (Double)ent.getKey();
	    LinkedList lst = (LinkedList)ent.getValue();
	    ct += lst.size();

	    // System.out.println("ct="+ct+", score="+score);

	    if (ct > n_cutoff) {
		crank_threshold = score.doubleValue();
		break;
	    }
	}

	return crank_threshold;
    }

    public LinkedList filterVertices_min_grank(Graph g, double min_grank) {
	LinkedList lst = new LinkedList();
	Set vset = g.getVertices();
	for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    // if we don't have completion data skip it
	    if (get_count(vquery_g)<1.0) {
		continue;
	    }
	    double sval = get_grank(vquery_g);
	    if (sval > min_grank) {
		lst.addLast(vquery_g);
	    }
	}
	return lst;
    }

    public LinkedList filterVertices_min_crank(Graph g, double min_crank) {
	LinkedList lst = new LinkedList();
	Set vset = g.getVertices();
	for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    // if we don't have completion data skip it
	    if (get_count(vquery_g)<1.0) {
		continue;
	    }
	    double sval;
	    try {
		sval = get_crank(vquery_g);
	    }
	    catch (Exception ex) {
		// no data on this vertex
		continue;
	    }
	    if (sval > min_crank) {
		lst.addLast(vquery_g);
	    }
	}
	return lst;
    }

    public String error(Graph g, double th) {
	double thg = get_grank_threshold(th, g);
	double thc = get_crank_threshold(th, g);
	LinkedList glist = filterVertices_min_grank(g, thg);
	LinkedList clist = filterVertices_min_crank(g, thc);
	clist.removeAll(glist);
	double err = (double)clist.size()/(double)glist.size();
	String s = (""+Test.twoPlaces.format(th)+
		    "\t"+Test.twoPlaces.format(err)+
		    "\t|\t"+glist.size()+"\t"+
		    Test.twoPlaces.format(thg)+"\t|\t"+
		    clist.size()+"\t"+
		    Test.twoPlaces.format(thc));
	return s;
    }

    public double pearson(Graph g) {
	Set vset = g.getVertices();
	int N = 0;
	for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    // if we don't have completion data skip it
	    if (get_count(vquery_g)<1.0) {
		continue;
	    }
	    N++;
	}
	double x[] = new double[N];
	double y[] = new double[N];

	int j=0;
	for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    // if we don't have completion data skip it
	    if (get_count(vquery_g)<1.0) {
		continue;
	    }
	    try {
		x[j]=get_grank(vquery_g);
		y[j]=get_crank(vquery_g);
		j++;
	    }
	    catch (Exception ex) {
		System.out.printf("Invariant violation!");
		System.exit(0);
	    }
	}

	double sum_sq_x = 0;
	double sum_sq_y = 0;
	double sum_coproduct = 0;
	double mean_x = x[1];
	double mean_y = y[1];
	for (int i=2; i<N; i++) {
	    double sweep = (i - 1.0) / i;
	    double delta_x = x[i] - mean_x;
	    double delta_y = y[i] - mean_y;
	    sum_sq_x += delta_x * delta_x * sweep;
	    sum_sq_y += delta_y * delta_y * sweep;
	    sum_coproduct += delta_x * delta_y * sweep;
	    mean_x += delta_x / i;
	    mean_y += delta_y / i;
	}
	double pop_sd_x = Math.sqrt( sum_sq_x / (double)N );
	double pop_sd_y = Math.sqrt( sum_sq_y / (double)N );
	double cov_x_y = sum_coproduct / (double)N;
	double correlation = cov_x_y / (pop_sd_x * pop_sd_y);
	return correlation;
    }
}
