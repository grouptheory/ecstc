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
public class AnalysisPearson extends Analysis {

    private static Analysis _instance = null;
    public static Analysis instance() {
	if (_instance==null) {
	    _instance=new AnalysisPearson();
	}
	return _instance;
    }
    private AnalysisPearson() {}

    String getName() { return "PEARSON"; }

    double compute(Graph g, StatsTrue st, StatsECSTC se) {

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
