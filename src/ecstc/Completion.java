package ecstc;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.util.*;

/**
 * Completes an RDS tree by using degree information from the
 * underlying graph from which the RDS tree was made
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Completion {

    private static final boolean SMART=false;

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
    protected final LinkedList _tasksIn = new LinkedList();
    protected final LinkedList _tasksOut = new LinkedList();

    // final options for edges
    protected final LinkedList _options = new LinkedList();

    // tables to convert from RDS vertices to completion vertices and back
    protected final Map _lutr2c = new HashMap();
    protected final Map _lutc2r = new HashMap();

    private int _target=0;
    private int _defecit=0;
    private double _efficiency=0.0;
    private int _wastage=0;

    // iterator over completions
    static class CompletionIterator implements Iterator {
	protected RDS _rds;
	protected Graph _g;
	protected boolean _empty;
	
	CompletionIterator (RDS rds, Graph g, boolean empty) {
	    _rds = rds;
	    _g = g;
	    _empty = empty;
	}

	public boolean hasNext() {
	    return true;
	}

	public Object next() {
	    Completion c;
	    int wastage = -1;
	    do {
		c = new Completion(_rds, _g, _empty);
		wastage++;
		Log.diag(Completion.class.getName(), Log.DEBUG, "Done... "+
			 ", Efficiency = "+c.getEfficiency());
	    }
	    while (c.getEfficiency() < Params.EFFICIENCY_CUTOFF);
	    c._wastage = wastage;

	    return c;
	}

	public void remove() {
	    // no-op
	}
    }

    // static factory over RDS completions
    public static Iterator iterator(RDS rds, Graph g, boolean empty) {
	return new CompletionIterator(rds, g, empty);
    }

    protected Completion(RDS rds, Graph g, boolean empty) {

	// save args
	_rds = rds;
	_g = g;

	// the completion graph is a new graph
	_completion = new DirectedSparseGraph();

	// Add the vertices of the RDS to the completion graph
	Set vset = rds._rds.getVertices();
	for (Iterator it=vset.iterator();it.hasNext();) {
	    Vertex rv = (Vertex)it.next();
	    Vertex gv = rds.getGraphVertex(rv);
	    Vertex cv = new SimpleDirectedSparseVertex();
	    _completion.addVertex(cv);

	    // save a bidirectional mapping from RDS to completion vertices
	    _lutr2c.put(rv, cv);
	    _lutc2r.put(cv, rv);

	    // put the edge deficiencies in the task list
	    int rdeg = (empty ? 0 : rv.inDegree());
	    int gdeg = gv.inDegree();
	    if (rdeg < gdeg) {
		// System.out.println(""+gv+" has IN degree "+rdeg+" in the tree but "+gdeg+" in the graph");
		for (int n=0; n<(gdeg-rdeg); n++) {
		    _tasksIn.addLast(cv);
		}
	    }
	    rdeg = (empty ? 0 : rv.outDegree());
	    gdeg = gv.outDegree();
	    if (rdeg < gdeg) {
		// System.out.println(""+gv+" has OUT degree "+rdeg+" in the tree but "+gdeg+" in the graph");
		for (int n=0; n<(gdeg-rdeg); n++) {
		    _tasksOut.addLast(cv);
		}
	    }
	}

	if (!empty) {
	    // Add the edges of the RDS to the completion graph
	    Set eset = rds._rds.getEdges();
	    for (Iterator it=eset.iterator();it.hasNext();) {
		Edge e = (Edge)it.next();
		Vertex ru = (Vertex)e.getEndpoints().getFirst();
		Vertex cu = getCompletionVertex(ru);
		Vertex rv = (Vertex)e.getEndpoints().getSecond();
		Vertex cv = getCompletionVertex(rv);
		DirectedSparseEdge e2 = new DirectedSparseEdge(cu,cv);
		_completion.addEdge(e2);
	    }
	}

	// complete the graph
	buildCompletion(rds, g);
    }

    boolean terminal() {
	for (Iterator v1it = _tasksOut.iterator(); v1it.hasNext();) {
	    Vertex cv1 = (Vertex)v1it.next();
	    for (Iterator v2it = _tasksIn.iterator(); v2it.hasNext();) {
		Vertex cv2 = (Vertex)v2it.next();
		if (cv1 != cv2) return false;
	    }
	}
	return true;
    }

    protected void buildCompletion(RDS rds, Graph g) {
	// print the status before completion
	Log.diag(Completion.class.getName(), Log.DEBUG, "Before completion: "+this.toString());

	_target = _tasksOut.size()+_tasksIn.size();

	Vertex v1,v2;
	boolean term = false;

	Log.diag(Completion.class.getName(), Log.DEBUG, "Phase 1");
	int badrun=0;
	do {

	    do {
		int n = _tasksOut.size();
		v1 = null;
		if (n > 0) {
		    int r1 = (int)(Randomness.getDouble() * n);
		    v1 = (Vertex)_tasksOut.get(r1);
		}
		
		n = _tasksIn.size();
		v2 = null;
		if (n > 0) {
		    int r2 = (int)(Randomness.getDouble() * n);
		    v2 = (Vertex)_tasksIn.get(r2);
		}
		
		term = terminal();
	    }
	    while ((v1 == v2) && !term);
	    
	    // self loops  are disallowed
	    if (v1==null || v2==null || v1==v2) {
		// no go
		badrun++;
	    }
	    else {
		// parallel edges are disallowed
		if (v1.isPredecessorOf(v2) || v2.isPredecessorOf(v1)) {
		    // no go
		    badrun++;
		}
		else {
		    // neither self loop, nor parallel edge
		    badrun=0;
		    _tasksOut.remove(v1);
		    _tasksIn.remove(v2);
		    DirectedSparseEdge e = new DirectedSparseEdge(v1,v2);
		    _completion.addEdge(e);
		    Log.diag(Completion.class.getName(), Log.DEBUG, "Adding ("+
			     rds.getGraphVertex(getRDSVertex(v1))+", "+
			     rds.getGraphVertex(getRDSVertex(v2))+")");
		
		    if (Params.SYMMETRIC) {
			boolean succ = _tasksOut.remove(v2);
			succ = succ && _tasksIn.remove(v1);
			if (!succ) {
			    Log.diag(Completion.class.getName(), Log.WARN, "Couldn't remove symmetric edge ("+
				     rds.getGraphVertex(getRDSVertex(v2))+", "+
				     rds.getGraphVertex(getRDSVertex(v1))+")");
			}
			else {
			    DirectedSparseEdge erev = new DirectedSparseEdge(v2,v1);
			    _completion.addEdge(erev);
			}
		    }
		}
	    }

	    if (badrun > Params.MAX_BADRUN) {
		term = true;
	    }
	}
	while (!term);

	Log.diag(Completion.class.getName(), Log.DEBUG, "Post phase I we had "+
		 _tasksOut.size()+" out edges and "+
		 _tasksIn.size()+" in edges unassigned");

	// in the second pass we do exhaustive search

	Log.diag(Completion.class.getName(), Log.DEBUG, "Phase 2");

	buildAllOptions();
	boolean done = false;
	while (!done) {
	    done = chooseOneOption() || (_options.size() == 0);
	    buildAllOptions();
	}

	Log.diag(Completion.class.getName(), Log.DEBUG, "Post phase II we had "+
		 _tasksOut.size()+" out edges and "+
		 _tasksIn.size()+" in edges unassigned");

	_defecit = _tasksOut.size()+_tasksIn.size();
	_efficiency = (double)(_target-_defecit)/(double)_target;
    }

    double getEfficiency() {
	return _efficiency;
    }
    
    int getWastage() {
	return _wastage;
    }

    int getE() {
	return _completion.numEdges();
    }

    int getV() {
	return _completion.numVertices();
    }

    protected void buildAllOptions() {
	_options.clear();
	for (Iterator v1it = _tasksOut.iterator(); v1it.hasNext();) {
	    Vertex cv1 = (Vertex)v1it.next();
	    for (Iterator v2it = _tasksIn.iterator(); v2it.hasNext();) {
		Vertex cv2 = (Vertex)v2it.next();
		// duplicate edges and self loops are not an option
		if (cv1.isPredecessorOf(cv2) || (cv1==cv2)) {
		    continue;
		}
		else {
		    // add the option
		    DirectedSparseEdge e2 = new DirectedSparseEdge(cv1,cv2);
		    _options.addLast(e2);
		}
	    }
	}
	Log.diag(Completion.class.getName(), Log.DEBUG, "Options="+_options.size());
    }

    protected boolean chooseOneOption() {
	if (_options.size() > 0) {
	    int n = _options.size();
	    int r = (int)(Randomness.getDouble() * n);
	    DirectedSparseEdge e = (DirectedSparseEdge)_options.get(r);
	    _completion.addEdge(e);
	    _tasksOut.remove(e.getEndpoints().getFirst());
	    _tasksIn.remove(e.getEndpoints().getSecond());
	    if (Params.SYMMETRIC) {
		DirectedSparseEdge e2 = new DirectedSparseEdge((Vertex)e.getEndpoints().getSecond(),
							       (Vertex)e.getEndpoints().getFirst());
		_completion.addEdge(e2);
		_tasksOut.remove(e.getEndpoints().getSecond());
		_tasksIn.remove(e.getEndpoints().getFirst());
	    }
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
	int r = (int)(Randomness.getDouble() * varray.length);
	Vertex v = (Vertex)varray[r];
	return v;
    }

    public String toString() {
	int v = _completion.numVertices();
	int e = _completion.numEdges();

	WeakComponentClusterer wcc = new WeakComponentClusterer();
	ClusterSet cs = wcc.extract(this._completion);

	String s = "Completion "+
	    ": vertices="+v+
	    ",  edges="+e+
	    ", deficiency="+_tasksOut.size()+"->"+_tasksIn.size()+
	    ", components = "+cs.size();

	return s;
    }
};

