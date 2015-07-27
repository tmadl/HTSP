/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.TspDrawer;
import htsp.solvers.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Tom
 */
public class LIDASolverNN extends HSpatialGrid implements Solver {
    TspDrawer drawer;
    @Override
    public void setTspDrawer(TspDrawer d) {drawer = d;}
    //^ facilitates visualisation for debugging; can be deleted

    protected int solutionGoalOrder = 0;
    List<Integer> solutionPath = new ArrayList<Integer>();
    protected int solutionIterations = 0;
    
    protected int goalminX, goalminY, goalmaxX, goalmaxY;

    public LIDASolverNN(int width, int height) {
        super(width, height);
    }

    public LIDASolverNN(int width, int height, GoalPoint[] goals) {
        super(width, height, goals);
    }

    public LIDASolverNN(GoalPoint[] goals) {
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

        //start with first goal, climb activation gradients, pass negative activation
        GoalObject firstGoal, currentGoal;
        GridNode currentNode;
        int maxclimbiterations = Math.max(this.getHeight(), this.getWidth());
        currentGoal = this.goals[0];
        currentNode = this.grid[currentGoal.getCurrentY()][currentGoal.getCurrentX()];
        solutionPath.add(currentGoal.getOrderNumber());
        double pathActivation = -0.5;
        for (int i = 0; i < this.goals.length - 1; i++) {
            //pass negative activation to current goal node
            if (!currentNode.isEmpty() && currentNode.getX() == currentGoal.getCurrentX() && currentNode.getY() == currentGoal.getCurrentY()) {
                currentNode.passActivation(-HSpatialGrid.DEFAULTGOALACTIVATION);
            }

            //climb activation gradient until next goal reached
            for (int j = 0; j < maxclimbiterations; j++) {
                double maxActivation = currentNode.getActivation();
                GridNode targetNode = null;
                //find neighbor with highest activation
                for (Connection connection : currentNode.getAdjacentNeighbors()) { //constant (depends on number of neighbors)
                    if (connection.getSink().getActivation() > maxActivation) {
                        targetNode = connection.getSink();
                        maxActivation = connection.getSink().getActivation();
                    }
                }

                if (GoalPath.debugging) {
                   drawer.setGoalpoints(getGoalpoints());
                   drawer.drawCircle(currentNode.getX(), currentNode.getY(), targetNode!=null?Color.green:Color.red, drawer.getGoalSize()+4);
                   drawer.draw();
                   try {
                       Thread.currentThread().sleep(200);
                   }
                   catch (Exception ex) {}
                }

                solutionIterations++;

                //move to highest neighbor
                if (targetNode != null) {
                    currentNode = targetNode;
                    currentNode.passActivation(pathActivation);

                    if (!currentNode.isEmpty()) { //neighbor contains goal, set current and add to path
                        currentGoal = (GoalObject)currentNode.getContent().get(0);
                        solutionPath.add(currentGoal.getOrderNumber());
                        break;
                    }
                }
                else {
                    //goal not on edge but no lowest point of activation found in neighbors
                    System.err.println("failed to move goal node!");
                }
            }
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
