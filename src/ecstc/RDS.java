package ecstc;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.utils.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import java.util.*;
//import edu.uci.ics.jung.io.*;
//import java.io.*;

/**
 * Build an RDS tree on a graph starting from a randomly chosen set of seeds.
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class RDS {

    // RDS degree
    protected static final int REFERRALS = 3;

    // the RDS graph
    final Graph _rds;

    // A map from graph vertex to time of discovery
    HashMap _gv2time = new HashMap();

    // The current time
    int _time = 0;

    // initial seeds
    final Set _seeds = new HashSet();

    // edge weight assigner
    final EdgeWeightAssigner _ews;

    // vertices of G that the RDS has discovered
    protected final Set _discovered = new HashSet();
    
    // the frontier of RDS
    protected final LinkedList _frontier = new LinkedList();

    // the growth sequence of RDS
    protected final LinkedList _growth = new LinkedList();

    // tables to convert from graph to RDS vertices and back
    protected final Map _lutg2r = new HashMap();
    protected final Map _lutr2g = new HashMap();


    // An iterator over RDS trees
    static class RDSIterator implements Iterator {

	protected Graph _g;
	protected int _seeds;
	protected EdgeWeightAssigner _ews;

	RDSIterator (Graph g, int seeds, EdgeWeightAssigner ews) {
	    _g = g;
	    _seeds = seeds;
	    _ews = ews;
	}

	public boolean hasNext() {
	    return true;
	}

	public Object next() {
	    return new RDS(_g, _seeds, _ews);
	}

	public void remove() {
	    // no-op
	}
    }

    // static factory method to make an iterator over RDS trees
    public static Iterator iterator(Graph g, int seeds, EdgeWeightAssigner ews) {
	return new RDSIterator(g, seeds, ews);
    }

    protected RDS(Graph g, int seeds, EdgeWeightAssigner ews) {
	// the time is zero
	_time = 0;

	// the RDS tree is a new graph
	_rds = new DirectedSparseGraph();

	// save the edge weight assigner
	_ews = ews;

	// add the seeds to the RDS tree, initializing the frontier
	initializeSeeds(g, seeds);

	// Add the vertices of g to the RDS tree
	Vertex rv;
	while ((rv = getNextFrontierVertex(g)) != null) {
	    grow(g, rv);
	}
    }

    // the first seed?
    private boolean _virgin = true;

    protected void initializeSeeds(Graph g, int seeds) {

	// initialize the seeds
	for (int i=0;i<seeds;i++) {

	    // get a new seed
	    Vertex gv = getNewSeed(g);

	    // make the new RDS vertex
	    Vertex rv = new SimpleDirectedSparseVertex();

	    // copy over the label
	    rv.setUserDatum(PajekNetReader.LABEL, 
			    gv.getUserDatum(PajekNetReader.LABEL), 
			    UserData.SHARED);

	    // save a bidirectional mapping between graph and RDS vertices
	    _lutg2r.put(gv, rv);
	    _lutr2g.put(rv, gv);

	    // add the seed to the RDS tree and the set of seeds
	    _seeds.add(rv);
	    _rds.addVertex(rv);
	    Log.diag(RDS.class.getName(), Log.DEBUG, "Added seed: "+gv);

	    // note the discovery time = 0
	    _gv2time.put(gv, new Integer(0));

	    // initially the frontier consists of just seeds
	    _frontier.addLast(rv);

	    // seeds are considered already discovered
	    _discovered.add(rv);
	}
    }

    protected Vertex getNewSeed(Graph g) {
	Vertex gv, rv;

	if (_virgin) {
	    // first seed always lies in the big component
	    gv = GraphUtils.getRandomBigComponentVertex(g);
	    _virgin = false;
	}
	else {
	    // get a new previously unused seed
	    do {
		gv = GraphUtils.getRandomGraphVertex(g);
		// see if its already been used as a seed
		rv = getRDSVertex(gv);
	    }
	    while (rv != null);
	}
	return gv;
    }

    private Vertex getRandomFrontierVertex() {
	Object[] varray = _frontier.toArray();
	int r = (int)(Randomness.getDouble() * varray.length);
	Vertex v = (Vertex)varray[r];
	return v;
    }

    private Vertex getFirstFrontierVertex() {
	Vertex v = (Vertex)_frontier.getFirst();
	return v;
    }

    protected Vertex getNextFrontierVertex(Graph g) {

	Vertex rv = null;

	Vertex gv = null;
	boolean found = false;

	while (_frontier.size() > 0) {

	    rv = getRandomFrontierVertex();

	    _frontier.remove(rv);
	    gv = getGraphVertex(rv);

	    if (isDeadGraphVertex(g, gv)) {
		// the frontier vertex is dead, no need to reschedule it...
		Log.diag(RDS.class.getName(), Log.DEBUG, "    Unable to grow from "+gv+", Graph-dead");
		continue;
	    }
	    else { // we found a non-dead frontier vertex!
		if (isSaturatedRDSVertex(rv)) {
		    // if the frontier vertex is rds saturated, continue
		    Log.diag(RDS.class.getName(), Log.DEBUG, "    Done growing "+gv+", RDS-saturated");
		    continue;
		}
		else {
		    // not dead, not saturated, put it back for reconsideration
		    _frontier.addLast(rv);
		    found = true;
		    break;
		}
	    }
	}

	if (found==false) {
	    return null;
	}
	else {
	    if (isDeadGraphVertex(g,gv)) {
		Log.diag(RDS.class.getName(), Log.FATAL, "Graph-dead vertex "+gv+" has no neighbors!");
	    }
	    if (isSaturatedRDSVertex(rv)) {
		Log.diag(RDS.class.getName(), Log.FATAL, "RDS-saturated vertex "+gv+" needs no neighbors!");
	    }
	    return rv;
	}
    }

    protected void grow(Graph g, Vertex rv) {

	// get the graph vertex corresponding to rv
	Vertex gv = getGraphVertex(rv);
	// find a random yet undiscovered neighbor
	Vertex gu = null;
	Vertex ru = null;
	do {
	    gu = GraphUtils.getRandomOutNeighborGraphVertex(g,gv, _ews);
	    ru = getRDSVertex(gu);
	}
	while ( ru != null );

	// add the discovered vertex
	ru = new SimpleDirectedSparseVertex();

	// copy over the label
	ru.setUserDatum(PajekNetReader.LABEL, 
			GraphUtils.getVertexLabel(gu), 
			UserData.SHARED);

	// save a bidirectional mapping
	_lutg2r.put(gu, ru);
	_lutr2g.put(ru, gu);

	// add ru to the RDS tree
	_rds.addVertex(ru);

	// note the discovery time
	_time++;
	_gv2time.put(gu, new Integer(_time));
	Log.diag(RDS.class.getName(), Log.DEBUG, "Growing "+gv+" -> "+gu);

	// augment the frontier and discovered sets
	_discovered.add(ru);
	_frontier.add(ru);

	// add the edge
	DirectedSparseEdge e = new DirectedSparseEdge(rv,ru);
	_rds.addEdge(e);
	_growth.addLast(e);
	if (Params.SYMMETRIC) {
	    DirectedSparseEdge e2 = new DirectedSparseEdge(ru,rv);
	    _rds.addEdge(e2);
	}
    }

    Vertex getRDSVertex(Vertex gv) {
	return (Vertex)_lutg2r.get(gv);
    }

    Vertex getGraphVertex(Vertex rv) {
	return (Vertex)_lutr2g.get(rv);
    }

    int getE() {
	return _rds.numEdges();
    }

    int getV() {
	return _rds.numVertices();
    }

    protected boolean isSaturatedRDSVertex(Vertex rv) {
	if (rv.outDegree()==REFERRALS) return true;
	else return false;
    }

    protected boolean isDeadGraphVertex(Graph g, Vertex gv) {
	Set e2 = gv.getOutEdges();
	Object earray[] = e2.toArray();

	for (int i=0; i<earray.length; i++) {
	    Edge e = (Edge)earray[i];
	    Vertex gu = e.getOpposite(gv);
	    Vertex ru = getRDSVertex(gu);

	    // we found an undiscovered neighbor, 
	    // so this vertex isn't dead
	    if (ru==null) {
		return false;
	    }
	}
	// all neighbors are already discovered
	// so this vertex is dead
	return true;
    }

    protected void printFrontier() {
	String s = "";
	s+=("Frontier = [");
	for (Iterator it2=_frontier.iterator();it2.hasNext();) {
	    Vertex rv=(Vertex)it2.next();
	    Vertex gv = getGraphVertex(rv);
	    s += (""+gv+",");
	}
	s+=("]");
	Log.diag(RDS.class.getName(), Log.INFO, s);
    }

    public String toString() {
	int v = _rds.numVertices();
	int e = _rds.numEdges();
	String s = "RDS vertices="+v+",  edges="+e+" :";
	for (Iterator it = _growth.iterator(); it.hasNext();) {
	    DirectedSparseEdge edge = (DirectedSparseEdge)it.next();
	    Vertex src = edge.getSource();
	    String srcStr = GraphUtils.getVertexLabel(src);
	    Vertex dest = edge.getDest();
	    String destStr = GraphUtils.getVertexLabel(dest);
	    s += ("("+srcStr+","+destStr+") ");
	}
	return s;
    }
};

