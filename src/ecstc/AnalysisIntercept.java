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
public class AnalysisIntercept extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisIntercept();
	}
	return _instance;
    }
    private AnalysisIntercept() {}

    String getName() { return "INTERCEPT"; }

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

	return beta0;

	/*
        System.out.println("R^2                 = " + R2);
        System.out.println("std error of beta_1 = " + Math.sqrt(svar1));
        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));
        svar0 = svar * sumx2 / (n * xxbar);
        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));

        System.out.println("SSTO = " + yybar);
        System.out.println("SSE  = " + rss);
        System.out.println("SSR  = " + ssr);
	*/
    }
}
