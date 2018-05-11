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
public abstract class StatsVector {

    protected final HashMap _stats = new HashMap();

    protected StatsVector(boolean ecstc) {
	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    if (ecstc) _stats.put(name, new StatsECSTC());
	    else _stats.put(name, new StatsTrue());
	}
    }

    final Stats getStats(String s) {
	return (Stats)_stats.get(s);
    }
};

