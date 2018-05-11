package ecstc;

public class Distance {

    //****************************
    // Get minimum of three values
    //****************************

    private static int Minimum (int a, int b, int c) {
	int mi;

	mi = a;
	if (b < mi) {
	    mi = b;
	}
	if (c < mi) {
	    mi = c;
	}
	return mi;

    }

    //*****************************
    // Compute Levenshtein distance
    //*****************************

    public static int LD (int[] s, int[] t, int n) {
	int d[][]; // matrix
	int i; // iterates through s
	int j; // iterates through t
	int s_i; // ith character of s
	int t_j; // jth character of t
	int cost; // cost

	// Step 1

	d = new int[n+1][n+1];

	// Step 2

	for (i = 0; i <= n; i++) {
	    d[i][0] = i;
	}

	for (j = 0; j <= n; j++) {
	    d[0][j] = j;
	}

	// Step 3

	for (i = 1; i <= n; i++) {

	    s_i = s[i - 1];

	    // Step 4

	    for (j = 1; j <= n; j++) {

		t_j = t[j - 1];

		// Step 5

		if (s_i == t_j) {
		    cost = 0;
		}
		else {
		    cost = 1;
		}

		// Step 6

		d[i][j] = Distance.Minimum( d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1]+cost );

	    }

	}

	// Step 7

	return d[n][n];
    }

}