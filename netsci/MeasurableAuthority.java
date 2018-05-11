package netsci;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.util.*;
//import edu.uci.ics.jung.io.*;
//import java.io.*;

/**
 * A class representing measuring authority
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class MeasurableAuthority implements Measurable {

    private final HITS _ranker;
    private final List _rlist;

    MeasurableAuthority(Graph g) {
	HITS ranker = new HITS(g, true); // use authority for ranking
	_ranker = new HITS(g);
	_ranker.setRemoveRankScoresOnFinalize(false);
	_ranker.evaluate();
	_rlist = _ranker.getRankings();
    }
	
    public double readValue(Vertex vquery) {
	double value = -1.0;
	for (Iterator rlit=_rlist.iterator(); rlit.hasNext(); ) {
	    NodeRanking nr = (NodeRanking)rlit.next();
	    Vertex rv = nr.vertex;
	    if (rv==vquery) {
		value = nr.rankScore;
		break;
	    }
	}
	return value;
    }
};

