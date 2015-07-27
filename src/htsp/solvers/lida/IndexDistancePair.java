/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

/**
 *
 * @author Tom
 */
public class IndexDistancePair implements Comparable {
    public int index;
    public double distance;

    public IndexDistancePair(int index, double distance) {
        this.index = index;
        this.distance = distance;
    }

    

    @Override
    public int compareTo(Object o) {
        if (o instanceof IndexDistancePair)
            return ((int)(distance*100) - (int)(((IndexDistancePair)o).distance*100));
        else
            return Integer.MAX_VALUE;
    }
}
