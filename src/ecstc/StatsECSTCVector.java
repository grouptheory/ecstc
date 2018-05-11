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
public class StatsECSTCVector extends StatsVector {

    public StatsECSTCVector() {
	super(true);
    }

    public void beginCompletion() {
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    StatsECSTC s = (StatsECSTC)getStats(name);
	    s.beginCompletion();
	}
    }


    public void endCompletion() { 
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    StatsECSTC s = (StatsECSTC)getStats(name);
	    s.endCompletion();
	}
    }

    public void beginRDS(Graph g) {
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    StatsECSTC s = (StatsECSTC)getStats(name);
	    s.beginRDS(g);
	}
    }
    
    public void endRDS(Graph g, MeasurementVector mtruev) {
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    StatsECSTC s = (StatsECSTC)getStats(name);
	    Measurement mtrue = (Measurement)mtruev.getMeasurement(name);
	    s.endRDS(g,mtrue);
	}

    }

    public void beginGraph(Graph g) {
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    StatsECSTC s = (StatsECSTC)getStats(name);
	    s.beginGraph(g);
	}
    }

    public void endGraph(Graph g) {
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    StatsECSTC s = (StatsECSTC)getStats(name);
	    s.endGraph(g);
	}
    }
};

