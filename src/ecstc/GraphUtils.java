package ecstc;

import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.util.*;

/**
 * Graph utilities class for ECSTC
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class GraphUtils {

    public static void symmetrizeAsDirectedGraph(Graph g) {
	HashSet revedges = new HashSet();

	for (Iterator it = g.getEdges().iterator(); it.hasNext();) {
	    DirectedSparseEdge e2 = (DirectedSparseEdge)it.next();
	    Vertex u1 = e2.getSource();
	    Vertex v1 = e2.getDest();
	    if (! v1.isPredecessorOf(u1)) {
		DirectedSparseEdge e2rev = new DirectedSparseEdge(v1,u1);
		revedges.add(e2rev);
	    }
	}

	for (Iterator it=revedges.iterator(); it.hasNext();) {
	    DirectedSparseEdge e2rev = (DirectedSparseEdge)it.next();
	    g.addEdge(e2rev);
	}
    }

    public static Vertex getRandomBigComponentVertex(Graph g) {

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
		int r = (int)(Randomness.getDouble() * varray.length);
		bigcompSeed = (Vertex)varray[r];
		Log.diag(GraphUtils.class.getName(), Log.DEBUG, "seed "+bigcompSeed+" has cluster size "+big);
		break;
	    }
	}
	return bigcompSeed;
    }

    static Vertex getRandomGraphVertex(Graph g) {
	Set vset = g.getVertices();
	Object[] varray = vset.toArray();
	int r = (int)(Randomness.getDouble() * varray.length);
	Vertex v = (Vertex)varray[r];
	return v;
    }

    static Vertex getRandomOutNeighborGraphVertex(Graph g, Vertex gv, EdgeWeightAssigner ews) {
	Set e2 = gv.getOutEdges();
	EdgeSelector es = new EdgeSelector(e2, ews);
	Edge e = es.getEdge();
	Vertex gu = e.getOpposite(gv);
	return gu;
    }

    static Vertex getRandomInNeighborGraphVertex(Graph g, Vertex gv, EdgeWeightAssigner ews) {
	Set e1 = gv.getInEdges();
	EdgeSelector es = new EdgeSelector(e1, ews);
	Edge e = es.getEdge();
	Vertex gu = e.getOpposite(gv);
	return gu;
    }

    static String getVertexLabel(Vertex v) {
	return (String)v.getUserDatum(PajekNetReader.LABEL);
    }
}
