package netsci;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import java.util.*;
//import edu.uci.ics.jung.io.*;
//import java.io.*;

/**
 * Completes an RDS tree by using degree information from the
 * underlying graph from which the RDS tree was made
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Completion {

    // debugging
    protected static final boolean DEBUG = false;

    // probability of connecting outside of the discovered graph
    protected static final double CLOSURE_PROB = 1.0;
    // maximum number of attempts to find an addable edge
    protected static final int MAXTRIES = 10;

    // the graph
    final Graph _g;

    // the rds
    final RDS _rds;

    // the completion of RDS;
    final Graph _completion;

    // deficiencies in vertex degree
    protected final LinkedList _tasks = new LinkedList();

    // final options for edges
    protected final LinkedList _options = new LinkedList();

    // tables to convert from RDS vertices to completion vertices and back
    protected final Map _lutr2c = new HashMap();
    protected final Map _lutc2r = new HashMap();

    // iterator over completions
    static class CompletionIterator implements Iterator {
	protected RDS _rds;
	protected Graph _g;
	
	CompletionIterator (RDS rds, Graph g) {
	    _rds = rds;
	    _g = g;
	}

	public boolean hasNext() {
	    return true;
	}

	public Object next() {
	    return new Completion(_rds, _g);
	}

	public void remove() {
	    // no-op
	}
    }

    // static factory over RDS completions
    public static Iterator iterator(RDS rds, Graph g) {
	return new CompletionIterator(rds, g);
    }

    protected Completion(RDS rds, Graph g) {

	// save args
	_rds = rds;
	_g = g;

	// the completion graph is a new graph
	_completion = new UndirectedSparseGraph();

	// Add the vertices of the RDS to the completion graph
	Set vset = rds._rds.getVertices();
	for (Iterator it=vset.iterator();it.hasNext();) {
	    Vertex rv = (Vertex)it.next();
	    Vertex gv = rds.getGraphVertex(rv);
	    Vertex cv = new SimpleUndirectedSparseVertex();
	    _completion.addVertex(cv);

	    // save a bidirectional mapping from RDS to completion vertices
	    _lutr2c.put(rv, cv);
	    _lutc2r.put(cv, rv);

	    // put the edge deficiencies in the task list
	    int rdeg = rv.inDegree();
	    int gdeg = gv.inDegree();
	    // if (rds._seeds.contains(rv)) System.out.print("* ");
	    // System.out.println("rdeg="+rdeg+", gdeg="+gdeg);
	    if (rdeg < gdeg) {
		for (int n=0; n<(gdeg-rdeg); n++) {
		    _tasks.addLast(cv);
		}
	    }
	}

	// Add the edges of the RDS to the completion graph
	Set eset = rds._rds.getEdges();
	for (Iterator it=eset.iterator();it.hasNext();) {
	    Edge e = (Edge)it.next();
	    Vertex ru = (Vertex)e.getEndpoints().getFirst();
	    Vertex cu = getCompletionVertex(ru);
	    Vertex rv = (Vertex)e.getEndpoints().getSecond();
	    Vertex cv = getCompletionVertex(rv);
	    UndirectedSparseEdge e2 = new UndirectedSparseEdge(cu,cv);
	    _completion.addEdge(e2);
	}

	// complete the graph
	buildCompletion(rds, g);
    }

    protected void buildCompletion(RDS rds, Graph g) {
	// print the status before completion
	if (DEBUG) System.out.println(""+this.toString());

	int tries = 0;
	while ((_tasks.size() > 1) && (tries <= MAXTRIES)) {
	    boolean found = false;
	    Vertex v1,v2;
	    
	    tries = 0;
	    do {
		int n = _tasks.size();
		int r1 = (int)(Math.random() * n);
		v1 = (Vertex)_tasks.remove(r1);
		n = _tasks.size();
		int r2 = (int)(Math.random() * n);
		v2 = (Vertex)_tasks.remove(r2);

		if (DEBUG) System.out.println("contemplating edge ("+v1+", "+v2+")");

		// no duplicate edges or self loops
		if (v1.isPredecessorOf(v2) || v2.isPredecessorOf(v1) || (v1==v2)) {
		    _tasks.addLast(v1);
		    _tasks.addLast(v2);
		}
		else {
		    found = true;
		}

		tries++;
		if (tries > MAXTRIES) {
		    // we give up
		    found = true;
		}
	    }
	    while (!found);

	    if (tries > MAXTRIES) {
		// we gave up
		if (DEBUG) System.out.println("Unable to add any more edges after "+MAXTRIES+" attempts");
	    }
	    else {
		if (DEBUG) System.out.println("adding ("+v1+", "+v2+")");
		UndirectedSparseEdge e = new UndirectedSparseEdge(v1,v2);
		_completion.addEdge(e);
	    }
	}

	// in the second pass we do exhaustive search

	buildAllOptions();
	boolean done = false;
	while (!done) {
	    done = chooseOneOption() || (_options.size() == 0);
	    buildAllOptions();
	}
    }

    protected void buildAllOptions() {
	_options.clear();
	for (Iterator v1it = _tasks.iterator(); v1it.hasNext();) {
	    Vertex cv1 = (Vertex)v1it.next();
	    for (Iterator v2it = _tasks.iterator(); v2it.hasNext();) {
		Vertex cv2 = (Vertex)v2it.next();
		// duplicate edges and self loops are not an option
		if (cv1.isPredecessorOf(cv2) || cv2.isPredecessorOf(cv1) || (cv1==cv2)) {
		    continue;
		}
		else {
		    // add the option
		    UndirectedSparseEdge e2 = new UndirectedSparseEdge(cv1,cv2);
		    _options.addLast(e2);
		}
	    }
	}
	if (DEBUG) System.out.println("Options="+_options.size());
    }

    protected boolean chooseOneOption() {
	if (_options.size() > 0) {
	    int n = _options.size();
	    int r = (int)(Math.random() * n);
	    UndirectedSparseEdge e = (UndirectedSparseEdge)_options.get(r);
	    _completion.addEdge(e);
	    _tasks.remove(e.getEndpoints().getFirst());
	    _tasks.remove(e.getEndpoints().getSecond());
	    return false;
	}
	else return true;
    }

    Vertex getCompletionVertex(Vertex rv) {
	return (Vertex)_lutr2c.get(rv);
    }

    protected Vertex getRDSVertex(Vertex cv) {
	return (Vertex)_lutc2r.get(cv);
    }

    static Vertex getRandomRDSVertex(RDS rds) {
	Set vset = rds._rds.getVertices();
	Object[] varray = vset.toArray();
	int r = (int)(Math.random() * varray.length);
	Vertex v = (Vertex)varray[r];
	return v;
    }

    public String toString() {
	int v = _completion.numVertices();
	int e = _completion.numEdges();
	String s = "Completion vertices="+v+",  edges="+e+", deficiency="+_tasks.size()+"";
	/*
	s+="deficient nodes = [";
	for (Iterator it2=_tasks.iterator();it2.hasNext();) {
	    Vertex fv=(Vertex)it2.next();
	    s+=(""+fv+",");
	}
	s+=("]");
	*/
	return s;
    }
};

