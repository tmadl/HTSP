/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.TspDrawer;
import htsp.solvers.GoalPoint;
import htsp.solvers.TSPGrid;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.QuickSelect;

/**
 *
 * @author Tom
 */
public class PathConnector {
    private static TspDrawer drawer;

    public static boolean debugging = false;

    public static void setTspDrawer(TspDrawer d) {drawer = d;}

    public static List<GoalObject> nearestMerger(List<GoalObject> path1, List<GoalObject> path2, int k) {
        

        return null;
    }

    public static GoalObject getNearest(GridNode currentNode, GoalObject[] goals, List<GoalObject> excludeList) {
        GoalObject best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < goals.length; i++) {
            if (!excludeList.contains(goals[i])) {
                double distance = TSPGrid.distance(currentNode.getX(), currentNode.getY(), goals[i].getCurrentX(), goals[i].getCurrentY());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = goals[i];
                }
            }
        }

        return best;
    }

    public static List<GoalObject> mergePathsUsingOptions(List<GoalObject> path1, List<GoalObject> path2, List<GoalObject> nearestOptions, int k) {
        /*List<GoalObject> path1 = new ArrayList<GoalObject>(), path2 = new ArrayList<GoalObject>();
        path1.addAll(npath1);
        path2.addAll(npath2);
        if (path1.size() > 1 && path1.get(0).equals(path1.get(path1.size() - 1)))
            path1.remove(path1.size() - 1);
        if (path1.size() > 1 && path1.get(path1.size() - 2).equals(path1.get(path1.size() - 1)))
            path1.remove(path1.size() - 1);
        if (path2.size() > 1 && path2.get(0).equals(path2.get(path2.size() - 1)))
            path2.remove(path2.size() - 1);
        if (path2.size() > 1 && path2.get(path2.size() - 2).equals(path2.get(path2.size() - 1)))
            path2.remove(path2.size() - 1);*/

        if (debugging && drawer != null) {
            for (GoalObject go : path1)
                drawer.drawCircle(go.getOriginalX(), go.getOriginalY(), Color.green, drawer.getGoalSize()+4);
            for (GoalObject go : path2)
                drawer.drawCircle(go.getOriginalX(), go.getOriginalY(), Color.red, drawer.getGoalSize()+4);
        }

        TSPGrid.removeDuplicates(path1);
        TSPGrid.removeDuplicates(path2);
        path2.removeAll(path1);
        if (path2.isEmpty()) return path1;
        //get new TSP centroid O(ns)
        double centX = 0, centY = 0;
        for (GoalObject go : path2) {
            centX += go.getOriginalX();
            centY += go.getOriginalY();
        }
        centX/=path2.size();
        centY/=path2.size();
        //find K nearest goals to centroid O(nl)
        IndexDistancePair[] goalsDistances = new IndexDistancePair[nearestOptions.size()];
        for (int i = 0; i < nearestOptions.size(); i++) {
            //goalsDistances[i]=new IndexDistancePair(i, TSPGrid.distance(centX, centY, go.getCurrentX(), go.getCurrentY()));
            double distance = TSPGrid.distance(centX, centY, nearestOptions.get(i).getOriginalX(), nearestOptions.get(i).getOriginalY());
            goalsDistances[i]=new IndexDistancePair(path1.indexOf(nearestOptions.get(i)), distance);
        }
        QuickSelect.quickSelect(goalsDistances, k);
        int bestMergeIndex1 = -1, bestMergeIndex2 = -1;
        double bestMergeCost = Double.MAX_VALUE, cCost;
        double intersectionPenalty = 0;
        //find best edge to replace by new TSP O(K*ns)
        int n = k<nearestOptions.size()?k:nearestOptions.size();
        List<GoalObject> mergedPath = new ArrayList<GoalObject>();

        TSPGrid.closePath(path1);
        TSPGrid.closePath(path2);

        for (int i = 0; i < n; i++) { //for k nearest goals to centroid in path 1
            //edge after oldGoal
            //TODO maybe also check edge BEFORE node?
            for (int j = 0; j < path2.size(); j++) { // for ns new goals
                //edge after newGoal
                //check whether this is the best insertion
                List<GoalObject> tmpPath = new ArrayList<GoalObject>();
                cCost=mergedPathDistance(path1, path2, goalsDistances[i].index, j, tmpPath);
                boolean hasintersections = PathConnector.hasIntersections(tmpPath);
                if (hasintersections)
                    cCost += intersectionPenalty;
                if (cCost < bestMergeCost) {
                //Path based if ((cCost=GoalPath.mergedPathDistance(path1, path2, path1.get(goalsDistances[i].index), path2.get(j), tmpPath, drawer)) < bestMergeCost) {
                    bestMergeCost = cCost;
                    bestMergeIndex1 = goalsDistances[i].index;
                    bestMergeIndex2 = j;
                    //mergedPath = tmpPath;
                    mergedPath.clear();
                    mergedPath.addAll(tmpPath);
                }
                if (debugging && drawer != null) {
                    drawer.drawGoalObjectPath(tmpPath);
                }
            }
        }
        if (debugging && drawer != null) {
            drawer.drawGoalObjectPath(mergedPath);
        }
        int i = 0;
        for (GoalObject go : mergedPath) {
            go.setOrderNumber(i++);
        }
        return mergedPath;
    }

    public static int mergeIterations = 0;
    public static List<GoalObject> mergePaths(List<GoalObject> path1, List<GoalObject> path2, int k) {
        mergeIterations = 0;
        if (debugging && drawer != null) {
            for (GoalObject go : path1)
                drawer.drawCircle(go.getOriginalX(), go.getOriginalY(), Color.green, drawer.getGoalSize()+4);
            for (GoalObject go : path2)
                drawer.drawCircle(go.getOriginalX(), go.getOriginalY(), Color.red, drawer.getGoalSize()+4);
        }

        TSPGrid.removeDuplicates(path1);
        TSPGrid.removeDuplicates(path2);
        path2.removeAll(path1);
        if (path2.isEmpty()) return path1;
        if (path1.isEmpty()) return path2;
        //get new TSP centroid O(ns)
        double centX = 0, centY = 0;
        for (GoalObject go : path2) {
            centX += go.getOriginalX();
            centY += go.getOriginalY();
        }
        centX/=path2.size();
        centY/=path2.size();
        //find K nearest goals to centroid O(nl)
        IndexDistancePair[] goalsDistances = new IndexDistancePair[path1.size()];
        for (int i = 0; i < path1.size(); i++) {
            //goalsDistances[i]=new IndexDistancePair(i, TSPGrid.distance(centX, centY, go.getCurrentX(), go.getCurrentY()));
            double distance = TSPGrid.distance(centX, centY, path1.get(i).getOriginalX(), path1.get(i).getOriginalY());
            goalsDistances[i]=new IndexDistancePair(i, distance);
        }
        QuickSelect.quickSelect(goalsDistances, k);

        mergeIterations += path1.size();

        int bestMergeIndex1 = -1, bestMergeIndex2 = -1;
        double bestMergeCost = Double.MAX_VALUE, cCost, cCost2;
        double intersectionPenalty = 0;
        //find best edge to replace by new TSP O(K*ns)
        int n = k<path1.size()?k:path1.size();
        List<GoalObject> mergedPath = new ArrayList<GoalObject>();

        TSPGrid.closePath(path1);
        TSPGrid.closePath(path2);

        for (int i = 0; i < n; i++) { //for k nearest goals to centroid in path 1
            //edge after oldGoal
            //TODO maybe also check edge BEFORE node?
            for (int j = 0; j < path2.size(); j++) { // for ns new goals
                //edge after newGoal
                //check whether this is the best insertion
                List<GoalObject> tmpPath = new ArrayList<GoalObject>(), tmpPath2 = new ArrayList<GoalObject>();
                mergeIterations++;
                //int index = (goalsDistances[i].index - 1 + path1.size())%path1.size();
                int index = goalsDistances[i].index;
                int index2 = (goalsDistances[i].index - 1 + path1.size())%path1.size(); //prev insert
                cCost=mergedPathDistance(path1, path2, index, j, tmpPath);
                if (PathConnector.hasIntersections(tmpPath))
                    cCost += intersectionPenalty;
                cCost2=mergedPathDistance(path1, path2, index2, j, tmpPath2);
                if (PathConnector.hasIntersections(tmpPath2))
                    cCost2 += intersectionPenalty;
                if (cCost2 < cCost) {
                    index = index2;
                    cCost = cCost2;
                    tmpPath = tmpPath2;
                }
                if (cCost < bestMergeCost) {
                //Path based if ((cCost=GoalPath.mergedPathDistance(path1, path2, path1.get(goalsDistances[i].index), path2.get(j), tmpPath, drawer)) < bestMergeCost) {
                    bestMergeCost = cCost;
                    bestMergeIndex1 = index;
                    bestMergeIndex2 = j;
                    //mergedPath = tmpPath;

                    mergedPath.clear();
                    mergedPath.addAll(tmpPath);
                }
                if (debugging && drawer != null) {
                    drawer.drawGoalObjectPath(tmpPath);
                }
            }
        }
        if (debugging && drawer != null) {
            drawer.drawGoalObjectPath(mergedPath);
            if (PathConnector.hasIntersections(mergedPath)) {
                boolean intersections = true;
            }
        }
        int i = 0;
        for (GoalObject go : mergedPath) {
            go.setOrderNumber(i++);
        }
        return mergedPath;
    }

    //TODO !test whether this works
    public static double mergedPathDistance(List<GoalObject> path1, List<GoalObject> path2, int index1, int index2, List<GoalObject> mergedPath) {
        //throw new Exception("Use GoalPath.mergedPathDistance");
        int p1o = -1, p2o = -1;
        double distance = 0;
        GoalObject currentGoal = null, nextGoal;

        Color c = Color.green;
        if (debugging && drawer != null) {
            for (GoalObject go : path1)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), c, drawer.getGoalSize()+8);
            c = Color.red;
            for (GoalObject go : path2)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), c, drawer.getGoalSize()+8);

            c = Color.blue;
            drawer.drawCircle(path1.get((index1+path1.size())%path1.size()).getCurrentX(), path1.get((index1+path1.size())%path1.size()).getCurrentY(), c, drawer.getGoalSize()+11);
            drawer.drawCircle(path2.get((index2+path2.size())%path2.size()).getCurrentX(), path2.get((index2+path2.size())%path2.size()).getCurrentY(), c, drawer.getGoalSize()+11);
        }

        c = Color.black;
        for (int i = 0; i < path1.size() + path2.size(); i++) {
            if (debugging && currentGoal != null && drawer != null)
                drawer.drawCircle(currentGoal.getCurrentX(), currentGoal.getCurrentY(), c, drawer.getGoalSize()+8);
            if (mergedPath != null && currentGoal != null) mergedPath.add(currentGoal);
            if (i <= index1) {
                nextGoal = path1.get((i+path1.size())%path1.size());
                if (currentGoal != null) distance += TSPGrid.distance(currentGoal, nextGoal);
                currentGoal = nextGoal;
            }
            else if (i <= index1+path2.size()) {
                if (p2o < 0) p2o = i;
                nextGoal = path2.get((i - p2o + index2+path2.size())%path2.size());
                if (currentGoal != null) distance += TSPGrid.distance(currentGoal, nextGoal);
                currentGoal = nextGoal;
            }
            else {
                if (p1o < 0) p1o = i;
                nextGoal = path1.get((i - p1o + index1 + 1 + path1.size())%path1.size());
                if (currentGoal != null) distance += TSPGrid.distance(currentGoal, nextGoal);
                currentGoal = nextGoal;
            }
        }
        if (mergedPath != null) mergedPath.add(currentGoal);

        TSPGrid.removeDuplicates(mergedPath);

        //also count distance back to starting point
        distance += TSPGrid.distance(currentGoal, path1.get(0));
        TSPGrid.closePath(mergedPath);

        double d;
        if (distance != (d=TSPGrid.goalObjectPathDistance(mergedPath))) {
            //System.err.println("wrong distance in merged path");
            distance = d;
        }

/*
         //remove duplicates
        if (mergedPath != null) {
            List<GoalObject> returnPath = null;
            returnPath = new ArrayList<GoalObject>();
            returnPath.add(mergedPath.get(0));
            for (int j = 1; j < mergedPath.size(); j++) {
                if (!mergedPath.get(j).equals(mergedPath.get(j-1)))
                    returnPath.add(mergedPath.get(j));
            }
            //if (!returnPath.get(0).equals(returnPath.get(returnPath.size()-1))) returnPath.add(returnPath.get(0));
            mergedPath = returnPath;
        }*/
        return distance;
    }

    //k nodes around path2 centroid considered
    //z max path1 nodes inserted into path2 considered
    public static List<GoalObject> mergePathsMultiedge(List<GoalObject> path1, List<GoalObject> path2, int k, int z) {
        if (debugging && drawer != null) {
            for (GoalObject go : path1)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), Color.green, drawer.getGoalSize()+4);
            for (GoalObject go : path2)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), Color.red, drawer.getGoalSize()+4);
        }

        TSPGrid.removeDuplicates(path1);
        TSPGrid.removeDuplicates(path2);
        path2.removeAll(path1);
        if (path2.isEmpty()) return path1;
        //get new TSP centroid O(ns)
        double centX = 0, centY = 0;
        for (GoalObject go : path2) {
            centX += go.getCurrentX();
            centY += go.getCurrentY();
        }
        centX/=path2.size();
        centY/=path2.size();
        //find K nearest goals to centroid O(nl)
        IndexDistancePair[] goalsDistances = new IndexDistancePair[path1.size()];
        for (int i = 0; i < path1.size(); i++) {
            //goalsDistances[i]=new IndexDistancePair(i, TSPGrid.distance(centX, centY, go.getCurrentX(), go.getCurrentY()));
            double distance = TSPGrid.distance(centX, centY, path1.get(i).getCurrentX(), path1.get(i).getCurrentY());
            goalsDistances[i]=new IndexDistancePair(i, distance);
        }
        QuickSelect.quickSelect(goalsDistances, k);
        double bestMergeCost = Double.MAX_VALUE, cCost, cCost2;
        double intersectionPenalty = 0;
        //find best edge to replace by new TSP O(K*ns)
        int n = k<path1.size()?k:path1.size();
        List<GoalObject> mergedPath = new ArrayList<GoalObject>(), bestMergedPath = new ArrayList<GoalObject>();
        for (int i = 0; i < n; i++) { //for k nearest goals to centroid in path 1
            //edge after oldGoal
            //TODO maybe also check edge BEFORE node?
            int[] connectionsInPath2 = new int[path2.size()], tmpConnections;
            for (int j = 0; j < path2.size(); j++) {
                connectionsInPath2[j] = 0;
            }
            for (int j = 0; j < path2.size(); j++) { // for ns new goals
                int localBestIndex = -1;
                double localBestCost = Double.MAX_VALUE;
                for (int l = 0; l < path2.size(); l++) { // for ns new goals
                    //edge after newGoal
                    //check whether this is the best insertion
                    //check whether (inserting path1 after l) or (inserting path2) or (inserting n path1 nodes then path 2) is best
                    for (int m = 0; m < z; m++) {
                        List<GoalObject> tmpPath = new ArrayList<GoalObject>(), tmpPath2 = new ArrayList<GoalObject>();
                        tmpConnections = connectionsInPath2.clone();
                        tmpConnections[l] = m;
                        int index = goalsDistances[i].index;
                        int index2= (goalsDistances[i].index - 1 + path1.size())%path1.size();
                        cCost=mergedPathDistanceMultiedge(path1, path2, index, j, tmpConnections, tmpPath);
                        if (PathConnector.hasIntersections(tmpPath))
                            cCost+=intersectionPenalty;//0.1;
                        cCost2=mergedPathDistanceMultiedge(path1, path2, index2, j, tmpConnections, tmpPath2);
                        if (PathConnector.hasIntersections(tmpPath))
                            cCost2+=intersectionPenalty;//0.1;
                        if (cCost2 < cCost) {
                            index = index2;
                            tmpPath = tmpPath2;
                            cCost = cCost2;
                        }
                        if (cCost < localBestCost) {
                            localBestCost = cCost;
                            localBestIndex = l;
                            mergedPath = tmpPath;
                        }
                        /*if (debugging && drawer != null) {
                            drawer.drawGoalObjectPath(tmpPath);
                        }*/
                    }
                }
                connectionsInPath2[localBestIndex] = 1;
                if (localBestCost < bestMergeCost) {
                    bestMergeCost = localBestCost;
                    bestMergedPath = mergedPath;
                        if (debugging && drawer != null) {
                            drawer.drawGoalObjectPath(bestMergedPath);
                        }
                }
            }
        }
        if (debugging && drawer != null) {
            drawer.drawGoalObjectPath(bestMergedPath);
            for (GoalObject go : path1)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), Color.green, drawer.getGoalSize()+4);
            for (GoalObject go : path2)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), Color.red, drawer.getGoalSize()+4);
        }
        int i = 0;
        for (GoalObject go : bestMergedPath) {
            go.setOrderNumber(i++);
        }
        return bestMergedPath;
    }


    //TODO !test whether this works
    /*in connectionsInPath2:
     0 means the next node in path2 is inserted,
     n>0 means that n nodes from path 1 are inserted before the next node in path2 is inserted
     */
    public static double mergedPathDistanceMultiedge(List<GoalObject> path1, List<GoalObject> path2, int index1, int index2, int[] connectionsInPath2, List<GoalObject> mergedPath) {
        //throw new Exception("Use GoalPath.mergedPathDistance");
        double distance = 0;
        GoalObject currentGoal = path1.get(0), nextGoal;

        Color c = Color.green;
        if (debugging) {
            for (GoalObject go : path1)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), c, drawer.getGoalSize()+8);
            c = Color.red;
            for (GoalObject go : path2)
                drawer.drawCircle(go.getCurrentX(), go.getCurrentY(), c, drawer.getGoalSize()+8);

            c = Color.blue;
            drawer.drawCircle(path1.get(index1).getCurrentX(), path1.get(index1).getCurrentY(), c, drawer.getGoalSize()+11);
            drawer.drawCircle(path2.get(index2).getCurrentX(), path2.get(index2).getCurrentY(), c, drawer.getGoalSize()+11);
            //for (int i = 0; i < connectionsInPath2.length; i++)
            //    drawer.drawCircle(path2.get(i).getCurrentX(), path2.get(i).getCurrentY(), c, drawer.getGoalSize()+11);
        }

        c = Color.black;
        int p1i = 0, p2i = 0, ci = 0;
        for (int i = 1; p1i < path1.size() || p2i < path2.size(); i++) {
            if (debugging && currentGoal != null)
                drawer.drawCircle(currentGoal.getCurrentX(), currentGoal.getCurrentY(), c, drawer.getGoalSize()+8);
            if (mergedPath != null && currentGoal != null) mergedPath.add(currentGoal);
            if (i <= index1) {
                nextGoal = path1.get(i);
                distance += TSPGrid.distance(currentGoal, nextGoal);
                currentGoal = nextGoal;
            }
            else if (i <= index1+path2.size()) {
                nextGoal = path2.get((p2i + index2 + path2.size())%path2.size());
                distance += TSPGrid.distance(currentGoal, nextGoal);
                currentGoal = nextGoal;
                if (mergedPath != null) mergedPath.add(currentGoal);
                p2i++;
                //also insert n nodes from path1
                int n = connectionsInPath2[ci];
                for (int j = 0; j < n && p1i < path1.size() - 1; j++) {
                    p1i++;
                    nextGoal = path1.get((p1i + index1 + path1.size())%path1.size());
                    distance += TSPGrid.distance(currentGoal, nextGoal);
                    currentGoal = nextGoal;
                    if (mergedPath != null) mergedPath.add(currentGoal);
                }
                mergedPath.remove(mergedPath.size() - 1); //currentGoal will be added again at the next iteration
            }
            else {
                p1i++;
                nextGoal = path1.get((p1i + index1 + path1.size())%path1.size());
                if (!mergedPath.contains(nextGoal)) {
                    distance += TSPGrid.distance(currentGoal, nextGoal);
                    currentGoal = nextGoal;
                }
            }
            /*
            else if (i <= index1+path2.size()) {
                if (p2o < 0) p2o = i;
                nextGoal = path2.get((i - p2o + index2+path2.size())%path2.size());
                distance += TSPGrid.distance(currentGoal, nextGoal);
                currentGoal = nextGoal;
            }
            else {
                if (p1o < 0) p1o = i;
                nextGoal = path1.get((i - p1o + index1 + 1 + path1.size())%path1.size());
                distance += TSPGrid.distance(currentGoal, nextGoal);
                currentGoal = nextGoal;
            }*/
        }
        if (mergedPath != null && mergedPath.get(mergedPath.size() - 1) != currentGoal) mergedPath.add(currentGoal);

        //also count distance back to starting point
        distance += TSPGrid.distance(currentGoal, path1.get(0));
        TSPGrid.closePath(mergedPath);

        double d;
        if (distance != (d=TSPGrid.goalObjectPathDistance(mergedPath))) {
            System.err.println("wrong distance in merged path");
            distance = d;
        }

        return distance;
    }

    public static List<GoalObject> removeIntersection(List<GoalObject> goalPath) {
//        return GoalPath.removeIntersections(goalPath);
        List<GoalPoint> swaps = new ArrayList<GoalPoint>();
        List<GoalPoint> doneswaps = new ArrayList<GoalPoint>();
        GoalObject[] returnPath = new GoalObject[goalPath.size()];
        goalPath.toArray(returnPath);
        for (int i = 1; i < goalPath.size(); i++) {
            Edge edge = new Edge(goalPath.get(i-1), goalPath.get(i));
            for (int j = i+2; j < goalPath.size(); j++) {
                Edge cedge = new Edge(goalPath.get(j-1), goalPath.get(j));
                if (edge.intersects(cedge)) { //intersection found, swap
                    swaps.add(new GoalPoint(i, j-1));
                    i = goalPath.size();
                        break;
                }
            }
        }
        for (int i = 0; i < swaps.size(); i++) {
            GoalPoint swap = swaps.get(i);
            if (!doneswaps.contains(swap)) {
                GoalObject tmp = returnPath[swaps.get(i).getX()];
                returnPath[swaps.get(i).getX()]=returnPath[swaps.get(i).getY()];
                returnPath[swaps.get(i).getY()]=tmp;
                doneswaps.add(swap);
            }
        }
        return Arrays.asList(returnPath);
    }


    public static boolean hasIntersections(List<GoalObject> goalPath) {
        for (int i = 1; i < goalPath.size(); i++) {
            Edge edge = new Edge(goalPath.get(i-1), goalPath.get(i));
            for (int j = 1; j < goalPath.size(); j++) {
                Edge cedge = new Edge(goalPath.get(j-1), goalPath.get(j));
                if (edge.intersects(cedge)) { //intersection found
                    boolean b = edge.intersects(cedge);
                    return true;
                }
            }
        }
        return false;
    }
}


