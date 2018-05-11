package ecstc;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import edu.uci.ics.jung.algorithms.shortestpath.*;
import java.util.*;

/**
 * A class representing measuring betweenness centrality
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class MeasurementDijkstra extends Measurement {

    static class MeasurementDijkstra_Factory extends Measurement.Factory {
	private MeasurementDijkstra_Factory() {
	    super.register();
	}

	public Measurement newMeasurement(Graph g, Stats s, VertexMapper vm) { 
	    return new MeasurementDijkstra(g,s,vm); 
	}

	public String getName() { return "DIJKSTRA"; }
    }

    private static Measurement.Factory _fact = null;
    public static Measurement.Factory getFactory() { 
	if (_fact == null) {
	    _fact = new MeasurementDijkstra_Factory();
	}
	return _fact; 
    }

    private DijkstraDistance _dd;
    private Vertex _golden;

    private MeasurementDijkstra(Graph g, Stats s, VertexMapper vm) {
	super(g,s, vm);
    }

    void initialize(Graph g) {
	_dd = new DijkstraDistance(g);
	_golden = GraphUtils.getRandomBigComponentVertex(g);
    }

    double readValue(Vertex vquery) {        
	return _dd.getDistance(vquery, _golden).doubleValue();
    }

    String getName() {
	return getFactory().getName();
    }
};

