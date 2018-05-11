package ecstc;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A class to collect measurements of the graph's vertices and
 * aggregate statistics about the vertices of the completions
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public abstract class Analysis {

    public static NumberFormat formatter = new DecimalFormat("#0000.0");

    abstract double compute(Graph g, StatsTrue st, StatsECSTC se);
    abstract String getName();
}

