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
public abstract class VertexMapper {

    protected VertexMapper() {
    }

    abstract Vertex vertexToKey(Vertex cv);
}
