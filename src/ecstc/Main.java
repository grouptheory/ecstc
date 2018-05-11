package ecstc;

import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeValue;

/**
 * Test driver class for the ECSTC
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Main {

    static void dump_graph(Graph gr) {

	System.out.println("*Vertices "+gr.getVertices().size());
	for (Iterator vit= gr.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    try {
		System.out.println(""+vquery_g);
	    }
	    catch (Exception ex) {
	    }
	}

	System.out.println("*Arcs");
	for (Iterator it = gr.getEdges().iterator(); it.hasNext();) {
	    DirectedSparseEdge e2 = (DirectedSparseEdge)it.next();
	    Vertex u1 = e2.getSource();
	    Vertex v1 = e2.getDest();
	    System.out.println(""+u1+" "+v1+" 1.0");
	}
    }

    static void dump_rds(RDS rds, Graph gr) {

	System.out.println("*Vertices "+rds._rds.getVertices().size());
	for (Iterator vit= rds._rds.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_r = (Vertex)vit.next();
	    Vertex vquery_g = rds.getGraphVertex(vquery_r);
	    try {
		System.out.println(""+vquery_g);
	    }
	    catch (Exception ex) {
	    }
	}

	System.out.println("*Arcs");
	for (Iterator it = rds._rds.getEdges().iterator(); it.hasNext();) {
	    DirectedSparseEdge e2 = (DirectedSparseEdge)it.next();
	    Vertex u1r = e2.getSource();
	    Vertex v1r = e2.getDest();
	    Vertex u1g = rds.getGraphVertex(u1r);
	    Vertex v1g = rds.getGraphVertex(v1r);
	    System.out.println(""+u1g+" "+v1g+" 1.0");
	}
    }


    static void dump_completion(Completion comp, RDS rds, Graph gr) {

	System.out.println("*Vertices "+comp._completion.getVertices().size());
	for (Iterator vit= comp._completion.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_c = (Vertex)vit.next();
	    Vertex vquery_r = comp.getRDSVertex(vquery_c);
	    Vertex vquery_g = rds.getGraphVertex(vquery_r);
	    try {
		System.out.println(""+vquery_g);
	    }
	    catch (Exception ex) {
	    }
	}

	System.out.println("*Arcs");
	for (Iterator it = comp._completion.getEdges().iterator(); it.hasNext();) {
	    DirectedSparseEdge e2 = (DirectedSparseEdge)it.next();
	    Vertex u1c = e2.getSource();
	    Vertex v1c = e2.getDest();
	    Vertex u1r = comp.getRDSVertex(u1c);
	    Vertex v1r = comp.getRDSVertex(v1c);
	    Vertex u1g = rds.getGraphVertex(u1r);
	    Vertex v1g = rds.getGraphVertex(v1r);
	    System.out.println(""+u1g+" "+v1g+" 1.0");
	}
    }


    public static NumberFormat formatter = new DecimalFormat("#000.0");

    private static String TAG, FNAME;

    // number of seeds
    private static int NUM_SEEDS = -1;

    // number of RDS trees to make
    private static int NUM_RDS_TREES = -1;

    // the number of completions for each RDS tree
    private static int NUM_COMPLETIONS = -1;

    private static final LinkedList EXPERIMENTS = new LinkedList();

    private static Graph g;
    private static int v;
    private static int e;

    /**
     * Main class
     *
     * @param args -- an array of arguments that are passed in on the command line.
     */

    public static void main(String [] args) {

	TAG = "example";
	FNAME = "example.dat";

	String NUM_SEEDSStr = "1";
	String NUM_RDS_TREESStr = "10";
	String NUM_COMPLETIONSStr = "10";
        
 	NUM_SEEDS = Integer.parseInt(NUM_SEEDSStr);
	NUM_RDS_TREES = Integer.parseInt(NUM_RDS_TREESStr);    
	NUM_COMPLETIONS = Integer.parseInt(NUM_COMPLETIONSStr);

	String NUM_RNGStr = System.getenv("RNGSEED");
	if (NUM_RNGStr == null) {
	    NUM_RNGStr = "0";
	}
	Randomness.RNGSEED = Integer.parseInt(NUM_RNGStr);

	MeasurementBC.getFactory();
	//MeasurementES.getFactory();
	//MeasurementCON.getFactory();
	//MeasurementHIER.getFactory();
	//MeasurementDijkstra.getFactory();

	Log.setLevel("results", Log.INFO);
	// Log.setLevel(RDS.class.getName(), Log.DEBUG);
	// Log.setLevel(Completion.class.getName(), Log.DEBUG);

	Log.diag(Main.class.getName(), Log.INFO, "Program starting normally.");

	g = null;
	v = e = 0;

	try {
	    PajekNetReader pnr = new PajekNetReader();

	    Log.diag(Main.class.getName(), Log.DEBUG, "Begin reading: "+FNAME);
	    g = pnr.load(FNAME);
	    Log.diag(Main.class.getName(), Log.DEBUG, "Done reading graph: "+FNAME);
	    
	    v = g.numVertices();
	    e = g.numEdges();

	    if (Params.SYMMETRIC) {
		HashSet revedges = new HashSet();

		for (Iterator it = g.getEdges().iterator(); it.hasNext();) {
		    DirectedSparseEdge e2 = (DirectedSparseEdge)it.next();
		    Vertex u1 = e2.getSource();
		    Vertex v1 = e2.getDest();
		    if (! v1.isPredecessorOf(u1)) {
			DirectedSparseEdge e2rev = new DirectedSparseEdge(v1,u1);
			revedges.add(e2rev);
		    }
		    Log.diag(Main.class.getName(), Log.INFO, "Edge: "+u1+","+v1);
		}

		for (Iterator it=revedges.iterator(); it.hasNext();) {
		    DirectedSparseEdge e2rev = (DirectedSparseEdge)it.next();
		    g.addEdge(e2rev);
		}
	    }
	}
	catch (IOException ex) {
	    Log.diag(Main.class.getName(), Log.FATAL, "Could not initialize, Exception: "+ex);
	}

	StatsTrueVector stats_true = new StatsTrueVector();
	StatsTrue stats_true_BC = (StatsTrue)stats_true.getStats(MeasurementBC.getFactory().getName());

	VertexMapper vm_true = new VertexMapperTrue();
	MeasurementVector m_true = new MeasurementVector(g, stats_true, vm_true);

	// use constant edge weight assigner
	EdgeWeightAssigner ews = new EdgeWeightAssigner_Constant();

	StatsECSTCVector stats_ecstc = new StatsECSTCVector();
	StatsECSTC stats_ecstc_BC = (StatsECSTC)stats_ecstc.getStats(MeasurementBC.getFactory().getName());

	// System.out.println("====True graph");
	// dump_graph(g);

	stats_ecstc.beginGraph(g);

	/*
	  System.out.println("====True measures");
	  for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	  // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    try {
		System.out.println(""+vquery_g+","+formatter.format(stats_true_BC.getMeasure(vquery_g))+"");
	    }
	    catch (Exception ex) {
	    }
	  }
	*/

	// iterate over RDS trees
	int rds_ct = 0;
	for (Iterator it = RDS.iterator(g, NUM_SEEDS, ews); it.hasNext();) {
	    RDS rds = (RDS)it.next();
	    if (++rds_ct > NUM_RDS_TREES ) break;

	    stats_ecstc.beginRDS(g);
	    
	    // System.out.println("====Tree "+rds_ct);
	    // dump_rds(rds, g);

	    int comp_ct = 0;
	    for (Iterator it2 = Completion.iterator(rds,g, Params.IGNORE_RDS_TREE); it2.hasNext();) {
		Completion comp = (Completion)it2.next();
		if (++comp_ct > NUM_COMPLETIONS ) break;
		
		stats_ecstc.beginCompletion();

		VertexMapper vm_ecstc = new VertexMapperECSTC(comp, rds, g);
		MeasurementVector mv = new MeasurementVector(comp._completion, stats_ecstc, vm_ecstc);

		stats_ecstc.endCompletion();

		// System.out.println("==== RDS "+rds_ct+", Completion "+comp_ct);
		// dump_completion(comp, rds, g);

		/*
		for (Iterator it3 = comp._completion.getEdges().iterator(); it3.hasNext();) {
		    DirectedSparseEdge e2 = (DirectedSparseEdge)it3.next();
		    Vertex u1 = e2.getSource();
		    Vertex v1 = e2.getDest();
		    Log.diag(Main.class.getName(), Log.INFO, "Completion Edge: "+u1+","+v1);
		}

		for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
		    // for each vertex in G
		    Vertex vquery_g = (Vertex)vit.next();
		    try {
			System.out.println(""+vquery_g+","+formatter.format(stats_ecstc_BC.getCompletionMeasure(vquery_g))+","+
					   formatter.format(stats_true_BC.getMeasure(vquery_g)));
				 
		    }
		    catch (Exception ex) {
		    }
		}
		*/

		/*
		  BetweennessCentrality ranker = new BetweennessCentrality(comp._completion, true, false);
		  ranker.evaluate();
		  ranker.printRankings(true,true);
		*/
	    }

	    stats_ecstc.endRDS(g, m_true);
	}

	stats_ecstc.endGraph(g);

	printHeader();
	Experimenter.instance().execute(g, stats_true, stats_ecstc);

	Log.diag(Main.class.getName(), Log.INFO, "Program ending normally.");
    }

    static void printHeader() {
	// now take measurements of each vertex in the completion
	Log.diag("results", Log.INFO, "# Tag "+TAG);
	Log.diag("results", Log.INFO, "# Fields 6");
	Log.diag("results", Log.INFO, "# FILENAME "+FNAME);
	Log.diag("results", Log.INFO, "# NUM_SEEDS "+NUM_SEEDS);
	Log.diag("results", Log.INFO, "# NUM_RDS_TREES "+NUM_RDS_TREES);
	Log.diag("results", Log.INFO, "# NUM_COMPLETIONS "+NUM_COMPLETIONS);
	Log.diag("results", Log.INFO, "# SYMMETRIC "+Params.SYMMETRIC);
	Log.diag("results", Log.INFO, "# RNGSEED "+Randomness.RNGSEED);
	Log.diag("results", Log.INFO, "# VERTICES "+v);
	Log.diag("results", Log.INFO, "# EDGES "+e);
    }

};

