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
public class VertexMapperTrue extends VertexMapper {

    VertexMapperTrue() {
	super();
    }

    Vertex vertexToKey(Vertex gv) {
	return gv;
    }
}
