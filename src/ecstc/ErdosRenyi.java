package ecstc;

import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.io.*;
import java.util.*;
import edu.uci.ics.jung.graph.decorators.*;

/**
 * Class to generate random graphs
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class ErdosRenyi extends RandomGraph {

    static Graph make(int n, double p) {
	Graph g = new DirectedSparseGraph();
	makeVertices(n, g);
	
	for (int v=0;v<n;v++) {
	    for (int u=0;u<n;u++) {
		if (u==v) continue;
		if(Randomness.getDouble() <= p) {
		    Edge e = new DirectedSparseEdge((Vertex)vmap.get(u), (Vertex)vmap.get(v));
		    g.addEdge(e);
		}
	    }
	}

	return g;
    }

    public static void main(String [] args) {

	if (args.length!=4) {
	    Log.diag(ErdosRenyi.class.getName(), Log.FATAL, "Usage: ErdosRenyi n p <filename> <RNGseed>");
	}
	
	String NUM_VERTICESStr = args[0];
	String NUM_PStr = args[1];
	String fname = args[2];

	String NUM_RNGStr = args[3];  
        Randomness.RNGSEED = Integer.parseInt(NUM_RNGStr);

	int NUM_VERTICES = Integer.parseInt(NUM_VERTICESStr);
	double NUM_P = Double.parseDouble(NUM_PStr); 
	fname = fname +"-ER,v="+NUM_VERTICES+",p="+NUM_P;   

	Graph g = ErdosRenyi.make(NUM_VERTICES, NUM_P);
        sl = ToStringLabeller.setLabellerTo(g);

	try {
	    PajekNetWriter pnw = new PajekNetWriter();

	    pnw.save(g,fname,sl,null);
	    Log.diag(ErdosRenyi.class.getName(), Log.INFO, "Output: "+fname);
	}
	catch (IOException ex) {
	    Log.diag(ErdosRenyi.class.getName(), Log.ERROR, "Exception: "+ex);
	}
    }
};

