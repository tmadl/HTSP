/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp.solvers;

import htsp.TspDrawer;
import htsp.solvers.bruteforce.BruteForceSolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author madlt
 */
public class RandomSolver extends BruteForceSolver {
    List<Integer> solution;

     public RandomSolver(int width, int height) {
        super(width, height);
    }

    public RandomSolver(int width, int height, GoalPoint[] goals) {
        super(width, height, goals);
    }

    public RandomSolver(GoalPoint[] goals) {
        super(goals);
    }

    @Override
    public int getWidth(){return width;}
    @Override
    public int getHeight() {return height;}
    
    @Override
    public double solve() {
        solution = new ArrayList<Integer>();
        for (int i = 0; i < goals.length; i++) {
            solution.add(i);
        }
        Collections.shuffle(solution);
        solution.add(solution.get(0));

        double cost = TSPGrid.pathDistance(goals, solution);
        System.out.println("Random solution cost: "+cost);
        return cost;
    }

    @Override
    public double getProgress() {return 0;}
    @Override
    public void init(int number) {}

    @Override
    public GoalPoint[] getGoalpoints()  {return goals;}
    @Override
    public GoalPoint getGoalPoint(int x, int y)  {return null;}
    @Override
    public List<Integer> getSolutionPath()  {return solution;}
    @Override
    public int getSolutionIterations() {return 0;}
    @Override
    public void setTspDrawer(TspDrawer d) {}
}
