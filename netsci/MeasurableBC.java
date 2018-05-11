package netsci;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.util.*;
//import edu.uci.ics.jung.io.*;
//import java.io.*;

/**
 * A class representing measuring betweenness centrality
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class MeasurableBC implements Measurable {

    private final BetweennessCentrality _ranker;
    private final List _rlist;

    private final WeakComponentClusterer _clusterer = new WeakComponentClusterer();
    private final ClusterSet _clusters;

    MeasurableBC(Graph g) {

	_ranker = new BetweennessCentrality(g, true, false);
	_ranker.setRemoveRankScoresOnFinalize(false);
	_ranker.evaluate();
	_rlist = _ranker.getRankings();

	_clusters = _clusterer.extract(g);
	// System.out.println("Number of components "+_clusters.size());
    }
	
    public double readValue(Vertex vquery) {
	double value = -1.0;
	int i=0;
	boolean done = false;
	for (Iterator it=_clusters.iterator(); it.hasNext() && !done; ) {
	    // System.out.println("Component "+i);
	    HashSet onecomp = (HashSet)it.next();
	    if (!onecomp.contains(vquery)) continue;
	    // else we found the component which has the vertex of interest

	    for (Iterator it2=onecomp.iterator(); it2.hasNext() && !done; ) {
		Vertex vx = (Vertex)it2.next();
		if (vx!=vquery) continue;
		// else we found the vertex of interest

		// calculate the relative betweenness rank of vquery
		int rankcalc = 0;
		double score = -1.0;
		for (Iterator rlit=_rlist.iterator(); rlit.hasNext(); ) {
		    NodeRanking nr = (NodeRanking)rlit.next();
		    Vertex rv = nr.vertex;
		    score = nr.rankScore;
		    if (onecomp.contains(rv)) {
			rankcalc++;
		    }
		    if (rv==vx) break;
		}

		value = (double)rankcalc/(double)onecomp.size();
		done = true;
		// System.out.println("Vertex:"+vx+" relrank:"+rankcalc+"/"+onecomp.size()+" absrank="+score);
	    }
	    i++;
	}
	return value;
    }
};

