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
public class AnalysisSlope extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisSlope();
	}
	return _instance;
    }
    private AnalysisSlope() {}

    String getName() { return "SLOPE"; }

    double compute(Graph g, StatsTrue st, StatsECSTC se) {

	int MAXN = 1000;
        int n = 0;
        double[] x = new double[g.numVertices()];
        double[] y = new double[g.numVertices()];

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;

	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    
	    try {
		double val = st.getMeasure(vquery_g);
		double est = se.getMeanMean(vquery_g);
		x[n] = val;
		y[n] = est;
		sumx  += x[n];
		sumx2 += x[n] * x[n];
		sumy  += y[n];
		n++;
	    }
	    catch (Exception ex) {
		// no estimate
	    }
	}

        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        // print results
        // System.out.println("y   = " + beta1 + " * x + " + beta0);


        // analyze results
        int df = n - 2;
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = beta1*x[i] + beta0;
            rss += (fit - y[i]) * (fit - y[i]);
            ssr += (fit - ybar) * (fit - ybar);
        }
        double R2    = ssr / yybar;
        double svar  = rss / df;
        double svar1 = svar / xxbar;
        double svar0 = svar/n + xbar*xbar*svar1;

	return beta1;

	/*
        System.out.println("R^2                 = " + R2);
        System.out.println("std error of beta_1 = " + Math.sqrt(svar1));
        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));
        svar0 = svar * sumx2 / (n * xxbar);
        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));

        System.out.println("SSTO = " + yybar);
        System.out.println("SSE  = " + rss);
        System.out.println("SSR  = " + ssr);


	// OLD CODE

	TreeMap estVal = new TreeMap();
	TreeMap trueVal = new TreeMap();
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    
	    try {
		double est = se.getMeanMean(vquery_g);
		
		double eps = 0;
		for (;estVal.get(new Double(est+eps))!=null; eps+=0.0001);
		estVal.put(new Double( (-1.0*(est+eps)) ), vquery_g);
		
		double val = st.getMeasure(vquery_g);
		eps = 0;
		for (;trueVal.get(new Double(val+eps))!=null; eps+=0.0001);
		trueVal.put(new Double( (-1.0*(val+eps)) ), vquery_g);
	    }
	    catch (Exception ex) {
		// no estimate
	    }
	}
	int p1[] = new int[g.numVertices()];
	int index=0;
	for (Iterator t1=trueVal.values().iterator(); t1.hasNext();) {
	    Vertex vquery_g = (Vertex)t1.next();
	    int v1 = Integer.parseInt(vquery_g.toString().substring(1));
	    p1[index++] = v1;
	}
	int p2[] = new int[g.numVertices()];
	index=0;
	for (Iterator t2=estVal.values().iterator(); t2.hasNext();) {
	    Vertex vquery_g = (Vertex)t2.next();
	    int v2 = Integer.parseInt(vquery_g.toString().substring(1));
	    p2[index++] = v2;
	}
	int count=index;
	// int ld = Distance.LD(p1, p2, count);
	int ld = 0;
	
	double sumXY = 0;
	double sumXSquared = 0;
	for (int i=0; i<count; i++) {
	    sumXY       += p1[i] * p2[i];
	    sumXSquared += p1[i] * p1[i];
	}
	double slopeEstimateECSTC = sumXY / sumXSquared;
	
        return slopeEstimateECSTC;
	*/
    }
}
