package ecstc;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import java.util.*;

/**
 * A registry of Measurables
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class MeasurementVector {

    private final HashMap _measurements = new HashMap();

    public MeasurementVector(Graph g, StatsVector sv, VertexMapper vm) {
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    Measurement.Factory mf = Measurement.getFactory(name);
	    Stats s = sv.getStats(name);
	    Measurement m = mf.newMeasurement(g,s,vm);
	    _measurements.put(name,m);
	}
    }

    Measurement getMeasurement(String s) {
	return (Measurement)_measurements.get(s);
    }
};

