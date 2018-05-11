package netsci;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.algorithms.metrics.*;
import java.util.*;
//import edu.uci.ics.jung.io.*;
//import java.io.*;

/**
 * A class representing measuring pagerank
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class MeasurableEffectiveSize implements Measurable {

    private final StructuralHoles _sh;
    private final EdgeWeightLabeller _ewl;

    MeasurableEffectiveSize(Graph g) {
	_ewl = EdgeWeightLabeller.getLabeller(g);
	for (Iterator eit=g.getEdges().iterator(); eit.hasNext(); ) {
	    Edge e = (Edge)eit.next();
	    _ewl.setWeight(e,1);
	}
	_sh = new StructuralHoles(_ewl);
    }
	
    public double readValue(Vertex vquery) {
	double value = _sh.effectiveSize(vquery);
	return value;
    }
};

