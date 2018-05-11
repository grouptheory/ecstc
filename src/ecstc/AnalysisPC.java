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
public class AnalysisPC extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisPC();
	}
	return _instance;
    }
    private AnalysisPC() {}

    String getName() { return "PC"; }

    double compute(Graph g, StatsTrue st, StatsECSTC se) {
	int MAXN = 1000;
        int n = 0;
        double[] x = new double[g.numVertices()];
        double[] y = new double[g.numVertices()];
	double sumx, sumy, sumx2, xbar, ybar, beta0, beta1;

        // FIRST pass: TRUE regression against degree
        sumx = 0.0, sumy = 0.0, sumx2 = 0.0;

	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    
	    try {
		x[n] = vquery_g.inDegree()+ vquery_g.outDegree();
		y[n] = st.getMeasure(vquery_g);
		sumx  += x[n];
		sumx2 += x[n] * x[n];
		sumy  += y[n];
		n++;
	    }
	    catch (Exception ex) {
		// no estimate
	    }
	}

        xbar = sumx / n;
        ybar = sumy / n;

        // compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        beta1 = xybar / xxbar;
        beta0 = ybar - beta1 * xbar;

        // print results
        System.out.println("REGRESSION OF TRUE VALUES:  y   = " + beta1 + " * x + " + beta0);

	HashMap errorTrue = new HashMap();

	int n=0;
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    try {
		double tmp = st.getMeasure(vquery_g);
		double fit = beta1*x[n] + beta0;
		double residue = (y[n] - fit);
		errorTrue.put(vquery_g, new Double(residue));

		n++;
	    }
	    catch (Exception ex) {
		// no estimate
	    }
	}

        // SECOND pass: ESTIMATE regression against degree
        sumx = 0.0, sumy = 0.0, sumx2 = 0.0;

	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    
	    try {
		x[n] = vquery_g.inDegree()+ vquery_g.outDegree();
		y[n] = se.getMeanMean(vquery_g);
		sumx  += x[n];
		sumx2 += x[n] * x[n];
		sumy  += y[n];
		n++;
	    }
	    catch (Exception ex) {
		// no estimate
	    }
	}

        xbar = sumx / n;
        ybar = sumy / n;

        // compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        beta1 = xybar / xxbar;
        beta0 = ybar - beta1 * xbar;

        // print results
        System.out.println("REGRESSION OF ESTIMATED VALUES:  y   = " + beta1 + " * x + " + beta0);

	HashMap errorEst = new HashMap();

	int n=0;
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    try {
		double tmp = st.getMeasure(vquery_g);
		double fit = beta1*x[n] + beta0;
		double residue = (y[n] - fit);
		errorEst.put(vquery_g, new Double(residue));

		n++;
	    }
	    catch (Exception ex) {
		// no estimate
	    }
	}

        // THIRD pass: ESTIMATE regression against degree

        Set vset = g.getVertices();
        int N = 0;
        for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
            Vertex vquery_g = (Vertex)vit.next();
	    try {
                se.getMeanMean(vquery_g);
                N++;
            }
            catch(Exception ex) {
            }
        }

	double result;
	if (N==0) {
	    result = 0.0;
	}
        else {
	    double x[] = new double[N];
	    double y[] = new double[N];
	    
	    double sumx, sumy;
	    sumx = sumy = 0;
	    int j=0;
	    for (Iterator vit= vset.iterator(); vit.hasNext(); ) {
		Vertex vquery_g = (Vertex)vit.next();
		try {
		    y[j]=se.getMeanMean(vquery_g);
		    x[j]=st.getMeasure(vquery_g);
		    sumx += x[j];
		    sumy += y[j];
		    j++;
		}
		catch(Exception ex) {
		}
	    }
	    double mean_x = sumx/(double)N;
	    double mean_y = sumy/(double)N;

	    double sumdev2x, sumdev2y;
	    sumdev2x = sumdev2y = 0;
	    for (int i=0; i<N; i++) {
		sumdev2x += ((x[i]-mean_x)*(x[i]-mean_x));
		sumdev2y += ((y[i]-mean_y)*(y[i]-mean_y));
	    }
	    double sx = Math.sqrt(sumdev2x / (double)N);
	    double sy = Math.sqrt(sumdev2y / (double)N);

	    double sumr = 0.0;
	    for (int i=0; i<N; i++) {
		double zx = (x[i] - mean_x)/sx;
		double zy = (y[i] - mean_y)/sy;
		sumr += zx*zy;
	    }
	    result = sumr / (double)N;
	}


        return result;
    }
}
