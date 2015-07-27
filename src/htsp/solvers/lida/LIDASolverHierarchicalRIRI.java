/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.TspDrawer;
import htsp.solvers.*;
import htsp.solvers.lida.excitation.SigmoidExcitation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Tom
 */
public class LIDASolverHierarchicalRIRI implements Solver {
    public static final double crossHierarchyDistance = 1, GOALAMPLIFICATION = 1;
    public static final int F = 5; //5 factor by which adjacent grid sizes differ (uneven!!!)
    public static final int crossConnectionOverlap = 1; //1 2 nodes on next level are connected to nodes on previous level inside of (F/2+crossConnectionOverlap) distance from center node
    public static final int paddingMultiplier = 2; //each level has (levels - cLevel + 1)*paddingMultiplier padding
    public static final double CROSSLEVELK = 0.5; //1 used in cross level connection weight calculation
    public static final String CROSSCLBL = "CROSSC";

    public static final int CONNECTIONK = 10;

    public static final int BACKCONNECTLIMIT = 3; //10

    public static final double GOALTHRESHOLD = 0.00252;

    HSpatialGrid[] gridHierarchy;
    private int levels;
    private int N; //number of goals
    GoalPoint[] originalGoals;

    TspDrawer drawer, drawer2;
    @Override
    public void setTspDrawer(TspDrawer d) {drawer = d;}

    public void setTspDrawers(TspDrawer d1, TspDrawer d2) {drawer = d1; drawer2 = d2;}
    //^ facilitates visualisation for debugging; can be deleted

    protected int solutionGoalOrder = 0;
    CopyOnWriteArrayList<Integer>[] solutionPaths;
    CopyOnWriteArrayList<GoalObject>[] solutionObjects;
    protected int solutionIterations = 0, subSolutionIterations;
    
    protected int goalminX, goalminY, goalmaxX, goalmaxY;

    boolean wholegrid = false;

    boolean debugging = false;
    boolean removeIntersections = true;
    int maxIntersectionRemovals = 500;

    public LIDASolverHierarchicalRIRI(int width, int height, int maxLevels) {
        this(width, height, TSPGrid.randomGoals((int)Math.sqrt(width*height), width, height), maxLevels);
    }

    public LIDASolverHierarchicalRIRI(int width, int height, GoalPoint[] goals, int maxLevels) {
        init(width, height, goals, maxLevels);
    }

    public LIDASolverHierarchicalRIRI(GoalPoint[] goalpoints, int maxLevels) {
        int maxX = 0, maxY = 0;
        int width, height;
        for (GoalPoint n : goalpoints) {
            if (n.getX() > maxX) maxX = n.getX();
            if (n.getY() > maxY) maxY = n.getY();
        }
        width=maxX+1;
        height=maxY+1;

        init(width, height, goalpoints, maxLevels);
    }

    public Connection getConnection(int x1, int y1, int x2, int y2) {
        return this.getConnection(x1, y1, x2, y2, 0);
    }

    public Connection getConnection(int x1, int y1, int x2, int y2, int level) {
        return gridHierarchy[level].getNode(x1, y1).getConnection(x2, y2);
    }

    public GoalPoint[] getOriginalGoals() {
        return originalGoals;
    }

    public static double getCrosslevelWeight(double distance) {
        return Math.exp(-CROSSLEVELK*distance);
    }

    protected final int getPadding(int level) {
        if (level == 0) return 0;
        else return(levels - level + 1) * paddingMultiplier;
    }

    protected final void init(int width, int height, GoalPoint[] goalpoints, int maxLevels) {
        GridNode.resetNumbering();

        originalGoals = goalpoints;
        N = goalpoints.length;
        levels = (int)(Math.log(N)/Math.log(F)+1); //levels=ceil(log_base_F (N))
        if (levels <= 0) levels = 1;
        if (levels > maxLevels) levels = maxLevels;
        gridHierarchy = new HSpatialGrid[levels];
        solutionPaths = new CopyOnWriteArrayList[levels];
        solutionObjects = new CopyOnWriteArrayList[levels];
        for (int i = 0; i < levels; i++) {
            if (i == 0) gridHierarchy[i] = new HSpatialGrid(width, height, false);
            else {// connect all layers, sigmoidactivation in all layers ((dont connect upper layers; use LinearExcitation))
                //double a = 10;//SigmoidExcitation.defaultA; *(i+1)
                double a = SigmoidExcitation.defaultA*(i+1);
                double b = 0.5*a;
                SigmoidExcitation excitation = new SigmoidExcitation(a, b);
                gridHierarchy[i] = new HSpatialGrid((int)Math.ceil(width / Math.pow(F, i)) + getPadding(i), (int)Math.ceil(height / Math.pow(F, i)) + getPadding(i),
                        false, 0, excitation);
            }
            solutionPaths[i] = new CopyOnWriteArrayList<Integer>();
            solutionObjects[i] = new CopyOnWriteArrayList<GoalObject>();
        }
        // cross-connect hierarchy
        for (int i = 1; i < levels; i++) {
            int pw = gridHierarchy[i-1].getWidth() - getPadding(i-1), ph = gridHierarchy[i-1].getHeight() - getPadding(i-1);
            int cw = gridHierarchy[i].getWidth() - getPadding(i), ch = gridHierarchy[i].getHeight() - getPadding(i);
            for (int j = 0; j < cw; j++) {
                for (int k = 0; k < ch; k++) {
                    //represented area size if scaled 1:1  FxF; real area size: crossConnectionRadius*crossConnectionRadius
                    int crossConnectionSize = F + crossConnectionOverlap*2;
                    int px = j*F+F/2, py = k*F+F/2; //center node in lower layer
                    for (int x = px - crossConnectionSize/2; x <= px + crossConnectionSize/2; x++) {
                        for (int y = py - crossConnectionSize/2; y <= py + crossConnectionSize/2; y++) {
                            try {
                                double distance2d = TSPGrid.distance(px, py, x, y);
                                double distance3d = Math.sqrt(crossHierarchyDistance*crossHierarchyDistance + distance2d*distance2d);
                                double weight = getCrosslevelWeight(distance3d);
                                int tx = x, ty = y;
                                //toroid crossconnection
                                /*if (tx < 0) tx += pw;
                                if (tx >= pw) tx -= pw;
                                if (ty < 0) ty += ph;
                                if (ty >= ph) ty -= ph;*/
                                if (tx >= 0 && ty >= 0 && tx < pw && ty < ph)
                                {
                                    Connection conn = new Connection(weight, distance3d, gridHierarchy[i-1].getNode(tx+getPadding(i-1)/2, ty+getPadding(i-1)/2), gridHierarchy[i].getNode(j+getPadding(i)/2, k+getPadding(i)/2), true);
                                    conn.setCrosshierarchy(true);
                                    gridHierarchy[i-1].addConnections(conn.getSource().getX(), conn.getSource().getY(), new Connection[] {conn});
                                }
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        // add goals to bottom layer gridHierarchy[0].addGoalPoints(goalpoints);

        //add goals
        //gridHierarchy[0].addGoalPoints(goalpoints, HSpatialGrid.DEFAULTGOALACTIVATION);
        setGoalPoints(goalpoints);
        /*
        for (int i = 1; i < gridHierarchy.length; i++) {
            List<GoalPoint> layergoals = new ArrayList<GoalPoint>();
            for (int x = 0; x < gridHierarchy[i].getWidth(); x++) {
                for (int y = 0; y < gridHierarchy[i].getHeight(); y++) {
                    if (gridHierarchy[i].getNode(x, y).getActivation() >= GOALTHRESHOLD) {
                        layergoals.add(new GoalPoint(x, y));
                    }
                    else gridHierarchy[i].getNode(x, y).setActivation(0);
                }
            }
            GoalPoint[] lgoalarray = new GoalPoint[layergoals.size()];
            layergoals.toArray(lgoalarray);
            gridHierarchy[i].addGoalPoints(lgoalarray, (double)HSpatialGrid.DEFAULTGOALACTIVATION / (i+1));
        }
        */

        //debug
        /*for (int i = 1; i < gridHierarchy.length; i++) {
            String grid = "";
            for (int x = 0; x < gridHierarchy[i].getWidth(); x++) {
                for (int y = 0; y < gridHierarchy[i].getHeight(); y++) {
                    grid += (new DecimalFormat("0.00")).format(gridHierarchy[i].getNode(x, y).getActivation())+" ";
                }
                grid+="\n";
            }
            grid +="\n--------------------\n";
            System.out.println(grid);
        }*/
    }

    protected void setGoalPoints(GoalPoint[] goalpoints) {
        //goals "wander" up connections to next level, always choosing highest activation
        gridHierarchy[0].addGoalPoints(goalpoints, HSpatialGrid.DEFAULTGOALACTIVATION);
        for (int i = 1; i < levels; i++) {
            GoalObject[] prevGoals = gridHierarchy[i-1].getGoalObjects();
            //List<GoalPoint> uniquegoallist = new ArrayList<GoalPoint>();
            Map<GoalPoint,List<GoalObject>> uniquegoals = new HashMap<GoalPoint,List<GoalObject>>();
            for (GoalObject go : prevGoals) {
                GridNode prevNode = gridHierarchy[i-1].getNode(go.getCurrentX(), go.getCurrentY());

                GridNode maxNode = null;
                double maxActivation = Double.MIN_VALUE;
                for (Connection c : prevNode.getNeighbors()) {
                    if (c.isCrosshierarchy() && c.getSink().getActivation() > maxActivation) {
                        maxNode = c.getSink();
                        maxActivation = maxNode.getActivation();
                    }
                }

                GoalPoint gp = new GoalPoint(maxNode.getX(), maxNode.getY());
                if (!uniquegoals.keySet().contains(gp)) {
                    List<GoalObject> golist = new ArrayList<GoalObject>();
                    golist.add(go);
                    uniquegoals.put(gp, golist);
                }
                else {
                    uniquegoals.get(gp).add(go);
                }
            }
            GoalObject[] goals = new GoalObject[uniquegoals.size()];
            int j = 0;
            for (GoalPoint gp : uniquegoals.keySet()) {
                goals[j] = new GoalObject(gp.getX(), gp.getY(), gridHierarchy[i].getNode(gp.getX(), gp.getY()).getNumber());
                goals[j].getSubGoalObjects().addAll(uniquegoals.get(gp));
                j++;
            }
            //next level goals determined; reset grid activations
            gridHierarchy[i].reset();
            gridHierarchy[i].addGoalObjects(goals, HSpatialGrid.DEFAULTGOALACTIVATION/(i+1));
        }
    }

    private void rescaleSetGoalPoints(GoalPoint[] goalpoints) {
        for (int i = 0; i < levels; i++) {
            //GoalObject[] goals = new GoalObject[goalpoints.length];
            List<GoalObject> goallist = new ArrayList<GoalObject>();
            for (GoalPoint gp : goalpoints) {
                int tx = (int)(gp.getX() / Math.pow(F, i)), ty = (int)(gp.getY() / Math.pow(F, i));
                if (tx < gridHierarchy[i].getWidth() && ty < gridHierarchy[i].getHeight()) {
                    if (gridHierarchy[i].getNode(tx, ty).getContent().isEmpty()) { //max one goal per node
                        GoalObject g = new GoalObject(tx, ty, gridHierarchy[i].getNode(tx, ty).getNumber());
                        gridHierarchy[i].getNode(tx, ty).addGoal(g, HSpatialGrid.DEFAULTGOALACTIVATION);
                        goallist.add(g);
                    }
                }
            }
            GoalObject[] goals = new GoalObject[goallist.size()];
            goallist.toArray(goals);
            gridHierarchy[i].setGoalObjects(goals);
        }
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
        init(width, height, goals, Integer.MAX_VALUE);
    }

    @Override
    public GoalPoint getGoalPoint(int x, int y) {
        GoalPoint gp = new GoalPoint(x, y, TspDrawer.getColorFromActivation(gridHierarchy[0].getNode(x, y).getActivation()));
        if (!gridHierarchy[0].getNode(x, y).isEmpty())
            gp.setMarked(true);
        return gp;
    }

    public GridNode getNode(int layer, int x, int y) {
        return gridHierarchy[layer].getNode(x, y);
    }

    public HSpatialGrid getGrid(int layer) {return gridHierarchy[layer];}

    @Override
    public int getWidth() {
        return gridHierarchy[0].getWidth();
    }

    @Override
    public int getHeight() {
        return gridHierarchy[0].getHeight();
    }

    public int getWidth(int layer) {return layer<gridHierarchy.length?gridHierarchy[layer].getWidth():0; }
    public int getHeight(int layer) {return layer<gridHierarchy.length?gridHierarchy[layer].getHeight():0; }

    @Override
    public double solve() {
        boolean oExactDist = TSPGrid.exactDistances;
        TSPGrid.exactDistances = true;

        solutionIterations = 0;
        if (GoalPath.debugging) {
            PathConnector.setTspDrawer(drawer);
        }
        /*for (int i = 0; i < levels; i++) {
            solveTsp(gridHierarchy[i], gridHierarchy[i].getGoalObjects(), solutionPaths[i], solutionObjects[i]);
            //backConnect(i);
        }*/
        int j;
        for (j = levels - 1; j >= 0; j--) {
            //solve easy high-level problem
            if (j==levels - 1 || gridHierarchy[j].getGoalObjects().length <= BACKCONNECTLIMIT) {
                solveTsp(gridHierarchy[j], gridHierarchy[j].getGoalObjects(), solutionPaths[j], solutionObjects[j]);
                solutionIterations += subSolutionIterations;
            }
            else {
                j++;
                break;
            }
        }
        for (int i = j; i > 0; i--) {
            //refine high-level solution with low-level sub-tsps
            backConnect(i);
        }

        TSPGrid.closePath(solutionObjects[0]);

        if (removeIntersections) {
            solutionObjects[0] = new CopyOnWriteArrayList(removeIntersections(solutionObjects[0]));
            List<Integer> path = new ArrayList<Integer>();
            for (GoalObject go : solutionObjects[0]) {
                //solutionPaths[level-1].add(goalList.indexOf(go));
                GridNode gn = new GridNode(go.getCurrentX(), go.getCurrentY());
                path.add(gridHierarchy[0].getNodes().indexOf(gn));
            }
            solutionPaths[0] = new CopyOnWriteArrayList(path);
        }

        TSPGrid.exactDistances = oExactDist;
        
        //if (solutionObjects[0].size() == N)
        double cost = TSPGrid.goalObjectPathDistance(solutionObjects[0]);
        System.out.println("Solution cost: "+cost+"; solution iterations: "+solutionIterations);
        return cost;
        //else return 0;
        //return solveLevel(0);
    }

    public List<GoalObject> removeIntersections(List<GoalObject> goalObjectPath) {
        return removeIntersections(goalObjectPath, maxIntersectionRemovals);
    }

    public List<GoalObject> removeIntersections(List<GoalObject> goalObjectPath, int maxRemovals) {
        List<GoalObject> solution = new ArrayList<GoalObject>();
        solution.addAll(goalObjectPath);
        int i = 0;

        while (PathConnector.hasIntersections(solution) && i < maxRemovals) {
            solution = PathConnector.removeIntersection(solution);
            i++;
        }
        if (TSPGrid.goalObjectPathDistance(solution) < TSPGrid.goalObjectPathDistance(goalObjectPath)) {
            return solution;
        }
        return goalObjectPath;
    }

    class ListSizeComparator implements Comparator<List> {
        @Override
        public int compare(List l1, List l2) {
            return l2.size()-l1.size();
        }
    }

    public void backConnect(int level) {
        if (level < 1) return;
        //traverse high level (few) goal objects
        List<Integer> lowerLevelPath = new ArrayList<Integer>();
        List<GoalObject> lowerLevelGoalObjects = new ArrayList<GoalObject>();
        int highLevelGoals = solutionObjects[level].size();
        List<GoalObject>[] subSolutionObjects = new List[highLevelGoals];
        List<Integer>[] subSolutionPaths = new List[highLevelGoals];
        //TODO maybe start with goal having most subgoals?
        if (solutionObjects[level].size() > 1 && solutionObjects[level].get(0) == solutionObjects[level].get(solutionObjects[level].size()-1))
            highLevelGoals--;

        List<List> problematicSubtsps = new ArrayList<List>();
        for (int i = 0; i < highLevelGoals; i++) {
            GoalObject hGoal = solutionObjects[level].get(i);
            //solve TSP with subgoals inside hGoal (on lower level)
            List<Integer> subTspPath = new ArrayList<Integer>();
            List<GoalObject> subTspGoalObjects = new ArrayList<GoalObject>();
            GoalObject[] subTspGoals = new GoalObject[hGoal.getSubGoalObjects().size()];
            hGoal.getSubGoalObjects().toArray(subTspGoals);

            double cost = solveTsp(gridHierarchy[level - 1], subTspGoals, subTspPath, subTspGoalObjects);
            solutionIterations += subSolutionIterations;
            //close path
            TSPGrid.closePath(subTspGoalObjects);
            subSolutionObjects[i] = subTspGoalObjects;
            subSolutionPaths[i] = subTspPath;
            if (i == 0) { //first solution
                lowerLevelPath = subTspPath;
                lowerLevelGoalObjects = subTspGoalObjects;
            }
            else { // connect new low level TSP (subTspPath) to old path (lowerLevelPath)
                //lowerLevelGoalPath.bruteConnectPath(lowerLevelGoalPath, CONNECTIONK);

                List<GoalObject> mergeResult = PathConnector.mergePaths(lowerLevelGoalObjects, subTspGoalObjects, CONNECTIONK);
                mergeResult = removeIntersections(mergeResult);
                //lowerLevelGoalObjects = mergeResult;
                boolean badSolution = PathConnector.hasIntersections(mergeResult);
                if (badSolution) {
                    problematicSubtsps.add(subTspGoalObjects);
                }
                else {
                    lowerLevelGoalObjects = mergeResult;
                }

                //TODO maybe count iterations in merge?
                this.solutionIterations++;
            }

                    //debugging code
                    if (GoalPath.debugging) {
                        drawer.drawGoalObjectPath(lowerLevelGoalObjects, gridHierarchy[level - 1].getNodes());
                        drawer2.drawCircle(hGoal.getCurrentX(), hGoal.getCurrentY(), Color.red, drawer2.getGoalSize()+4);
                        try {
                            Thread.sleep(100);
                        }
                        catch (Exception ex) {}
                    }
        }

        for (List problematicSubtsp : problematicSubtsps) {
            lowerLevelGoalObjects = PathConnector.mergePaths(lowerLevelGoalObjects, problematicSubtsp, CONNECTIONK);

                    //debugging code
                    if (GoalPath.debugging) {
                        drawer.drawGoalObjectPath(lowerLevelGoalObjects, gridHierarchy[level - 1].getNodes());
                        try {
                            Thread.sleep(100);
                        }
                        catch (Exception ex) {}
                    }
        }
       
        solutionObjects[level-1] = new CopyOnWriteArrayList(lowerLevelGoalObjects);
        solutionPaths[level-1] = new CopyOnWriteArrayList<Integer>();
        for (GoalObject go : solutionObjects[level-1]) {
            //solutionPaths[level-1].add(goalList.indexOf(go));
            GridNode gn = new GridNode(go.getCurrentX(), go.getCurrentY());
            solutionPaths[level-1].add(gridHierarchy[level - 1].getNodes().indexOf(gn));
        }
        TSPGrid.closePath(solutionPaths[level-1]);
        if (GoalPath.debugging) {
            drawer.setPath(solutionPaths[level-1]);
            drawer.draw();
        }
    }

    //public void connectSolutions(List<Integer> finalPath, List<GoalObject> finalGoalObjects, List<Integer> oldPath, List<GoalObject> oldObjects, List<Integer> newPath, List<GoalObject> newObjects) {}

    public double solveTsp(HSpatialGrid grid, GoalObject[] goals, List<Integer> resultPath, List<GoalObject> resultObjects) {
        subSolutionIterations = 0;
        try {
            goalminY = goalminX = Integer.MAX_VALUE;
            goalmaxY = goalmaxX = 0;
            for (GoalObject goal : goals) {
                goal.setCurrentX(goal.getOriginalX());
                goal.setCurrentY(goal.getOriginalY());
                if (goal.getCurrentX() < goalminX) goalminX = goal.getCurrentX();
                if (goal.getCurrentX() > goalmaxX) goalmaxX = goal.getCurrentX();
                if (goal.getCurrentY() < goalminY) goalminY = goal.getCurrentY();
                if (goal.getCurrentY() > goalmaxY) goalmaxY = goal.getCurrentY();
                //subSolutionIterations++;
            }
            if (wholegrid) {
                goalminX = goalminY = 0;
                goalmaxX = grid.getWidth() - 1;
                goalmaxY = grid.getHeight() - 1;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return solveTsp(grid, goals, goalminX, goalmaxX, goalminY, goalmaxY, resultPath, resultObjects);
    }

    public double solveTsp(HSpatialGrid grid, GoalObject[] goals, int minX, int maxX, int minY, int maxY, List<Integer> resultPath, List<GoalObject> resultObjects) {
        solutionGoalOrder = 0;
        resultPath.clear();
        resultObjects.clear();
        goalminX = minX;
        goalmaxX = maxX;
        goalminY = minY;
        goalmaxY = maxY;

        int max_iterations = Math.max(grid.getWidth(), grid.getHeight()); //never reached if all goes well

        boolean finished = false;
        int it = 0;
        for (it = 0; it < max_iterations && !finished; it++) { //constant (depends on grid size)
            boolean goaladjusted = false;
            for (GoalObject goal : goals) { //linear with n the number of goals
                if (goal.getCurrentX() == goalminX || goal.getCurrentY() == goalminY || goal.getCurrentX() == goalmaxX || goal.getCurrentY() == goalmaxY) {
                    continue;
                }
                GridNode currentNode = grid.getNode(goal.getCurrentX(), goal.getCurrentY());
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
                    grid.getNode(goal.getCurrentX(), goal.getCurrentY()).removeContent(goal);
                    goal.setCurrentX(targetNode.getX());
                    goal.setCurrentY(targetNode.getY());
                    grid.getNode(goal.getCurrentX(), goal.getCurrentY()).addContent(goal);
                    grid.getNode(goal.getCurrentX(), goal.getCurrentY()).passActivation(HSpatialGrid.DEFAULTGOALINHIBITION);
                    goaladjusted = true;
                    subSolutionIterations++;
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

        //traverse grid edge clockwise from (0,0), set order numbers and put back goal objects to original place
        int fx = goalminX, fy = goalminY;
        for (int i = goalminX; i < goalmaxX; i++) {
            if (!grid.getNode(i, fy).isEmpty())
                processEdgeGoal(i, fy, grid, goals, resultPath);
        }
        fx = goalmaxX;
        for (int i = goalminY; i < goalmaxY; i++) {
            if (!grid.getNode(fx, i).isEmpty())
                processEdgeGoal(fx, i, grid, goals, resultPath);
        }
        fy = goalmaxY;
        for (int i = goalmaxX; i >= goalminX; i--) {
            if (!grid.getNode(i, fy).isEmpty())
                processEdgeGoal(i, fy, grid, goals, resultPath);
        }
        fx = goalminX;
        for (int i = goalmaxY; i >= goalminY; i--) {
            if (!grid.getNode(fx, i).isEmpty())
                processEdgeGoal(fx, i, grid, goals, resultPath);
        }

        //resultObjects contains all goals along best path (but doesnt contain first goal twice)
        for (int i = 0; i < resultPath.size(); i++) {
            //resultObjects.add(goals[(int)resultPath.get(i)]);
            resultObjects.add((GoalObject)grid.getNodes().get((int)resultPath.get(i)).getContent().get(0));
        }

        if (resultObjects.size() > 0 && resultObjects.get(0) != resultObjects.get(resultObjects.size() - 1))
            resultObjects.add(resultObjects.get(0));

        /*if (removeIntersections) {
            int i = 0;
            while (PathConnector.hasIntersections(resultObjects) && i < this.maxIntersectionRemovals) {
                resultObjects = PathConnector.removeIntersections(new CopyOnWriteArrayList(resultObjects));
                i++;
            }
        }*/

        //resultPath contains indices of goals along best path (contains index of first goal twice - back to origin)
        if (resultPath.size() > 0 && resultPath.get(0) != resultPath.get(resultPath.size() - 1))
            resultPath.add(resultPath.get(0));

        if (debugging && drawer != null) {
            //debugging code - show goals not lying on edge
            for (GoalObject goal : goals) {
                if (!(goal.getCurrentX() == goalminX || goal.getCurrentY() == goalminY || goal.getCurrentX() == goalmaxX || goal.getCurrentY() == goalmaxY)) {
                    System.err.println("error in solution: goal hasnt reached border: "+goal.getCurrentX() +" "+ goal.getCurrentY());
                    return 0;
                }
                drawer.addPx(drawer.transformX(goal.getCurrentX(), true), drawer.transformY(goal.getCurrentY(), true));
            }
        }

        //reset goal current coords
        for (GoalObject goal : goals) {
            goal.setCurrentX(goal.getOriginalX());
            goal.setCurrentY(goal.getOriginalY());
        }

        //double cost = TSPGrid.pathDistance(goals, resultPath);
        double cost = TSPGrid.goalObjectPathDistance(resultObjects);
        //System.out.println("Solution cost: "+cost+"; solution iterations: "+solutionIterations);
        return cost;
    }

    @Override
    public int getSolutionIterations() {
        return this.solutionIterations;
    }

    protected void processEdgeGoal(int x, int y, HSpatialGrid grid, GoalObject[] goals, List<Integer> resultPath) {
        try {
            //GoalObject goal = ((GoalObject)grid[y][x].getContent());
            CopyOnWriteArrayList<Object> content = new CopyOnWriteArrayList(grid.getNode(x, y).getContent());
            for (Object o : content) {
                GoalObject goal = (GoalObject)o;
                goal.setOrderNumber(solutionGoalOrder++);
                grid.getNode(x, y).removeContent(goal);
                grid.getNode(goal.getOriginalX(), goal.getOriginalY()).addContent(goal);
                resultPath.add(goal.getOriginalOrderNumber());
                //resultPath.add(Arrays.asList(goals).indexOf(goal)); //TODO solve with originalOrderNumber
                subSolutionIterations++;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //walking clockwise along the outer border of a rectangle, how far is the point
    protected int distanceOnBorderFromCorner(int x, int y, int level) {
        int d = Integer.MAX_VALUE;
        if (y == 0)
            d = x;
        else if (x == gridHierarchy[level].getWidth())
            d = gridHierarchy[level].getWidth() + y;
        else if (y == gridHierarchy[level].getHeight())
            d = gridHierarchy[level].getHeight() + 2 * gridHierarchy[level].getWidth() - x;
        else if (x == 0)
            d = 2 * gridHierarchy[level].getHeight() + 2 * gridHierarchy[level].getWidth() - y;

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
        return gridHierarchy[0].getGoalPoints();
    }

    public GoalPoint[] getGoalpoints(int layer) {
        if (layer < gridHierarchy.length)
            return gridHierarchy[layer].getGoalPoints();
        else return null;
    }

    @Override
    public List<Integer> getSolutionPath() {
        return solutionPaths[0];
    }

    public List<Integer> getSolutionPath(int level) {
        if (level >= solutionPaths.length) return new ArrayList<Integer>();
        return solutionPaths[level];
    }

    public List<Integer> getSolutionPathFromGoalPath(int level) {
        List<Integer> solutionPath = new ArrayList<Integer>();
        List<GoalObject> goalPathList = Arrays.asList(gridHierarchy[level].getGoalObjects());
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
