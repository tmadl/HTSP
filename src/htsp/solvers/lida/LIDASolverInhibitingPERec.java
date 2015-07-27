/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.TspDrawer;
import htsp.solvers.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Tom
 */
public class LIDASolverInhibitingPERec extends HSpatialGrid implements Solver {
    TspDrawer drawer;
    @Override
    public void setTspDrawer(TspDrawer d) {drawer = d;}
    //^ facilitates visualisation for debugging; can be deleted

    protected int solutionGoalOrder = 0;
    List<Integer> solutionPath = new ArrayList<Integer>();
    protected int solutionIterations = 0;

    boolean removeIntersections = true;
    int maxIntersectionRemovals = 100;

    protected int goalminX, goalminY, goalmaxX, goalmaxY;

    public LIDASolverInhibitingPERec(int width, int height) {
        super(width, height);
    }

    public LIDASolverInhibitingPERec(int width, int height, GoalPoint[] goals) {
        super(width, height, goals);
    }

    public LIDASolverInhibitingPERec(GoalPoint[] goals) {
        super(goals);
    }

    @Override
    public final void init(int number) {
        init(TSPGrid.randomGoalPoints(number));
    }

    @Override
    public final void init(GoalPoint[] goals) {
        int maxX = 0, maxY = 0;
        for (GoalPoint n : goals) {
            if (n.getX() > maxX) maxX = n.getX();
            if (n.getY() > maxY) maxY = n.getY();
        }
        init(goals, maxX+1+SpatialGrid.SPREADRADIUS, maxY+1+SpatialGrid.SPREADRADIUS);
    }

    @Override
    public final void init(GoalPoint[] goals, int width, int height) {
        super.init(width, height, goals);
    }

    @Override
    public GoalPoint getGoalPoint(int x, int y) {
        GoalPoint gp = new GoalPoint(x, y, TspDrawer.getColorFromActivation(grid[y][x].getActivation()));
        if (!grid[y][x].isEmpty())
            gp.setMarked(true);
        return gp;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    private void printGrid() {
        String printstr = "";
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (!grid[i][j].isEmpty()) {
                    if (grid[i][j].getContent() instanceof GoalObject)
                        printstr += "G|";
                    else
                        printstr += "?|";
                }
                else
                    printstr += " |";
            }
            printstr+="\n";
            for (int j = 0; j < width; j++)
                printstr+="--";
            printstr+="\n";
        }
        System.out.println(printstr);
    }

    @Override
    public double solve() {
        solutionGoalOrder = 0;
        solutionPath.clear();
        solutionIterations = 0;

        int max_iterations = Math.max(width, height);
        try {
            goalminY = goalminX = Integer.MAX_VALUE;
            goalmaxY = goalmaxX = 0;
            for (GoalObject goal : this.goals) {
                goal.setCurrentX(goal.getOriginalX());
                goal.setCurrentY(goal.getOriginalY());
                if (goal.getCurrentX() < goalminX) goalminX = goal.getCurrentX();
                if (goal.getCurrentX() > goalmaxX) goalmaxX = goal.getCurrentX();
                if (goal.getCurrentY() < goalminY) goalminY = goal.getCurrentY();
                if (goal.getCurrentY() > goalmaxY) goalmaxY = goal.getCurrentY();
                solutionIterations++;
            }
        }
        catch (Exception ex) {return -1;}

        //printGrid();

        boolean finished = false;
        int it = 0;
        for (it = 0; it < max_iterations && !finished; it++) { //constant (depends on grid size)
            boolean goaladjusted = false;
            for (GoalObject goal : this.goals) { //linear with n the number of goals
                if (goal.getCurrentX() == goalminX || goal.getCurrentY() == goalminY || goal.getCurrentX() == goalmaxX || goal.getCurrentY() == goalmaxY) {
                    continue;
                }
                GridNode currentNode = grid[goal.getCurrentY()][goal.getCurrentX()];
                if (currentNode.getX() != goal.getCurrentX() || currentNode.getY() != goal.getCurrentY())
                    System.err.println("WRONG GOAL COORD REPRESENTATION");
                int cBorderDistance = distanceToBorder(goal.getCurrentX(), goal.getCurrentY());
                double minActivation = currentNode.getActivation();
                GridNode targetNode = null;
                //move goal to one of the neighbors
                for (Connection connection : currentNode.getAdjacentNeighbors()) { //constant (depends on number of neighbors)
                    int nBorderDistance = distanceToBorder(connection.getSink().getX(), connection.getSink().getY());
                    if (nBorderDistance < cBorderDistance)
                    {
                        if (connection.getSink().getActivation() < minActivation && connection.getSink().isEmpty()) {
                            targetNode = connection.getSink();
                            minActivation = connection.getSink().getActivation();
                        }
                    }
                }

                if (targetNode == null) {
                    //no empty neighbor to move to - find best nonempty neighbor
                    for (Connection connection : currentNode.getAdjacentNeighbors()) { //constant (depends on number of neighbors)
                        int nBorderDistance = distanceToBorder(connection.getSink().getX(), connection.getSink().getY());
                        if (nBorderDistance < cBorderDistance)
                        {
                            if (connection.getSink().getActivation() < minActivation || targetNode == null) {
                                targetNode = connection.getSink();
                                minActivation = connection.getSink().getActivation();
                            }
                        }
                    }
                }

                //move goal object towards edge, where activation is lowest
                if (targetNode != null) {
                    grid[goal.getCurrentY()][goal.getCurrentX()].removeContent(goal);
                    goal.setCurrentX(targetNode.getX());
                    goal.setCurrentY(targetNode.getY());

                    if (distanceToBorder(goal.getCurrentX(), goal.getCurrentY()) != 0 || grid[goal.getCurrentY()][goal.getCurrentX()].getContent().isEmpty()
                            || true) {
                        grid[goal.getCurrentY()][goal.getCurrentX()].addContent(goal);
                    }
                    else {
                        //edge node not empty - ensure goals ordered by clockwise traversal order
                    }

                    grid[goal.getCurrentY()][goal.getCurrentX()].passActivation(SpatialGrid.DEFAULTGOALINHIBITION);
                    goaladjusted = true;
                    solutionIterations++;
                    if (GoalPath.debugging) {
                       drawer.setGoalpoints(getGoalpoints());
                       drawer.draw();
                       try {
                           Thread.currentThread().sleep(100);
                       }
                       catch (Exception ex) {}
                    }
                }
                else {
                    //goal not on edge but no lowest point of activation found in neighbors
                    System.err.println("failed to move goal node!");
                }
            }
            if (!goaladjusted) {
                finished = true;
                break;
            }
        }

        if (it==max_iterations) System.err.println("reached max_iterations");

        //all goals on the grid edge; now determine ordering

        //List<GoalObject> goalPathList = Arrays.asList(goalPath);
        //Collections.sort(goalPathList, Comparator); // O(n log n) complexity - traverse edge instead

        //traverse grid edge clockwise from (0,0), set order numbers and put back goal objects to original place
        int fx = goalminX, fy = goalminY;
        for (int i = goalminX; i < goalmaxX; i++) {
            if (!grid[fy][i].isEmpty())
                processEdgeGoal(i, fy);
        }
        fx = goalmaxX;
        for (int i = goalminY; i < goalmaxY; i++) {
            if (!grid[i][fx].isEmpty())
                processEdgeGoal(fx, i);
        }
        fy = goalmaxY;
        for (int i = goalmaxX; i >= goalminX; i--) {
            if (!grid[fy][i].isEmpty())
                processEdgeGoal(i, fy);
        }
        fx = goalminX;
        for (int i = goalmaxY; i >= goalminY; i--) {
            if (!grid[i][fx].isEmpty())
                processEdgeGoal(fx, i);
        }

        //System.out.println("####");
        //printGrid();

        //solutionPath.add(solutionPath.get(0));

        List<GoalObject> resultObjects = new ArrayList<GoalObject>();
        //resultObjects contains all goals along best path (but doesnt contain first goal twice)
        for (int i = 0; i < solutionPath.size(); i++) {
            //resultObjects.add(goals[(int)resultPath.get(i)]);
            resultObjects.add((GoalObject)getNodes().get((int)solutionPath.get(i)).getContent().get(0));
        }

        if (resultObjects.size() > 0 && resultObjects.get(0) != resultObjects.get(resultObjects.size() - 1))
            resultObjects.add(resultObjects.get(0));

        if (removeIntersections) {
            int i = 0;
            while (PathConnector.hasIntersections(resultObjects) && i < maxIntersectionRemovals) {
                resultObjects = PathConnector.removeIntersection(new CopyOnWriteArrayList(resultObjects));
                i++;
            }
        }

        //solutionPath contains indices of goals along best path (contains index of first goal twice - back to origin)
        if (solutionPath.size() > 0 && solutionPath.get(0) != solutionPath.get(solutionPath.size() - 1))
            solutionPath.add(solutionPath.get(0));


        //debugging code - show goals not lying on edge
        for (GoalObject goal : goals) {
            if (!(goal.getCurrentX() == goalminX || goal.getCurrentY() == goalminY || goal.getCurrentX() == goalmaxX || goal.getCurrentY() == goalmaxY)) {
                System.err.println("error in solution: goal hasnt reached border: "+goal.getCurrentX() +" "+ goal.getCurrentY());
                return 0;
            }
            if (drawer != null) drawer.addPx(drawer.transformX(goal.getCurrentX(), true), drawer.transformY(goal.getCurrentY(), true));
        }


        double cost = TSPGrid.pathDistance(this.getGoalPoints(), solutionPath);
        System.out.println("Solution cost: "+cost+"; solution iterations: "+solutionIterations);
        return cost;
    }

    @Override
    public int getSolutionIterations() {
        return this.solutionIterations;
    }

    protected void processEdgeGoal(int x, int y) {
        try {
            //GoalObject goal = ((GoalObject)grid[y][x].getContent());
            CopyOnWriteArrayList<Object> content = new CopyOnWriteArrayList(grid[y][x].getContent());
            for (Object o : content) {
                GoalObject goal = (GoalObject)o;
                goal.setOrderNumber(solutionGoalOrder++);
                grid[y][x].removeContent(goal);
                grid[goal.getOriginalY()][goal.getOriginalX()].addContent(goal);
                solutionPath.add(goal.getOriginalOrderNumber());
                solutionIterations++;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //walking clockwise along the outer border of a rectangle, how far is the point
    protected int distanceOnBorderFromCorner(int x, int y) {
        int d = Integer.MAX_VALUE;
        if (y == 0)
            d = x;
        else if (x == width)
            d = width + y;
        else if (y == height)
            d = height + 2 * width - x;
        else if (x == 0)
            d = 2 * height + 2 * width - y;

        return d;
    }

    protected int distanceToBorder(int x, int y) {
        return Math.min(Math.min(x - goalminX, goalmaxX - x), Math.min(y - goalminY, goalmaxY - y));
    }

    @Override
    public double getProgress() {
        return 0;
    }

    @Override
    public GoalPoint[] getGoalpoints() {
        return super.getGoalPoints();
    }

    @Override
    public List<Integer> getSolutionPath() {
        return solutionPath;
    }

    public List<Integer> getSolutionPathFromGoalPath() {
        List<Integer> solutionPath = new ArrayList<Integer>();
        List<GoalObject> goalPathList = Arrays.asList(goals);
        //sorted by current order number, but solution path consists of indices from original order number
        Collections.sort(goalPathList);
        for (GoalObject g : goalPathList) {
            solutionPath.add(g.getOriginalOrderNumber());
        }
        if (solutionPath.get(solutionPath.size() - 1) != solutionPath.get(0))
            solutionPath.add(solutionPath.get(0));
        return solutionPath;
    }
}
