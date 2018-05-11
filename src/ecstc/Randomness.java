package ecstc;

import edu.uci.ics.jung.graph.*;
import java.util.*;

/**
 * Class to generate random numbers
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Randomness {

    // random number seed
    static int RNGSEED = 0;

    private static boolean _initialized = false;
    private static Random _rng = null;

    public static void initialize(int seed) {
	if (!_initialized) {
	    _initialized = true;
	    Log.diag(Randomness.class.getName(), Log.DEBUG, "RNG initialized with seed = "+seed);
	    _rng = new Random(seed);
	}
	else {
	    Log.diag(Randomness.class.getName(), Log.WARN, "Cannot reinitialize RNG!");
	}
    }

    public static Double getDouble() {
	if (!_initialized) {
	    Log.diag(Randomness.class.getName(), Log.INFO, "Using specified RNG seed");
	    initialize(Randomness.RNGSEED);
	}
	return _rng.nextDouble();
    }
}
