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
public class Registry {
    private static Registry _instance = null;
    public static Registry instance() {
	if (_instance == null) {
	    _instance = new Registry();
	}
	return _instance;
    }
};

