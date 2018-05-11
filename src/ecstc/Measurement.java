package ecstc;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import java.util.*;

/**
 * An interface representing a measurement quantity
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public abstract class Measurement {

    private static final HashMap _factories = new HashMap();

    static Iterator iteratorFactories() {
	return _factories.keySet().iterator();
    }

    static Measurement.Factory getFactory(String s) {
	return (Measurement.Factory)_factories.get(s);
    }

    public static abstract class Factory {
	public abstract Measurement newMeasurement(Graph g, Stats s, VertexMapper vm);
	public abstract String getName();

	protected void register() {
	    if (_factories.get(this.getName())!=null) {
		Log.diag(Measurement.Factory.class.getName(), Log.FATAL, "Multiple registrations for Measurement:"+this.getName());
	    }
	    else {
		_factories.put(this.getName(), this);
	    }
	}

    }

    protected Measurement(Graph g, Stats s, VertexMapper vm) {
	initialize(g);
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    Vertex vquery_g = (Vertex)vit.next();
	    double val1 = this.readValue(vquery_g);
	    Vertex key = vm.vertexToKey(vquery_g);
	    s.setMeasure(key, val1);
	}
    }

    abstract void initialize(Graph g);
    abstract double readValue(Vertex vquery);
    abstract String getName();
};


