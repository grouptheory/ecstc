package ecstc;

import edu.uci.ics.jung.graph.*;
import java.util.*;

/**
 * Class to assign constant weights to edges in ECSTC
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class EdgeWeightAssigner_Constant implements EdgeWeightAssigner {

    public double getWeight(Edge e) {
	return 1.0;
    }
}
