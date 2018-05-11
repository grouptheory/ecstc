package ecstc;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.algorithms.metrics.*;
import java.util.*;

/**
 * A class representing measuring effective size
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class MeasurementES extends Measurement {

    public static class MeasurementES_Factory extends Measurement.Factory {
	private MeasurementES_Factory() {
	    super.register();
	}

	public Measurement newMeasurement(Graph g, Stats s, VertexMapper vm) { 
	    return new MeasurementES(g,s,vm); 
	}

	public String getName() { return "ES"; }
    }

    public static Measurement.Factory _fact = null;
    public static Measurement.Factory getFactory() {
	if (_fact == null) {
	    _fact = new MeasurementES_Factory();
	}
	return _fact;
    }

    private StructuralHoles _sh;

    MeasurementES(Graph g, Stats s, VertexMapper vm) {
	super(g,s, vm);
    }
	
    void initialize(Graph g) {
	_sh = new StructuralHoles(new ConstantEdgeValue(1.0));
    }

    public double readValue(Vertex vquery) {        
	return _sh.effectiveSize(vquery);
    }

    public String getName() {
	return getFactory().getName();
    }
};

