package ecstc;

import edu.uci.ics.jung.graph.*;
import java.util.*;

/**
 * Class to produce output log
 * 
 * @version 	$$
 * @author 	Bilal Khan
 */
public class Log {

    public static class Level implements Comparable {
	private int _value;

	Level(int v) {
	    _value = v;
	}

	public int compareTo(Object obj) {
	    Level other = (Level)obj;
	    if (this._value < other._value) return -1;
	    else if (this._value > other._value) return +1;
	    else return 0;
	}

	public String toString() {
	    if (_value==1) return "DEBUG";
	    else if (_value==2) return "INFO";
	    else if (_value==3) return "WARN";
	    else if (_value==4) return "ERROR";
	    else return "FATAL";
	}
    }

    public static final Level DEBUG = new Level(1);
    public static final Level INFO = new Level(2);
    public static final Level WARN = new Level(3);
    public static final Level ERROR = new Level(4);
    public static final Level FATAL = new Level(5);
    
    public static final Level DEFAULT_LEVEL = INFO;
    private static final int KEYLENGTH = 10;

    private static final HashMap _key2level = new HashMap();

    public static void setLevel(Object key, Level lev) {
	_key2level.put(key, lev);
    }
    
    public static Level getLevel(Object key) {
	Level setting = (Level)_key2level.get(key);
	if (setting==null) {
	    setting = DEFAULT_LEVEL;
	}
	return setting;
    }
    
    public static void diag(Object key, Level lev, String s) {
	Level setting = getLevel(key);

	if (key instanceof String) {
	    String skey = (String)key;
	    int index = skey.lastIndexOf(".");
	    if (index >= 0) {
		skey = skey.substring(index+1);
	    }
	    int len = skey.length();
	    while (len < KEYLENGTH) {
		skey = skey+" ";
		len = skey.length();
	    }

	    if (len > KEYLENGTH) {
		skey = skey.substring(0, KEYLENGTH);
	    }
	    else {
	    }
	    key = skey;
	    
	}

	if (setting.compareTo(lev) <= 0) {
	    System.out.println(key+" ("+lev+") "+"\t"+s);
	    if (lev.compareTo(FATAL)==0) {
		System.exit(-1);
	    }
	}
    }
}
