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
public class Experimenter {

    public static NumberFormat formatter = new DecimalFormat("#0000.0");
    public static NumberFormat formatterlong = new DecimalFormat("#.0000");
    private HashMap _analyses = new HashMap();

    private Experimenter() {
	initialize();
    }

    private static Experimenter _instance = null;

    public static Experimenter instance() {
	if (_instance==null) {
	    _instance=new Experimenter();
	}
	return _instance;
    }

    void registerAnalysis(Analysis a) {
	_analyses.put(a.getName(), a);
    }

    void initialize() {
 	registerAnalysis(AnalysisPearson.instance());
 	registerAnalysis(AnalysisCoverage.instance());
 	registerAnalysis(AnalysisSlope.instance());
 	registerAnalysis(AnalysisIntercept.instance());
 	registerAnalysis(AnalysisMisclassification.instance());
 	registerAnalysis(AnalysisMisclassificationDegreeHeuristic.instance());
	registerAnalysis(AnalysisWorstCaseError.instance());
	registerAnalysis(AnalysisStdMean.instance());
	registerAnalysis(AnalysisMeanStd.instance());
	registerAnalysis(AnalysisWorstVertexMeanStd.instance());
	registerAnalysis(AnalysisStdVertexMeanStd.instance());
	registerAnalysis(AnalysisStdStd.instance());
    }

    void showAnalyses(Graph g, StatsTrue st, StatsECSTC se, String exp) {
	Log.diag("results", Log.INFO, "# ==== ANALYSIS "+exp+" BEGIN");
	for (Iterator it=_analyses.values().iterator(); it.hasNext();) {
	    Analysis a = (Analysis)it.next();
	    double v = a.compute(g, st, se);
	    Log.diag("results", Log.INFO, "# "+a.getName()+" "+formatterlong.format(v)+" "+exp);
	}
	Log.diag("results", Log.INFO, "# ==== ANALYSIS "+exp+" END");
    }

    void showEstimates(Graph g, StatsTrue st, StatsECSTC se, String exp) {
	Log.diag("results", Log.INFO, "# ==== ESTIMATES "+exp+" BEGIN");
	Log.diag("results", Log.INFO, "# V \t true \t aveM \t stdM \t aveS \t stdS");
	for (Iterator vit= g.getVertices().iterator(); vit.hasNext(); ) {
	    // for each vertex in G
	    Vertex vquery_g = (Vertex)vit.next();
	    try {
		Log.diag("results", Log.INFO, ""+vquery_g+" \t"+
			 formatter.format(st.getMeasure(vquery_g))+" \t"+
			 formatter.format(se.getMeanMean(vquery_g))+" \t"+
			 formatter.format(se.getStdMean(vquery_g))+" \t"+
			 formatter.format(se.getMeanStd(vquery_g))+" \t"+
			 formatter.format(se.getStdStd(vquery_g))+"");
	    }
	    catch (Exception ex) {
	    }
	}
	Log.diag("results", Log.INFO, "# ==== ESTIMATES "+exp+" END");
    }

    void execute(Graph g, StatsTrueVector stv, StatsECSTCVector sev) {
	
	Log.diag("results", Log.INFO, "# ======== EXPERIMENTER BEGIN");

	for (Iterator it = Measurement.iteratorFactories(); it.hasNext();) {
	    String name = (String)it.next();
	    Measurement.Factory mf = Measurement.getFactory(name);
	    StatsTrue st = (StatsTrue)stv.getStats(name);
	    StatsECSTC se = (StatsECSTC)sev.getStats(name);
	    showAnalyses(g,st,se,mf.getName());
	    if (Params.SHOW_ESTIMATES) showEstimates(g,st,se,mf.getName());
	}
	Log.diag("results", Log.INFO, "# ======== EXPERIMENTER END");
    }
}

