package ecstc;

import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import edu.uci.ics.jung.utils.*;
import java.io.*;
import java.util.*;
import edu.uci.ics.jung.graph.decorators.*;

/**
 * Class to generate random graphs
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public abstract class RandomGraph {

    protected static final HashMap vmap = new HashMap();
    protected static StringLabeller sl = null;
    private static int nexti = 0;

    protected static Vertex addVertex() {
	Vertex v = new SimpleDirectedSparseVertex();
	try {
	    sl.setLabel(v,""+nexti);
	}
	catch (Exception ex) {
	}
	vmap.put(new Integer(nexti), v);
	nexti++;
	return v;
    }

    protected static void makeVertices(int n, Graph g) {
	vmap.clear();
	for (int i=0;i<n;i++) {
	    Vertex v = addVertex();
	    g.addVertex(v);
	}
    }
};

