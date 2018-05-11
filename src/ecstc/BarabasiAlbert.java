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
public class BarabasiAlbert extends RandomGraph {

    static int k0 = 2;     // initial ER degree
    static int m = 5;      // number of edges at attachment
    static double a = 1.0; // offset to degree

    static Graph make(int n) {
	Graph g = new DirectedSparseGraph();
	makeVertices(k0, g);	

	double total = 0;
	for (int u=0;u<k0;u++) {
	    Vertex vert_u = (Vertex)vmap.get(u);
	    for (int v=u+1;v<k0;v++) {
		Vertex vert_v = (Vertex)vmap.get(v);
		if (u==v) continue;
		Edge e = new DirectedSparseEdge(vert_u, vert_v);

		//System.out.println("Adding "+vert_u+" --> "+vert_v+"");
		g.addEdge(e);
		total+=0.5;
	    }
	}
	
	TreeMap cumuldist = new TreeMap();

	for (int u=k0;u<n;u++) {
	    Vertex vert_u = addVertex();
	    g.addVertex(vert_u);

	    cumuldist.clear();
	    double accum = 0;
	    for (int v=0;v<u;v++) {
		Vertex vert_v = (Vertex)vmap.get(v);
		int d = vert_v.inDegree() + vert_v.outDegree();
		accum += d;
		accum += a;
		cumuldist.put(new Double(accum), vert_v);
	    }

	    for (int i=0;i<m;i++) {	    
		Vertex chosen = vert_u;
		Set nu = vert_u.getSuccessors();
		do {
		    double toss = accum * Randomness.getDouble();
		    SortedMap tail = cumuldist.tailMap(new Double(toss));
		    chosen = (Vertex)tail.get(tail.firstKey());
		}
		while ((chosen==vert_u) || (nu.contains(chosen)));

		Edge e = new DirectedSparseEdge(vert_u, chosen);
		//System.out.println("Adding "+vert_u+" --> "+chosen+"");
		g.addEdge(e);
	    }
	}

	return g;
    }

    public static void main(String [] args) {

	if (args.length!=6) {
	    Log.diag(BarabasiAlbert.class.getName(), Log.FATAL, "Usage: BarabasiAlbert n k0 m a <filename> <RNGseed>");
	}
	
	String NUM_VERTICESStr = args[0];

	String NUM_k0Str = args[1];  
        k0 = Integer.parseInt(NUM_k0Str);

	String NUM_mStr = args[2];  
        m = Integer.parseInt(NUM_mStr);

	// the first dynamically added vertex will need m vertices to
	// attach to, hence we must have k0 >= m.
	if (k0 < m) k0 = m;

	String NUM_aStr = args[3];  
        a = Double.parseDouble(NUM_aStr);

	String fname = args[4];

	String NUM_RNGStr = args[5];  
        Randomness.RNGSEED = Integer.parseInt(NUM_RNGStr);

	int NUM_VERTICES = Integer.parseInt(NUM_VERTICESStr);
	fname = fname +"-BA,v="+NUM_VERTICES+",k0="+k0+",m="+m+",a="+a;

	Graph g = BarabasiAlbert.make(NUM_VERTICES);
        sl = ToStringLabeller.setLabellerTo(g);
        
	try {
	    PajekNetWriter pnw = new PajekNetWriter();

	    pnw.save(g,fname,sl,null);
	    Log.diag(BarabasiAlbert.class.getName(), Log.INFO, "Output: "+fname);
	}
	catch (IOException ex) {
	    Log.diag(BarabasiAlbert.class.getName(), Log.ERROR, "Exception: "+ex);
	}
    }

};

