package netsci;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import java.util.*;
//import edu.uci.ics.jung.io.*;
//import java.io.*;

/**
 * Build an RDS tree on a graph starting from a randomly chosen set of
 * seeds.  The RDS tree is biased in the following way: a person
 * always gives out their coupons to (previously undiscovered)
 * individuals with the highest degree.
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class RDSBiased extends RDS {

    static class RDSBiasedIterator extends RDS.RDSIterator {

	RDSBiasedIterator (Graph g, int seeds) {
	    super(g, seeds);
	}

	public Object next() {
	    return new RDSBiased(_g, _seeds);
	}

	public void remove() {
	    // no-op
	}
    }

    public static Iterator iterator(Graph g, int seeds) {
	return new RDSBiasedIterator(g, seeds);
    }

    private RDSBiased(Graph g, int seeds) {
	super(g, seeds);
    }

    // overrides the updateMaxDeg in base RDS class
    protected void updateMaxDeg(Graph g, Vertex gv) {

	// GOAL: _gv2maxdeg(gv) should equal to the minimum G-degree
	// over all RDS-nighbors of gv which were discovered after gv
	// (i.e. referrals by gv)

	// get all the G-neighbors of gv
	Set e1 = gv.getInEdges();
	Set e2 = gv.getOutEdges();
	Set eall = new HashSet();
	eall.addAll(e1);
	eall.addAll(e2);
	Object earray[] = eall.toArray();

	// time gv was discovered
	int gvtime = ((Integer)_gv2time.get(gv)).intValue();

	// we are doing a minimization
	int maxdeg = 999;
	
	// iterate over all G-neighbors of gv
	for (int i=0;i<earray.length; i++) {
	    Edge e = (Edge)earray[i];
	    Vertex gu = e.getOpposite(gv);
	    Vertex ru = getRDSVertex(gu);
	    // but consider only RDS-neighbors of gv
	    if (ru==null) continue;

	    // System.out.println("Considering "+gv+"-->"+gu+"  time="+_gv2time.get(gu));

	    // only consider children of gv (not the parent)
	    int gutime = ((Integer)_gv2time.get(gu)).intValue();
	    if (gutime < gvtime) continue;

	    // get the G-degree of the RDS-neighbor
	    int degu = gu.getInEdges().size() + gu.getOutEdges().size();

	    // keep track of the minimum G-degree seen
	    if (degu < maxdeg) {
		maxdeg = degu;
	    }
	}
	
	// System.out.println("Maxdeg "+gv+"-->"+maxdeg);
	// save the value
	_gv2maxdeg.put(gv, new Integer(maxdeg));
    }

    // overrides the getRandomNeighborGraphVertex in base RDS class
    protected Vertex getRandomNeighborGraphVertex(Graph g, Vertex gv) {
	Set e1 = gv.getInEdges();
	Set e2 = gv.getOutEdges();
	Set eall = new HashSet();
	eall.addAll(e1);
	eall.addAll(e2);
	Object earray[] = eall.toArray();

	// find the neighbor of gv that is both undiscovered and has
	// the highest degree
	int maxdeg = -1;
	Vertex answer = null;
	for (int i=0;i<earray.length; i++) {
	    Edge e = (Edge)earray[i];
	    Vertex gu = e.getOpposite(gv);
	    Vertex ru = getRDSVertex(gu);
	    // must be undiscovered
	    if (ru!=null) continue;

	    int degu = gu.getInEdges().size() + gu.getOutEdges().size();
	    if (degu > maxdeg) {
		// save the one with the highest degree
		maxdeg = degu;
		answer = gu;
	    }
	}
	if (answer==null) {
	    System.out.println("ERROR answer=null for vertex "+gv);
	    System.out.println("ERROR isDeadGraphVertex(g, gv) = "+isDeadGraphVertex(g, gv));
	    System.exit(0);
	}

	return answer;
    }

    public String toString() {
	int v = _rds.numVertices();
	int e = _rds.numEdges();
	String s = "RDSBiased vertices="+v+",  edges="+e;
	return s;
    }
};

