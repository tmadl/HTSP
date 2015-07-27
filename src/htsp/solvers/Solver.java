/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp.solvers;

import htsp.TspDrawer;
import java.util.List;

/**
 *
 * @author madlt
 */
public interface Solver {
    public int getWidth();
    public int getHeight();
    public double solve();
    //public void solve(int nodeIndex, List<Integer> path, double cost);
    public double getProgress();
    public void init(int number);
    public void init(GoalPoint[] nodes);
    public void init(GoalPoint[] nodes, int width, int height);
    public GoalPoint[] getGoalpoints();
    public GoalPoint getGoalPoint(int x, int y);
    public List<Integer> getSolutionPath();
    public int getSolutionIterations();
    public void setTspDrawer(TspDrawer d);
}
