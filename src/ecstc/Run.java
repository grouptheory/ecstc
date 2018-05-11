package ecstc;

import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.io.*;
import java.util.*;

/**
 * Test driver class for the ECSTC
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Run {

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

	if (args.length!=6) {
	    Log.diag(Main.class.getName(), Log.FATAL, "Usage: <tag> <filename> <numSeeds> <numRDStrees> <numCompletions> <RNGseed>\n");
	}

	TAG = args[0];
	FNAME = args[1];

	String NUM_SEEDSStr = args[2];
	String NUM_RDS_TREESStr = args[3];
	String NUM_COMPLETIONSStr = args[4];
        
 	NUM_SEEDS = Integer.parseInt(NUM_SEEDSStr);
	NUM_RDS_TREES = Integer.parseInt(NUM_RDS_TREESStr);    
	NUM_COMPLETIONS = Integer.parseInt(NUM_COMPLETIONSStr);

	String NUM_RNGStr = System.getenv("RNGSEED");
	if (NUM_RNGStr == null) {
	    NUM_RNGStr = args[5];
	}
	Randomness.RNGSEED = Integer.parseInt(NUM_RNGStr);

	MeasurementBC.getFactory();
	MeasurementES.getFactory();
	MeasurementCON.getFactory();
	MeasurementHIER.getFactory();
	MeasurementDijkstra.getFactory();

	Log.setLevel("results", Log.DEBUG);
	Log.setLevel(Main.class.getName(), Log.DEBUG);
	Log.setLevel(RDS.class.getName(), Log.DEBUG);
	Log.setLevel(Completion.class.getName(), Log.DEBUG);

	Log.diag(Main.class.getName(), Log.INFO, "Program starting normally.");

	g = null;
	v = e = 0;

	try {
	    PajekNetReader pnr = new PajekNetReader();

	    Log.diag(Main.class.getName(), Log.DEBUG, "Begin reading: "+FNAME);
	    g = pnr.load(FNAME);
	    Log.diag(Main.class.getName(), Log.DEBUG, "Done reading graph: "+FNAME);

	    if (Params.SYMMETRIC) {
		GraphUtils.symmetrizeAsDirectedGraph(g);
	    }

	    v = g.numVertices();
	    e = g.numEdges();

	    Log.diag(Main.class.getName(), Log.INFO, "G = ("+v+","+e+") ");
	}
	catch (IOException ex) {
	    Log.diag(Main.class.getName(), Log.FATAL, "Could not initialize, Exception: "+ex);
	}

	StatsTrueVector stats_true = new StatsTrueVector();
	VertexMapper vm_true = new VertexMapperTrue();
	MeasurementVector m_true = new MeasurementVector(g, stats_true, vm_true);

	// use constant edge weight assigner
	EdgeWeightAssigner ews = new EdgeWeightAssigner_Constant();

	StatsECSTCVector stats_ecstc = new StatsECSTCVector();
	stats_ecstc.beginGraph(g);

	// iterate over RDS trees
	int rds_ct = 0;
	for (Iterator it = RDS.iterator(g, NUM_SEEDS, ews); it.hasNext();) {
	    RDS rds = (RDS)it.next();
	    if (++rds_ct > NUM_RDS_TREES ) break;

	    stats_ecstc.beginRDS(g);
	    
	    int comp_ct = 0;
	    for (Iterator it2 = Completion.iterator(rds,g, Params.IGNORE_RDS_TREE); it2.hasNext();) {
		Completion comp = (Completion)it2.next();
		if (++comp_ct > NUM_COMPLETIONS ) break;
		
		stats_ecstc.beginCompletion();

		VertexMapper vm_ecstc = new VertexMapperECSTC(comp, rds, g);
		MeasurementVector mv = new MeasurementVector(comp._completion, stats_ecstc, vm_ecstc);

		stats_ecstc.endCompletion();

		Log.diag(Main.class.getName(), Log.INFO, 
			 "RDS "+rds_ct+", Completion "+comp_ct+
			 ", RDS = ("+rds.getV()+","+rds.getE()+") "+
			 ", Comp = ("+comp.getV()+","+comp.getE()+") "+
			 ", Efficiency = "+comp.getEfficiency()+
			 ", Wastage = "+comp.getWastage());
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

