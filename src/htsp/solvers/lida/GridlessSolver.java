/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp.solvers.lida;

import htsp.solvers.*;
import htsp.TspDrawer;
import htsp.solvers.bruteforce.BruteForceSolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author madlt
 */
public class GridlessSolver extends BruteForceSolver {
     public GridlessSolver(int width, int height) {
        super(width, height);
    }

    public GridlessSolver(int width, int height, GoalPoint[] goals) {
        super(width, height, goals);
    }

    public GridlessSolver(GoalPoint[] goals) {
        super(goals);
    }

    @Override
    public int getWidth(){return 0;}
    @Override
    public int getHeight() {return 0;}

    //(Csizmadia & Muller, 2008)
    public static double getWeight(double distance) {
        double k = 1;
        return Math.exp(-k*distance);
    }

    @Override
    public double solve() {
        List<Integer> solution = new ArrayList<Integer>();
        /*
        for (int i = 0; i < goals.length; i++) {
            solution.add(i);
        }
        Collections.shuffle(solution);
        solution.add(solution.get(0));*/

        GridNode.repassThreshold = 0.00001;
        GridNode.resetNumbering();

        GridNode[] nodes = new GridNode[goals.length];
        //Connection[] connections = new Connection[(goals.length*goals.length+goals.length)/2]; //cross-connected
        Connection[] connections = new Connection[goals.length*goals.length - goals.length];

        for (int i = 0; i < goals.length; i++) {
            nodes[i] = new GridNode(goals[i].getX(), goals[i].getY());
        }

        //cross-connect nodes
        int c = 0;
        for (int i = 0; i < goals.length; i++) {
            Connection[] neighbors = new Connection[goals.length - 1];
            int n = 0;
            for (int j = 0; j < goals.length; j++) {
                if (i != j)
                {
                    double d = TSPGrid.distance(goals[i], goals[j]);
                    double w = getWeight(d);
                    connections[c] = new Connection(w, d, nodes[i], nodes[j]);
                    neighbors[n++] = connections[c];
                    c++;
                }
            }
            nodes[i].setNeighbors(neighbors);
        }

        //pass activation
        double goalActivation = 0.1;
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].passActivation(goalActivation);
        }

        //sort by activation
        Collections.sort(Arrays.asList(nodes));

        //assemble path
        for (int i = 0; i < nodes.length; i++) {
            solution.add(nodes[i].getNumber());
            System.out.print(nodes[i].getNumber()+" ");
        }
        solution.add(nodes[0].getNumber());
        System.out.print("\n");

        double cost = TSPGrid.pathDistance(goals, solution);
        System.out.println("Gridless solution cost: "+cost);
        return cost;
    }

    @Override
    public double getProgress() {return 0;}
    @Override
    public void init(int number) {}

    @Override
    public GoalPoint[] getGoalpoints()  {return null;}
    @Override
    public GoalPoint getGoalPoint(int x, int y)  {return null;}
    @Override
    public List<Integer> getSolutionPath()  {return null;}
    @Override
    public int getSolutionIterations() {return 0;}
    @Override
    public void setTspDrawer(TspDrawer d) {}
}
