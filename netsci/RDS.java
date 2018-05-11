package netsci;

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

    // debugging
    protected static final boolean DEBUG = false;

    // RDS degree
    protected static final int REFERRALS = 3;

    // the RDS graph
    final Graph _rds;

    // A map from graph vertex to time of discovery
    HashMap _gv2time = new HashMap();

    // The current time
    int _time = 0;

    // A map from graph vertex to maximum permissable degree of 
    // neighbors in the completion (excluding RDS neighbors)
    HashMap _gv2maxdeg = new HashMap();

    // initial seeds
    final Set _seeds = new HashSet();

    // vertices of G that the RDS has discovered
    protected final Set _discovered = new HashSet();
    
    // the frontier of RDS
    protected final LinkedList _frontier = new LinkedList();

    // tables to convert from graph to RDS vertices and back
    protected final Map _lutg2r = new HashMap();
    protected final Map _lutr2g = new HashMap();


    // An iterator over RDS trees
    static class RDSIterator implements Iterator {

	protected Graph _g;
	protected int _seeds;
	
	RDSIterator (Graph g, int seeds) {
	    _g = g;
	    _seeds = seeds;
	}

	public boolean hasNext() {
	    return true;
	}

	public Object next() {
	    return new RDS(_g, _seeds);
	}

	public void remove() {
	    // no-op
	}
    }

    // static factor method to make an iterator over RDS trees
    public static Iterator iterator(Graph g, int seeds) {
	return new RDSIterator(g, seeds);
    }

    private boolean _virgin = true;

    protected Vertex getNewSeed(Graph g) {
	Vertex gv, rv;

	if (_virgin) {
	    // first seed always lies in the big component
	    gv = Test.getBigCompSeed(g);
	    _virgin = false;
	}
	else {
	    // get a new previously unused seed
	    do {
		gv = getRandomGraphVertex(g);
		// see if its already been used as a seed
		rv = getRDSVertex(gv);
	    }
	    while (rv != null);
	}
	return gv;
    }

    protected void initializeSeeds(Graph g, int seeds) {
	// initialize the seeds
	for (int i=0;i<seeds;i++) {
	    // get a new seed
	    Vertex gv = getNewSeed(g);

	    // make the new RDS vertex
	    Vertex rv = new SimpleUndirectedSparseVertex();

	    // save a bidirectional mapping between graph and RDS vertices
	    _lutg2r.put(gv, rv);
	    _lutr2g.put(rv, gv);

	    // add the seed to the RDS tree
	    _seeds.add(rv);
	    _rds.addVertex(rv);
	    if (DEBUG) System.out.println("Added seed: "+gv);

	    // note the discovery time = 0
	    _gv2time.put(gv, new Integer(0));

	    // initially the frontier consists of just seeds
	    _frontier.addLast(rv);

	    // seeds are considered already discovered
	    _discovered.add(rv);

	    // update maxDegree
	    updateMaxDeg(g, gv);
	}
    }

    protected Vertex getNextFrontierVertex(Graph g) {
	Vertex rv = null;
	Vertex gv = null;
	boolean found = false;
	while (_frontier.size() > 0) {
	    rv = (Vertex)_frontier.removeFirst();
	    gv = getGraphVertex(rv);
	    if (DEBUG) System.out.println("Growing from = "+gv);
	    if (isDeadGraphVertex(g, gv)) {
		// the frontier vertex is dead, no need to reschedule it...
		if (DEBUG) System.out.println("    All ("+gv.inDegree()+") neighbors of "+gv+" in G have been discovered, it's dead");
		continue;
	    }
	    else { // we found a non-dead frontier vertex!
		if (isSaturatedRDSVertex(rv)) {
		    // if the frontier vertex is rds saturated, continue
		    if (DEBUG) System.out.println("    Done growing from "+rv+" All ("+rv.inDegree()+") RDS-neighbors have been selected");
		    continue;
		}
		else {
		    // not dead, not saturated
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
		System.out.println("FRONTIER ERROR Dead vertex "+gv+" has no neighbors!");
		System.exit(0);
	    }
	    if (isSaturatedRDSVertex(rv)) {
		System.out.println("FRONTIER ERROR Saturated vertex "+rv+" needs no neighbors!");
		System.exit(0);
	    }
	    return rv;
	}
    }

    protected void printFrontier() {
	System.out.print("Frontier = [");
	for (Iterator it2=_frontier.iterator();it2.hasNext();) {
	    Vertex fv=(Vertex)it2.next();
	    System.out.print(""+fv+",");
	}
	System.out.println("]");
    }

    protected void grow(Graph g, Vertex rv) {
	// get the graph vertex corresponding to rv
	Vertex gv = getGraphVertex(rv);
	// find a random yet undiscovered neighbor
	Vertex gu = null;
	Vertex ru = null;
	do {
	    gu = getRandomNeighborGraphVertex(g,gv);
	    ru = getRDSVertex(gu);
	    // System.out.println("Considering neighbor = "+ru);
	}
	while ( ru != null );

	// add the discovered vertex
	ru = new SimpleUndirectedSparseVertex();

	// save a bidirectional mapping
	_lutg2r.put(gu, ru);
	_lutr2g.put(ru, gu);

	// add ru to the RDS tree
	_rds.addVertex(ru);

	// note the discovery time
	_time++;
	_gv2time.put(gu, new Integer(_time));
	if (DEBUG) System.out.println("Added vertex: "+gu);

	// augment the frontier and discovered sets
	_discovered.add(ru);
	_frontier.add(ru);

	// add the edge
	UndirectedSparseEdge e = new UndirectedSparseEdge(rv,ru);
	_rds.addEdge(e);

	// update the max degree information 
	updateMaxDeg(g, gv);
	updateMaxDeg(g, gu);

	if (DEBUG) System.out.println("Added edge: "+rv+"-->"+ru);
    }

    protected RDS(Graph g, int seeds) {
	// the time is zero
	_time = 0;

	// the RDS tree is a new graph
	_rds = new UndirectedSparseGraph();

	// add the seeds to the RDS tree, initializing the frontier
	initializeSeeds(g, seeds);

	// Add the vertices of g to the RDS tree
	Vertex rv;
	while ((rv = getNextFrontierVertex(g)) != null) {
	    grow(g, rv);
	}
    }

    Vertex getRDSVertex(Vertex gv) {
	return (Vertex)_lutg2r.get(gv);
    }

    Vertex getGraphVertex(Vertex rv) {
	return (Vertex)_lutr2g.get(rv);
    }

    static Vertex getRandomGraphVertex(Graph g) {
	Set vset = g.getVertices();
	Object[] varray = vset.toArray();
	int r = (int)(Math.random() * varray.length);
	Vertex v = (Vertex)varray[r];
	return v;
    }

    protected boolean isSaturatedRDSVertex(Vertex rv) {
	if (_seeds.contains(rv)) {
	    if (rv.inDegree()==REFERRALS) return true;
	    else return false;
	}
	else  {
	    if (rv.inDegree()==REFERRALS+1) return true;
	    else return false;
	}
    }

    protected boolean isDeadGraphVertex(Graph g, Vertex gv) {
	Set e1 = gv.getInEdges();
	Set e2 = gv.getOutEdges();
	Set eall = new HashSet();
	eall.addAll(e1);
	eall.addAll(e2);

	Object earray[] = eall.toArray();
	if (DEBUG) System.out.println("Num neighbors="+earray.length);

	for (int i=0; i<earray.length; i++) {
	    Edge e = (Edge)earray[i];
	    Vertex gu = e.getOpposite(gv);
	    Vertex ru = getRDSVertex(gu);

	    // we found an undiscovered neighbor, 
	    // so this vertex isn't dead
	    if (ru==null) {
		// System.out.println("Neighbor="+ru);
		return false;
	    }
	}
	// all neighbors are already discovered
	// so this vertex is dead
	return true;
    }

    protected void updateMaxDeg(Graph g, Vertex gv) {
	// no op
    }

    protected Vertex getRandomNeighborGraphVertex(Graph g, Vertex gv) {

	if (isDeadGraphVertex(g,gv)) {
	    System.out.println("ERROR Dead vertex has no neighbors!");
	    System.exit(0);
	}

	Set e1 = gv.getInEdges();
	Set e2 = gv.getOutEdges();
	Set eall = new HashSet();
	eall.addAll(e1);
	eall.addAll(e2);
	Object earray[] = eall.toArray();
	int r = (int)(Math.random() * earray.length);
	Edge e = (Edge)earray[r];
	Vertex gu = e.getOpposite(gv);
	return gu;
    }

    public String toString() {
	int v = _rds.numVertices();
	int e = _rds.numEdges();
	String s = "RDS vertices="+v+",  edges="+e;
	return s;
    }
};

