package ecstc;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import java.util.*;

/**
 * A class representing measuring betweenness centrality
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class MeasurementBC extends Measurement {

    static class MeasurementBC_Factory extends Measurement.Factory {
	private MeasurementBC_Factory() {
	    super.register();
	}

	public Measurement newMeasurement(Graph g, Stats s, VertexMapper vm) { 
	    return new MeasurementBC(g,s,vm); 
	}

	public String getName() { return "BC"; }
    }

    private static Measurement.Factory _fact = null;
    public static Measurement.Factory getFactory() { 
	if (_fact == null) {
	    _fact = new MeasurementBC_Factory();
	}
	return _fact; 
    }

    private BetweennessCentrality _ranker;

    private MeasurementBC(Graph g, Stats s, VertexMapper vm) {
	super(g,s, vm);
    }

    void initialize(Graph g) {
	_ranker = new BetweennessCentrality(g, true, false);
	_ranker.setRemoveRankScoresOnFinalize(false);
	_ranker.evaluate();     
    }

    double readValue(Vertex vquery) {     
	return _ranker.getRankScore(vquery);
    }

    String getName() {
	return getFactory().getName();
    }
};

