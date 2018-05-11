package netsci;

import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

/**
 * Test driver class for the NetSci
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Test {

    // debugging
    protected static final boolean DEBUG = true;

    // number of seeds
    private static int NUM_SEEDS = 10;

    // number of RDS trees to make
    private static int NUM_RDS_TREES = 10;

    // the number of completions for each RDS tree
    private static int NUM_COMPLETIONS = 10;

    // the number of snapshots
    private static int NUM_SNAPSHOTS = 1;

    // biased RDS and Completions
    private static boolean BIASED = false;

    // the experiment
    private static String EXPERIMENT = "unknown";

    // string formatter
    public static DecimalFormat twoPlaces = new DecimalFormat("0.00000");

    // statistics collector
    private static Stats _stats = new Stats();

    /**
     * Main class
     *
     * @param args -- an array of arguments that are passed in on the command line.
     */

    // static method that defines the graph parameter we want to study
    private static Measurable makeMeasurable(Graph graph) {
	if (EXPERIMENT.equals("H")) return new MeasurableHubness(graph);
	else if (EXPERIMENT.equals("A")) return new MeasurableAuthority(graph);
	else if (EXPERIMENT.equals("BC")) return new MeasurableBCRaw(graph);
	else if (EXPERIMENT.equals("ES")) return new MeasurableEffectiveSize(graph);
	else if (EXPERIMENT.equals("CON")) return new MeasurableConstraint(graph);
	else {
	    System.out.println("Unknown experiment: "+EXPERIMENT);
	    System.exit(0);
	}
	return null;
    }

    // static method that defines how we build RDS trees
    private static Iterator makeRDSIterator(Graph graph, int seeds) {
	if (BIASED) {
	    return RDSBiased.iterator(graph, seeds);
	}
	else {
	    return RDS.iterator(graph, seeds);
	}
    }

    // static method that defines how we build completions
    private static Iterator makeCompletionIterator(RDS rds, Graph g) {
	if (BIASED) {
	    return CompletionBiased.iterator(rds, g);
	}
	else {
	    return Completion.iterator(rds, g);
	}
    }

    public static void main(String [] args) {

	if (args.length!=6) {
	    System.out.println("Usage: <filename> <numSeeds> <numRDStrees> <numCompletions> <biased> <exp>");
	    System.out.println("biased: 0/1");
	    System.out.println("exp: H, A, BC, ES, CON");
	}

	String fname = args[0];
	String NUM_SEEDSStr = args[1];
	String NUM_RDS_TREESStr = args[2];
	String NUM_COMPLETIONSStr = args[3];
	String BIASEDStr = args[4];

	NUM_SEEDS = Integer.parseInt(NUM_SEEDSStr);
	NUM_RDS_TREES = Integer.parseInt(NUM_RDS_TREESStr);
	NUM_COMPLETIONS = Integer.parseInt(NUM_COMPLETIONSStr);
	int tmpBIASED = Integer.parseInt(BIASEDStr);
	if (tmpBIASED==1) {
	    BIASED = true;
	}
	else {
	    BIASED = false;
	}
	EXPERIMENT = args[5];
	
	long stage = 0;
	long maxstage = NUM_RDS_TREES * NUM_COMPLETIONS; 
	long blip = maxstage / NUM_SNAPSHOTS;
	if (blip<1) blip=1;

	if (DEBUG) System.out.println("Begin reading: "+fname);
	try {
	    PajekNetReader pnr = new PajekNetReader();
	    Graph g = pnr.load(fname);
	    int v = g.numVertices();
	    int e = g.numEdges();
	    if (DEBUG) System.out.println("Done reading graph: "+fname);
	    if (DEBUG) System.out.println("Graph specs: vertices="+v+", edges="+e);
	    // if (DEBUG) debugOutput(g);

	    // undirectify the graph
	    g = undirectify(g);

	    // compute measures for vertices in the graph
	    Measurable measG = makeMeasurable(g);
	    for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
		Vertex vquery_g = (Vertex)vit.next();
		double val1 = measG.readValue(vquery_g);
		_stats.initialize(vquery_g, val1);
	    }

	    int rds_ct = 1;
	    // iterate over RDS trees
	    for (Iterator it = makeRDSIterator(g,NUM_SEEDS); it.hasNext();) {
		// get the next RDS tree
		RDS rds = (RDS)it.next();
		if (DEBUG) System.out.println(""+rds.toString());

		int comp_ct = 1;
		// iterate over completions
		for (Iterator it2 = makeCompletionIterator(rds,g); it2.hasNext();) {
		    // get the next completion
		    Completion comp = (Completion)it2.next();
		    if (DEBUG) System.out.println(""+comp.toString());

		    // make the measurement apparatus
		    Measurable measComp = makeMeasurable(comp._completion);

		    // now take measurements of each vertex in the completion
		    for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
			// for each vertex in G
			Vertex vquery_g = (Vertex)vit.next();
			Vertex vquery_r = rds.getRDSVertex(vquery_g);
			if (vquery_r==null) {
			    // only consider vertices also in the RDS tree
			    continue;
			}
			
			// compute measures for vertices in the completion
			Vertex vquery_c = comp.getCompletionVertex(vquery_r);
			double val3 = measComp.readValue(vquery_c);
			_stats.accum(vquery_g, val3);
		    }

		    WeakComponentClusterer wcc = new WeakComponentClusterer();
		    ClusterSet cs = wcc.extract(comp._completion);
		    if (DEBUG) System.out.println("Components = "+cs.size());
		    
		    stage++;
		    if (stage % blip == 0) {
			// output aggregate statistics to a file every blip completions....
			output(stage, g);
		    }

		    comp_ct++;
		    if (comp_ct> NUM_COMPLETIONS ) {
			// done making completions
			break;
		    }
		}

		rds_ct++;
		if (rds_ct> NUM_RDS_TREES ) {
		    // done making RDS trees
		    break;
		}
	    }

	    // output the final aggregate statistics
	    output(-1, g);

	    // output the lift curve & pearson
	    outputError(g);
	    outputErrorPearson(g);
	}
	catch (IOException ex) {
	    System.out.println("I/O error: "+ex);
	}
    }

    public static void outputError(Graph g) {
	try {
	    String logname = "output."+NUM_SEEDS+"."+NUM_RDS_TREES+"."+NUM_COMPLETIONS+".error";
	    BufferedWriter out = new BufferedWriter(new FileWriter(logname));
	    for (double th=0.5; th<1.0; th+=0.05) {
		out.write(_stats.error(g, th)+"\n");
	    }
	    out.close();
	} catch (IOException ex) {
	} 
    }

    public static void outputErrorPearson(Graph g) {
	try {
	    String logname = "output."+NUM_SEEDS+"."+NUM_RDS_TREES+"."+NUM_COMPLETIONS+".pearson";
	    BufferedWriter out = new BufferedWriter(new FileWriter(logname));
	    out.write("r = "+twoPlaces.format(_stats.pearson(g)));
	    out.close();
	} catch (IOException ex) {
	} 
    }

    public static void output(long stage, Graph g) {
	try {
	    String logname;
	    if (stage<0) {
		logname = "output."+NUM_SEEDS+"."+NUM_RDS_TREES+"."+NUM_COMPLETIONS+"-final";
	    }
	    else {
		logname = "output."+NUM_SEEDS+"."+NUM_RDS_TREES+"."+NUM_COMPLETIONS+"-"+stage;
	    }

	    BufferedWriter out = new BufferedWriter(new FileWriter(logname));
	    for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
		Vertex vquery_g = (Vertex)vit.next();
		double grank = _stats.get_grank(vquery_g);
		double crank;
		double ctr =  _stats.get_count(vquery_g);
		try {
		    crank = _stats.get_crank(vquery_g);
		}
		catch (Exception ex2) {
		    // skip the vertex for which we have no data
		    continue;
		}
		double diff = Math.abs(crank-grank);
		out.write(""+
			  twoPlaces.format(diff)+"\t"+
			  twoPlaces.format(ctr)+"\t"+
			  twoPlaces.format(grank)+"\t"+
			  twoPlaces.format(crank)+"\t"+
			  vquery_g+"\n");
	    }
	    out.close();
	} catch (IOException ex) {
	} 
    }

    public static void debugOutput(Graph g) {
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    if (vquery_g.inDegree() != vquery_g.outDegree()) {
		System.out.print("***");
	    }
	    System.out.println(" "+vquery_g+" in="+vquery_g.inDegree()+" out="+vquery_g.outDegree());
	}
	System.exit(0);
    }

    public static Graph undirectify(Graph g) {
	// duplicate vertices
	return DirectionTransformer.toUndirected(g, false);
    }

    public static Vertex getBigCompSeed(Graph g) {

	Vertex bigcompSeed = null;
	WeakComponentClusterer wccg = new WeakComponentClusterer();
	ClusterSet csg = wccg.extract(g);

	int big = -1;
	for (Iterator itc=csg.iterator(); itc.hasNext();) {
	    Set clus = (Set)itc.next();
	    if (clus.size() > big) big = clus.size();
	}

	for (Iterator itc=csg.iterator(); itc.hasNext();) {
	    Set clus = (Set)itc.next();
	    if (clus.size()==big) {
		Object[] varray = clus.toArray();
		int r = (int)(Math.random() * varray.length);
		bigcompSeed = (Vertex)varray[r];
		if (DEBUG) System.out.println("seed "+bigcompSeed+" has cluster size "+big);
		break;
	    }
	}
	return bigcompSeed;
    }
};

