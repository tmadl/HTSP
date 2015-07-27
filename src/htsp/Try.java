/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp;

import htsp.solvers.lida.Edge;
import htsp.solvers.lida.GoalObject;

/**
 *
 * @author Tom
 */
public class Try {
    public static void main(String args[]) {
        //Solver s = new ConcordeSolver();
        //System.out.println(s.solve());

        /*Integer[] nums = new Integer[] {1, 2, 3, 4, 5};
        for (Iterator i = new Permute(nums); i.hasNext(); ) {
	  //final String [] a = (String []) i.next();
          Integer[] a = (Integer[]) i.next();
	  System.out.println (i);
        }*/
/*
        int i = 0;
        Edge e1 = new Edge(new GoalObject(-1, -1, i++), new GoalObject(2, 2, i++));
        double k = e1.getSlope();
        double d = e1.getIntercept();
        Edge e2 = new Edge(new GoalObject(4, 0, i++), new GoalObject(0, 4, i++));
        double k2 = e2.getSlope();
        double d2 = e2.getIntercept();
        boolean b = e1.intersects(e2);
        System.out.println(b);*/
        int[] a = new int[3];
        int[] b = a.clone();
        b[0] = 10;
        System.out.println(a[0]);
    }
}
