/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp.solvers.bruteforce;

import htsp.TspDrawer;
import htsp.solvers.TSPGrid;
import htsp.solvers.GoalPoint;
import htsp.solvers.Solver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author madlt
 */
public class BruteForceSolver extends TSPGrid implements Solver {
    private double bestCost;
    private Set<Integer> goalsSet;
    private List<Integer> solution;

    private double cSolutionNo = -1, maxSolutions;

    TspDrawer drawer;
    @Override
    public void setTspDrawer(TspDrawer d) {drawer = d;}

    public BruteForceSolver(int width, int height) {
        //super(width, height);
        init(goals);
    }
    
    public BruteForceSolver(int width, int height, GoalPoint[] goals) {
        //super(width, height, goals);
        init(goals, width, height);
    }

    public BruteForceSolver(GoalPoint[] goals) {
        //super(width, height, goals);
        init(goals);
    }
    
    @Override
    public void init(int number) {
        init(TSPGrid.randomGoalPoints(number));
    }

    @Override
    public final void init(GoalPoint[] goals, int width, int height) {
        this.goals = goals;
        this.width = width;
        this.height = height;
        grid=new int[height][];
        for (int i = 0; i<height; i++) grid[i]=new int[width];

        for (GoalPoint n : goals) {
            grid[n.getY()][n.getX()]=1;
        }

        goalsSet = new HashSet<Integer>();
        for (int i = 0; i < goals.length; i++) goalsSet.add(i);
        bestCost = Double.MAX_VALUE;
        solution = new ArrayList<Integer>();

        cSolutionNo = -1;
        maxSolutions = (int)(factorial(goals.length - 1) * 3.71827877); //approx
    }

    @Override
    public int getSolutionIterations() {return (int)maxSolutions;}

    @Override
    public final void init(GoalPoint[] goals) {
        int maxX = 0, maxY = 0;
        for (GoalPoint n : goals) {
            if (n.getX() > maxX) maxX = n.getX();
            if (n.getY() > maxY) maxY = n.getY();
        }
        init(goals, maxX+1, maxY+1);
    }

    @Override
    public GoalPoint getGoalPoint(int x, int y) {
        for (GoalPoint g : goals) {
            if (g.getX() == x && g.getY() == y) return g;
        }
        return null;
    }

    private double factorial(int n) {
        double result = n;
        while (--n > 1)
            result *= n;
        return result;
    }
    
    @Override
    public int getWidth() {return width;}
    @Override
    public int getHeight() {return height;}
    
    @Override
    public double getProgress() {
        return (int)((100.0/maxSolutions)*cSolutionNo);
    }

    @Override
    public double solve() {
        List<Integer> path = new ArrayList<Integer>();
        path.add(0);
        cSolutionNo = -1;
        //exactDistances = true;
        solve(0, path, 0);
        //exactDistances = false;
        //double realCost = TSPGrid.pathDistance(goals, solution);
        System.out.println("BruteForce cost: "+bestCost);
        return bestCost;
    }
    
    protected void solve(int goalIndex, List<Integer> path, double cost) {
        Set<Integer> choices = new HashSet<Integer>(goalsSet);
        choices.removeAll(path);
        if (choices.isEmpty() && path.get(path.size() - 1) != 0) {
            choices.add(0);
        }

        cSolutionNo++;

        for (int c : choices) {
            List<Integer> newPath = new ArrayList<Integer>(path);
            newPath.add(c);
            solve(c, newPath, cost + this.distance(goalIndex, c));
        }
        
        if (choices.isEmpty()) {
            //solution
            if (cost < bestCost) {
                bestCost = cost;
                solution = path;
            }
        }
    }
    
    public double cost(List<Integer> path) {
        double c = 0;
        for (int i=0; i<path.size(); i++) {
            c += distance(path.get(i), path.get(i+1));
        }
        c += distance(path.get(path.size()-1), path.get(0));
        return c;
    }
    
    @Override
    public List<Integer> getSolutionPath() {
        return solution;
    }
    
    @Override
    public GoalPoint[] getGoalpoints() {
        return goals;
    }
}
