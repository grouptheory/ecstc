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
public class CompletionBiased extends Completion {

    private static int _phase = 1;

    static class CompletionBiasedIterator extends Completion.CompletionIterator {
	
	CompletionBiasedIterator (RDS rds, Graph g) {
	    super(rds, g);
	}

	public Object next() {
	    return new CompletionBiased(_rds, _g);
	}
    }

    public static Iterator iterator(RDS rds, Graph g) {
	return new CompletionBiasedIterator(rds, g);
    }

    protected CompletionBiased(RDS rds, Graph g) {
	super(rds, g);
    }

    protected void buildCompletion(RDS rds, Graph g) {
	// print the status before completion
	if (DEBUG) System.out.println(""+this.toString());

	// phase 1
	_phase=1;

	buildAllOptions();
	boolean done = false;
	while (!done) {
	    done = chooseOneOption() || (_options.size() == 0);
	    buildAllOptions();
	}

	// phase 2
	_phase=2;

	buildAllOptions();
	done = false;
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
		// check for duplicate edges and self loops
		if (cv1.isPredecessorOf(cv2) || cv2.isPredecessorOf(cv1) || (cv1==cv2)) {
		    continue;
		}
		// check that adding the edge doesn't imply that v1 or
		// v2 did not follow directions at RDS tree build time
		else if ((_phase==1) && (degreeViolation(cv1,cv2))) {
		    continue;
		}
		// everything is fine... in phase 2 degree violations are ok
		else {
		    // add the option
		    UndirectedSparseEdge e2 = new UndirectedSparseEdge(cv1,cv2);
		    _options.addLast(e2);
		}
	    }
	}
	if (DEBUG) System.out.println("Options="+_options.size());
    }

    private boolean degreeViolation(Vertex cv1, Vertex cv2) {
	Vertex rv1 = getRDSVertex(cv1);
	Vertex gv1 = _rds.getGraphVertex(rv1);
	int d1target = gv1.inDegree();
	int time1 = ((Integer)_rds._gv2time.get(gv1)).intValue();

	Vertex rv2 = getRDSVertex(cv2);
	Vertex gv2 = _rds.getGraphVertex(rv2);
	int d2target = gv2.inDegree();
	int time2 = ((Integer)_rds._gv2time.get(gv2)).intValue();

	if (_rds._gv2maxdeg.get(gv1)==null) {
	    System.out.println("maxdeg gv1 "+gv1+" is null");
	}
	if (_rds._gv2maxdeg.get(gv2)==null) {
	    System.out.println("maxdeg gv2 "+gv2+" is null");
	}

	int maxdeg1 = ((Integer)_rds._gv2maxdeg.get(gv1)).intValue();
	int maxdeg2 = ((Integer)_rds._gv2maxdeg.get(gv2)).intValue();

	// v1 was undiscovered when v2 was discovered and v1's G
	// degree > the smallest G-degree of the RDS referrals that v2
	// made.  Ergo, v2 lied and did not give its coupons to the
	// nodes with highest degree.  This makes the proposed edge
	// illegal since we are presuming all nodes cooperated with
	// the instructions.
	if ((d1target > maxdeg2) && (time1 > time2)) {
	    //System.out.print("Violation: ");
	    //if (d1target > maxdeg2) System.out.print("d1target("+d1target+") > maxdeg2("+maxdeg2+")");
	    //if (time1 > time2) System.out.print("time1("+time1+") > time2("+time2+")");
	    //System.out.println("");
	    return true;
	}

	// v2 was undiscovered when v1 was discovered and v2's G
	// degree > the smallest G-degree of the RDS referrals that v1
	// made.  Ergo, v1 lied and did not give its coupons to the
	// nodes with highest degree.  This makes the proposed edge
	// illegal since we are presuming all nodes cooperated with
	// the instructions.
	if ((d2target > maxdeg1) && (time2 > time1)) {
	    //System.out.print("Violation: ");
	    //if (d2target > maxdeg1) System.out.print("d2target("+d2target+") > maxdeg1("+maxdeg1+")");
	    //if (time2 > time1) System.out.print("time2("+time2+") > time1("+time1+")");
	    //System.out.println("");
	    return true;
	}

	// everything is fine
	return false;
    }

    public String toString() {
	int v = _completion.numVertices();
	int e = _completion.numEdges();
	String s = "CompletionBiased vertices="+v+",  edges="+e+", deficiency="+_tasks.size()+"";
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

