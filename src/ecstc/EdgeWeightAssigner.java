package ecstc;

import edu.uci.ics.jung.graph.*;
import java.util.*;

/**
 * Interface to assign weights to edges in ECSTC
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public interface EdgeWeightAssigner {

    public double getWeight(Edge e);
}
