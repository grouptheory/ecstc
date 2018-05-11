package ecstc;

import edu.uci.ics.jung.graph.*;
import java.util.*;

/**
 * Class to select edges according to a specified distribution
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class EdgeSelector {

    private final TreeMap _accum2edge = new TreeMap();
    private final double _max;

    public EdgeSelector(Set edges, EdgeWeightAssigner a) {
	double accum=0.0;
	for (Iterator it=edges.iterator(); it.hasNext();) {
	    Edge e = (Edge)it.next();
	    double w = a.getWeight(e);
	    accum+=w;
	    _accum2edge.put(new Double(accum), e);
	}
	_max = accum;
    }

    public Edge getEdge() {
	SortedMap tail = (SortedMap)_accum2edge.tailMap( _max*Randomness.getDouble() );
	Edge e = (Edge)tail.get(tail.firstKey());
	return e;
    }
}
