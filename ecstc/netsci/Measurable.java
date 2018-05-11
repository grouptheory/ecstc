package netsci;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import java.util.*;
//import edu.uci.ics.jung.io.*;
//import java.io.*;

/**
 * An interface representing a measurable quantity
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public interface Measurable {
    public double readValue(Vertex vquery);
};

