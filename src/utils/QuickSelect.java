/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import htsp.solvers.TSPGrid;
import htsp.solvers.lida.IndexDistancePair;
import htsp.solvers.lida.GoalObject;
import java.util.ArrayList;
import java.util.List;

/**
 * k smallest elements in O(n) time - http://pine.cs.yale.edu/pinewiki/QuickSelect
 */
public class QuickSelect {
  /**
     * Quick selection algorithm.
     * Places the kth smallest item in a[k-1].
     * @param a an array of Comparable items.
     * @param k the desired rank (1 is minimum) in the entire array.
     */
    public static void quickSelect( Comparable [ ] a, int k ) {
        quickSelect( a, 0, a.length - 1, k );
    }

    /**
     * Internal selection method that makes recursive calls.
     * Uses median-of-three partitioning and a cutoff of 10.
     * Places the kth smallest item in a[k-1].
     * @param a an array of Comparable items.
     * @param low the left-most index of the subarray.
     * @param high the right-most index of the subarray.
     * @param k the desired rank (1 is minimum) in the entire array.
     */
    private static void quickSelect( Comparable [ ] a, int low, int high, int k ) {
        if( low + CUTOFF > high )
            insertionSort( a, low, high );
        else {
            // Sort low, middle, high
            int middle = ( low + high ) / 2;
            if( a[ middle ].compareTo( a[ low ] ) < 0 )
                swapReferences( a, low, middle );
            if( a[ high ].compareTo( a[ low ] ) < 0 )
                swapReferences( a, low, high );
            if( a[ high ].compareTo( a[ middle ] ) < 0 )
                swapReferences( a, middle, high );

            // Place pivot at position high - 1
            swapReferences( a, middle, high - 1 );
            Comparable pivot = a[ high - 1 ];

            // Begin partitioning
            int i, j;
            for( i = low, j = high - 1; ; ) {
                while( a[ ++i ].compareTo( pivot ) < 0 )
                    ;
                while( pivot.compareTo( a[ --j ] ) < 0 )
                    ;
                if( i >= j )
                    break;
                swapReferences( a, i, j );
            }

            // Restore pivot
            swapReferences( a, i, high - 1 );

            // Recurse; only this part changes
            if( k <= i )
                quickSelect( a, low, i - 1, k );
            else if( k > i + 1 )
                quickSelect( a, i + 1, high, k );
        }
    }


    /**
     * Internal insertion sort routine for subarrays
     * that is used by quicksort.
     * @param a an array of Comparable items.
     * @param low the left-most index of the subarray.
     * @param n the number of items to sort.
     */
    private static void insertionSort( Comparable [ ] a, int low, int high ) {
        for( int p = low + 1; p <= high; p++ ) {
            Comparable tmp = a[ p ];
            int j;

            for( j = p; j > low && tmp.compareTo( a[ j - 1 ] ) < 0; j-- )
                a[ j ] = a[ j - 1 ];
            a[ j ] = tmp;
        }
    }


    private static final int CUTOFF = 10;

    /**
     * Method to swap to elements in an array.
     * @param a an array of objects.
     * @param index1 the index of the first object.
     * @param index2 the index of the second object.
     */
    public static final void swapReferences( Object [ ] a, int index1, int index2 ) {
        Object tmp = a[ index1 ];
        a[ index1 ] = a[ index2 ];
        a[ index2 ] = tmp;
    }

    public static void main (String [] args) {
        List<GoalObject> goals = new ArrayList<GoalObject>();
        int o = 0;
        goals.add(new GoalObject(2, 4, o++));
        goals.add(new GoalObject(3, 3, o++));
        goals.add(new GoalObject(5, 7, o++));
        goals.add(new GoalObject(2, 1, o++));
        goals.add(new GoalObject(5, 9, o++));
        goals.add(new GoalObject(11, 7, o++));
        goals.add(new GoalObject(6, 6, o++));
        goals.add(new GoalObject(1, 1, o++));
        goals.add(new GoalObject(5, 5, o++));
        goals.add(new GoalObject(6, 7, o++));
        goals.add(new GoalObject(3, 1, o++));
        goals.add(new GoalObject(5, 6, o++));
        IndexDistancePair[] goalsDistances = new IndexDistancePair[goals.size()];
        double centX = 0, centY = 0;
        int i = 0;
        for (GoalObject go : goals) {
            goalsDistances[i]=(new IndexDistancePair(i, TSPGrid.distance(centX, centY, go.getCurrentX(), go.getCurrentY())));
            i++;
        }
        int k = 3;
        QuickSelect.quickSelect(goalsDistances, k);
        for (i = 0; i < k; i++) {
            System.out.println(goals.get(goalsDistances[i].index)+"\t"+goalsDistances[i].distance);
        }
    }

}
