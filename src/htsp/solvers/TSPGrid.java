/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp.solvers;

import htsp.solvers.lida.GoalObject;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author madlt
 */
public class TSPGrid {
    protected int width, height;
    protected GoalPoint[] goals;
    protected int[][] grid;

    public TSPGrid() {
        this(htsp.HTSP.DEFAULTWIDTH, htsp.HTSP.DEFAULTHEIGHT);
    }

    public TSPGrid(int width, int height) {
        this(width, height, randomGoals((int)Math.sqrt(width*height), width, height));
    }


    public TSPGrid(GoalPoint[] goals) {
        this.goals = goals;
        int maxX = 0, maxY = 0;
        for (GoalPoint n : goals) {
            if (n.X > maxX) maxX = n.X;
            if (n.Y > maxY) maxY = n.Y;
        }
        grid=new int[maxY+1][];
        for (int i = 0; i<maxY+1; i++) grid[i]=new int[maxX+1];
        this.width = maxX+1;
        this.height = maxY+1;
        for (GoalPoint n : goals) {
            grid[n.Y][n.X]=1;
        }
    }

    public TSPGrid(int width, int height, GoalPoint[] goals) {
        this.width = width;
        this.height = height;
        this.goals = goals;
        //grid=new int[maxY+1][];
        //for (int i = 0; i<maxY+1; i++) grid[i]=new int[maxX+1];
        grid=new int[height][];
        for (int i = 0; i<height; i++) grid[i]=new int[width];
        
        for (GoalPoint n : goals) {
            grid[n.Y][n.X]=1;
        }
    }

    public static boolean exactDistances = false;

    public double distance(int goalIndex1, int goalIndex2) {
        return distance(goals[goalIndex1], goals[goalIndex2]);
    }

    public static double distance(GoalPoint n1, GoalPoint n2) {
        //return Math.round(Math.sqrt(Math.pow(n1.X - n2.X, 2) + Math.pow(n1.Y - n2.Y, 2)));
        return distance(n1.X, n1.Y, n2.X, n2.Y);
    }

    public static double distance(GoalObject n1, GoalObject n2) {
        //return Math.round(Math.sqrt(Math.pow(n1.X - n2.X, 2) + Math.pow(n1.Y - n2.Y, 2)));
        return distance(n1.getOriginalX(), n1.getOriginalY(), n2.getOriginalX(), n2.getOriginalY());
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        if (exactDistances)
            return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        else
            return Math.round(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static double distance(int x1, int y1, int x2, int y2, boolean exactDist) {
        if (exactDist)
            return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        else
            return Math.round(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }
    
    public GoalPoint[] getGoals() {
        return goals;
    }

    public static double pathDistance(GoalObject[] nodes, List<Integer> path) {
        closePath(path);
        double cost = 0;
        for (int i = 1; i < path.size(); i++) {
            cost += distance(nodes[path.get(i)], nodes[path.get(i-1)]);
        }
        return cost;
    }

    public static double pathDistance(GoalPoint[] nodes, List<Integer> path) {
        if (!path.get(0).equals(path.get(path.size() - 1))) path.add(path.get(0));
        double cost = 0;
        for (int i = 1; i < path.size(); i++) {
            cost += distance(nodes[path.get(i)], nodes[path.get(i-1)]);
        }
        return cost;
    }

    public static void closePath(List cnodes) {
        if (cnodes.size() == 1 || cnodes.size() > 1 && !cnodes.get(0).equals(cnodes.get(cnodes.size() - 1)))
            cnodes.add(cnodes.get(0));
    }

    public static double goalObjectPathDistance(List<GoalObject> nodes) {
        if (nodes == null || nodes.size() == 0)
            return 0;
        List<GoalObject> cnodes = new ArrayList<GoalObject>(nodes);
        closePath(cnodes);
        double cost = 0;
        for (int i = 1; i < cnodes.size(); i++) {
            cost += distance(cnodes.get(i), cnodes.get(i-1));
        }
        return cost;
    }

    public static double goalObjectPathDistance(GoalObject[] nodes) {
        if (nodes == null || nodes.length == 0)
            return 0;
        double cost = 0;
        for (int i = 1; i < nodes.length; i++) {
            cost += distance(nodes[i], nodes[i-1]);
        }
        if (nodes[nodes.length-1]!=nodes[0]) cost += distance(nodes[nodes.length-1], nodes[0]);
        return cost;
    }

    public static double goalPathDistance(List<GoalPoint> nodes) {
        List<GoalPoint> cnodes = new ArrayList<GoalPoint>(nodes);
        closePath(cnodes);
        double cost = 0;
        for (int i = 1; i < cnodes.size(); i++) {
            cost += distance(cnodes.get(i), cnodes.get(i-1));
        }
        return cost;
    }

    public static void removeDuplicates(List list) {
        if (list != null && list.size() > 0) {
            List returnList = new ArrayList();
            returnList.add(list.get(0));
            for (int j = 1; j < list.size(); j++) {
                //if (!list.get(j).equals(list.get(j-1)))
                if (!returnList.contains(list.get(j)))
                    returnList.add(list.get(j));
            }
            try {
                list.clear();
                list.addAll(returnList);
            }
            catch (Exception ex) {
                System.err.println("failed to remove duplicates from "+(list.getClass().toString())+" "+list.toString());
            }
        }
    }

    public List<GoalPoint> getNodesFromPath(List<Integer> path) {
        List<GoalPoint> result = new ArrayList<GoalPoint>();
        for (Integer i : path) {
            result.add(goals[i]);
        }
        return result;
    }

    public static GoalPoint[] randomGoalPoints(int number) {
        int size = (int)(1.7 * Math.sqrt(number)); //* 2.5
        return randomGoals(number, size, size);
    }
    
    public static GoalPoint[] randomGoals(int number, int gridWidth, int gridHeight) {
        ArrayList<GoalPoint> nodes = new ArrayList<GoalPoint>();
        for (int i = 0; i < number; i++) {
            GoalPoint n = null;
            while (nodes.contains(
                     n=new GoalPoint((int)(Math.random()*gridWidth), (int)(Math.random()*gridHeight))
                   )
                  );
            nodes.add(n);
        }
        int size = nodes.size();
        GoalPoint[] result = new GoalPoint[size];
        nodes.toArray(result);
        return result;
    }

    public static void exportGoals(GoalPoint[] goals, String path) {
        String header = "NAME : asdf\nCOMMENT : asdf\nTYPE : TSP\nDIMENSION : replacedim\nEDGE_WEIGHT_TYPE : EUC_2D\nNODE_COORD_SECTION\n";
        header = header.replace("replacedim", Integer.toString(goals.length));
        String content = header;
        for (int i = 0; i < goals.length; i++) {
            content += (i+1)+" "+goals[i].getX()+" "+goals[i].getY()+"\n";
        }
        content += "EOF\n";
        try {
            FileWriter f = new FileWriter(path);
            f.write(content);
            f.close();
        }
        catch (Exception ex) {}
    }
}
