package ecstc;

import edu.uci.ics.jung.io.*;
import edu.uci.ics.jung.graph.*;
import java.util.*;

/**
 * A class to collect measurements of the graph's vertices and
 * aggregate statistics about the vertices of the completions
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class VertexMapperECSTC extends VertexMapper {

    private final Completion _c;
    private final RDS _r;
    private final Graph _g;

    VertexMapperECSTC(Completion c, RDS r, Graph g) {
	super();
	_c = c;
	_r = r;
	_g = g;
    }

    Vertex vertexToKey(Vertex cv) {
	Vertex rv = _c.getRDSVertex(cv);
	Vertex gv = _r.getGraphVertex(rv);
	return gv;
    }
}
