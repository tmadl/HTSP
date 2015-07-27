/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.TspDrawer;
import htsp.solvers.TSPGrid;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 * @author Tom
 */
public class GoalPath {
    //protected List<Edge> graph = new ArrayList<Edge>();
    protected Map<GoalObject,Set<Edge>> graph = new ConcurrentHashMap<GoalObject,Set<Edge>>(); // new representation - assign edges to goal objects
    protected boolean graphIsClosed;
    protected GoalObject lastGoal = null;
    protected List<GoalObject> goalObjectPath = new ArrayList<GoalObject>();
    protected int lastOrderNumber = 0;

    protected TspDrawer drawer;
    
    public static boolean debugging = true;
    public static int debugSleepTime = 0;

    public GoalPath(GoalPath copy) {
        //graph.addAll(copy.graph);
        for (GoalObject go : copy.graph.keySet())
            graph.put(go.clone(), new CopyOnWriteArraySet<Edge>(copy.graph.get(go)));
    }

    public GoalPath(List<GoalObject> goalObjectPath) {
        this.goalObjectPath.clear();
        //this.goalObjectPath.addAll(goalObjectPath);
        for (GoalObject go : goalObjectPath)
            if (!this.goalObjectPath.contains(go))
                this.goalObjectPath.add(go);
        //this.goalObjectPath.add(this.goalObjectPath.get(this.goalObjectPath.size() - 1));
        TSPGrid.closePath(this.goalObjectPath);
        int length = this.goalObjectPath.size() - 1;
        for (int i = 0; i < length; i++) {
            //graph.add(new Edge(goalObjectPath.get(i-1), goalObjectPath.get(i)));
            int next = i+1, prev = i - 1;
            if (i >= length - 1) next = 0;
            if (i == 0) prev = length - 1;
            Set<Edge> edgeSet = new CopyOnWriteArraySet<Edge>();
            edgeSet.add(new Edge(this.goalObjectPath.get(i), this.goalObjectPath.get(prev), -1));
            edgeSet.add(new Edge(this.goalObjectPath.get(i), this.goalObjectPath.get(next), 1));
            graph.put(new GoalObject(this.goalObjectPath.get(i).getOriginalX(), this.goalObjectPath.get(i).getOriginalY(), i), edgeSet);
        }
        lastOrderNumber = length;
        graphIsClosed = true;
    }

    public List<GoalObject> swapEdges(Edge e1, Edge e2) {
        return swapEdges(e1, e2, false);
    }

    public List<GoalObject> swapEdges(Edge e1, Edge e2, boolean flip) {
        double cost = TSPGrid.goalObjectPathDistance(getGoalObjectPath());
        List<GoalObject> oldgoals = new ArrayList<GoalObject>(this.getGraph().keySet());
        List<GoalObject> candidates = new ArrayList<GoalObject>();
        candidates.add(e1.getSource());
        candidates.add(e1.getSink());
        candidates.add(e2.getSource());
        candidates.add(e2.getSink());
        GoalPath bestgp = new GoalPath(this), cgp;
        double bestcost = cost, ccost;

        for (int e1s = 0; e1s < 4; e1s++) {
            cgp = new GoalPath(this);
            cgp.removeEdges(e1.getSource(), e1.getSink());
            Set<Edge> edges = cgp.getGraph().get(candidates.get(e1s));
            Edge ed = new Edge(candidates.get(e1s), e1.getSink(), flip?-1:1);
            this.drawEdge(ed, Color.green);
            edges.add(ed);
            cgp.getGraph().put(candidates.get(e1s), edges);
            for (int e2s = 0; e2s < 4; e2s++) {
                GoalPath tmpgp = new GoalPath(cgp);
                tmpgp.removeEdges(e2.getSource(), e2.getSink());
                Set<Edge> edges2 = tmpgp.getGraph().get(candidates.get(e2s));
                Edge ed2=new Edge(candidates.get(e2s), e2.getSink(), flip?-1:1);
                this.drawEdge(ed2, Color.red);
                edges2.add(ed2);
                tmpgp.getGraph().put(candidates.get(e2s), edges2);

                List<GoalObject> goals = tmpgp.getGoalObjectPath();
                TSPGrid.removeDuplicates(goals);
                List<GoalObject> e = new ArrayList<GoalObject>(oldgoals);
                e.removeAll(goals);
                if (goals.size() >= oldgoals.size() && e.isEmpty())
                    ccost=TSPGrid.goalObjectPathDistance(goals);
                else ccost = Double.MAX_VALUE;
                if (ccost<bestcost) {
                    bestgp = tmpgp;
                    bestcost = ccost;
                }
            }
            for (int e2e = 0; e2e < 4; e2e++) {
                GoalPath tmpgp = new GoalPath(cgp);
                tmpgp.removeEdges(e2.getSource(), e2.getSink());
                Set<Edge> edges2 = tmpgp.getGraph().get(e2.getSource());
                Edge ed2 = new Edge(e2.getSource(), candidates.get(e2e), flip?-1:1);
                this.drawEdge(ed2, Color.red);
                edges2.add(ed2);
                tmpgp.getGraph().put(e2.getSource(), edges2);

                List<GoalObject> goals = tmpgp.getGoalObjectPath();
                TSPGrid.removeDuplicates(goals);
                List<GoalObject> e = new ArrayList<GoalObject>(oldgoals);
                e.removeAll(goals);
                if (goals.size() >= oldgoals.size() && e.isEmpty())
                    ccost=TSPGrid.goalObjectPathDistance(goals);
                else ccost = Double.MAX_VALUE;
                if (ccost<bestcost) {
                    bestgp = tmpgp;
                    bestcost = ccost;
                }
            }
        }
        for (int e1e = 0; e1e < 4; e1e++) {
            cgp = new GoalPath(this);
            cgp.removeEdges(e1.getSource(), e1.getSink());
            Set<Edge> edges = cgp.getGraph().get(e1.getSource());
            Edge ed = new Edge(e1.getSource(), candidates.get(e1e), flip?-1:1);
            this.drawEdge(ed, Color.green);
            edges.add(ed);
            cgp.getGraph().put(e1.getSource(), edges);
            for (int e2s = 0; e2s < 4; e2s++) {
                GoalPath tmpgp = new GoalPath(cgp);
                tmpgp.removeEdges(e2.getSource(), e2.getSink());
                Set<Edge> edges2 = tmpgp.getGraph().get(candidates.get(e2s));
                Edge ed2 = new Edge(candidates.get(e2s), e2.getSink(), flip?-1:1);
                this.drawEdge(ed2, Color.red);
                edges2.add(ed2);
                tmpgp.getGraph().put(candidates.get(e2s), edges2);

                List<GoalObject> goals = tmpgp.getGoalObjectPath();
                TSPGrid.removeDuplicates(goals);
                List<GoalObject> e = new ArrayList<GoalObject>(oldgoals);
                e.removeAll(goals);
                if (goals.size() >= oldgoals.size() && e.isEmpty())
                    ccost=TSPGrid.goalObjectPathDistance(goals);
                else ccost = Double.MAX_VALUE;
                if (ccost<bestcost) {
                    bestgp = tmpgp;
                    bestcost = ccost;
                }
            }
            for (int e2e = 0; e2e < 4; e2e++) {
                GoalPath tmpgp = new GoalPath(cgp);
                tmpgp.removeEdges(e2.getSource(), e2.getSink());
                Set<Edge> edges2 = tmpgp.getGraph().get(e2.getSource());
                Edge ed2 = new Edge(e2.getSource(), candidates.get(e2e), flip?-1:1);
                this.drawEdge(ed2, Color.red);
                edges2.add(ed2);
                tmpgp.getGraph().put(e2.getSource(), edges2);

                List<GoalObject> goals = tmpgp.getGoalObjectPath();
                TSPGrid.removeDuplicates(goals);
                List<GoalObject> e = new ArrayList<GoalObject>(oldgoals);
                e.removeAll(goals);
                if (goals.size() >= oldgoals.size() && e.isEmpty())
                    ccost=TSPGrid.goalObjectPathDistance(goals);
                else ccost = Double.MAX_VALUE;
                if (ccost<bestcost) {
                    bestgp = tmpgp;
                    bestcost = ccost;
                }
            }
        }

        List<GoalObject> goalObjects = bestgp.getGoalObjectPath();
        TSPGrid.removeDuplicates(goalObjects);
        TSPGrid.closePath(goalObjects);

        if (!flip) {
            List<GoalObject> flippedGoals = swapEdges(e1, e2, true);
            TSPGrid.removeDuplicates(flippedGoals);
            List<GoalObject> e = new ArrayList<GoalObject>(getGraph().keySet());
            e.removeAll(flippedGoals);
            double fcost = Double.MAX_VALUE;
            if (flippedGoals.size() >= getGraph().keySet().size() && e.isEmpty())
                fcost=TSPGrid.goalObjectPathDistance(flippedGoals);
            if (fcost < TSPGrid.goalObjectPathDistance(goalObjects) || flippedGoals.size() > goalObjects.size()) {
                goalObjects = flippedGoals;
            }
        }

        return goalObjects;
    }

    public Map<GoalObject, Set<Edge>> getGraph() {
        return graph;
    }

    protected void addGoalToPath(GoalObject goal) {
        if (!goalObjectPath.contains(goal)) {
            goalObjectPath.add(goal);
            for (Edge e : graph.get(goal)) {
                if (e.getDirection() >= 0) {
                    addGoalToPath(e.getSink());
                }
            }
        }
    }

    public List<GoalObject> getGoalObjectPath() {
        goalObjectPath = new ArrayList<GoalObject>();
        GoalObject cgo = graph.keySet().iterator().next();
        while (goalObjectPath.size() < graph.keySet().size() && cgo != null) {
            if (debugging)
                if (drawer != null && cgo != null)
                    drawer.drawCircle(cgo.getCurrentX(), cgo.getCurrentY(), Color.black);
            goalObjectPath.add(cgo);
            Set<Edge> edges = graph.get(cgo);
            cgo = null;
            for (Edge e : edges) {
                if (!goalObjectPath.contains(e.getSink())) {
                    cgo = e.getSink();
                    break;
                }
            }
        }
        TSPGrid.closePath(goalObjectPath);
        return goalObjectPath;
    }

    /*public List<GoalObject> getGoalObjectPath() {
        //addGoalToPath(graph.keySet().iterator().next());
        lastGoal = getLastGoal();
        if (lastGoal == null && !graphIsClosed) { //no lastGoal found, or multiple "lastGoals" (loose ends)
            if (lastGoals.size() == 2) {
                int direction = 1;
                if (lastGoals.get(0).getOrderNumber() < lastGoals.get(1).getOrderNumber()) direction = -1;
                graph.get(lastGoals.get(0)).add(new Edge(lastGoals.get(0), lastGoals.get(1), direction));
                graph.get(lastGoals.get(1)).add(new Edge(lastGoals.get(1), lastGoals.get(0), -direction));
                if (debugging) drawEdge(new Edge(lastGoals.get(0), lastGoals.get(1), 1), Color.MAGENTA);
            }
            else {
                System.err.println("failed to find lastgoal - goal object path will be wrong");
            }
            graphIsClosed = true;
        }

        goalObjectPath = new ArrayList<GoalObject>(graph.keySet().size());
        GoalObject cgo = graph.keySet().iterator().next();
        goalObjectPath.add(cgo);
        int cDirection = 1, directionChanges = 0;
        while (true) {
            GoalObject nextGoal = null;
            for (Edge e : graph.get(cgo)) {
                if ((e.getDirection() == cDirection || e.getDirection() == 0) && !e.getSink().equals(e.getSource()) && !goalObjectPath.contains(e.getSink())) {
                    nextGoal = e.getSink();
                    directionChanges = 0;
                    break;
                }
            }
            if (nextGoal == null) {
                if (directionChanges++ <= 1)
                    cDirection*=-1;
                else
                    break;
            }
            else {
                // A B| A C <- ignore second A
                cgo = nextGoal;
                if (goalObjectPath.size() < 2 || !goalObjectPath.get(goalObjectPath.size()-2).equals(cgo)) {
                    goalObjectPath.add(cgo);
                }
            }
        }

        TSPGrid.removeDuplicates(goalObjectPath);
        TSPGrid.closePath(goalObjectPath);
        
        return goalObjectPath;
    }*/

    public GoalObject connectPath(List<GoalObject> newGoalObjectPath, boolean isLast) {
        return connectPath(new GoalPath(newGoalObjectPath), isLast);
    }

    public boolean hasConnection(Edge connection) {
        for (GoalObject go : graph.keySet()) {
            GoalObject edgesHaveGoal = null;
            if (go.equals(connection.getSource()))
                edgesHaveGoal = connection.getSink();
            if (go.equals(connection.getSink()))
                edgesHaveGoal = connection.getSource();
            if (edgesHaveGoal != null) {
                for (Edge e : graph.get(go)) {
                    if (e.getSink().equals(edgesHaveGoal))
                        return true;
                }
            }
        }
        return false;
    }
/*
    public void bruteConnectPath(List<GoalObject> newGoalObjectPath, int k) {
        bruteConnectPath(new GoalPath(newGoalObjectPath), k);
    }

    /*
    merging approach see Mulder & Wunsch 2003 "Million city traveling salesman problem solution by divide and conquer clustering with adaptive resonance neural networks"
     * /
    public void bruteConnectPath(GoalPath newGoalObjectPath, int k) {
        //get new TSP centroid O(ns)
        double centX = 0, centY = 0;
        for (GoalObject go : newGoalObjectPath.getGraph().keySet()) {
            centX += go.getCurrentX();
            centY += go.getCurrentY();
        }
        centX/=newGoalObjectPath.getGraph().keySet().size();
        centY/=newGoalObjectPath.getGraph().keySet().size();
        //find K nearest goals to centroid O(nl)
        GoalDistancePair[] goalsDistances = new GoalDistancePair[graph.keySet().size()];
        int gi = 0;
        for (GoalObject go : graph.keySet()) {
            goalsDistances[gi++]=(new GoalDistancePair(go, TSPGrid.distance(centX, centY, go.getCurrentX(), go.getCurrentY())));
        }
        QuickSelect.quickSelect(goalsDistances, k);
        GoalPath bestMerge = null, currentMerge = null;
        double bestMergeCost = Double.MAX_VALUE;
        //find best edge to replace by new TSP O(K*ns)
        for (int i = 0; i < k; i++) { //for k nearest goals to centroid
            for (GoalObject newGoal : newGoalObjectPath.getGraph().keySet()) { // for ns new goals
                for (Edge e : newGoalObjectPath.getGraph().get(newGoal)) {
                    //check whether this is the best insertion
                    currentMerge = GoalPath.merge(this, newGoalObjectPath, e);
                }
            }
        }
    }

    //merge two complete paths (TSPs) along specified edge
    public static GoalPath merge(GoalPath path1, GoalPath path2, Edge edge) {

    }*/

    //connects two graphs (using end goals or closest goals) then returns end goal of connected graph
    public GoalObject connectPath(GoalPath newGoalObjectPath, boolean isLast) { //if isLast is true, try to establish correct circular connection
        //connect - see _backconnection.png ALTERNATIVE
        GoalObject C1 = null, C2 = null;

        if (!newGoalObjectPath.graphIsClosed && newGoalObjectPath.lastGoal != null)
            C2 = newGoalObjectPath.lastGoal;
        
        if ((this.lastGoal = getLastGoal()) != null) {
            C1 = this.lastGoal;
        }

        Edge connection = null;
        if (this.lastGoals.size() > 1) {
            //more than 1 end, connect to nearest
            boolean odbg = debugging;
            debugging = false;
            Edge bestConnection = null, currentConnection;
            for (GoalObject go : this.lastGoals) {
                 currentConnection = getPathConnection(newGoalObjectPath, go, C2);
                 if (bestConnection == null || currentConnection != null && currentConnection.getLength() < bestConnection.getLength())
                     bestConnection = currentConnection;
            }
            connection = bestConnection;
            debugging = odbg;
            if (debugging)
                drawEdge(connection);
        }
        else {
            connection = getPathConnection(newGoalObjectPath, C1, C2);
        }
        
        if (connection == null) {
            System.err.println("failed to find path connection");
        }
        else if (hasConnection(connection)) {
            System.err.println("connection exists already");
        }
        else {
            C1 = connection.getSource();
            C2 = connection.getSink();
            //add goals and edges
            for (GoalObject newGoal : newGoalObjectPath.graph.keySet()) {
                if (!graph.keySet().contains(newGoal))
                    graph.put(new GoalObject(newGoal.getOriginalX(), newGoal.getOriginalY(), lastOrderNumber++), newGoalObjectPath.graph.get(newGoal));
            }
            if (newGoalObjectPath.graphIsClosed) { //added subgraph was closed and C2 has >1 edge, prune C2's longest edge
                pruneLongestEdge(C2, isLast);
            }
            //add novel edges between C1 and C2
            graph.get(C2).add(new Edge(C2, C1, -1));
            graph.get(C1).add(new Edge(C1, C2, 1));
        }
        lastGoal = getLastGoal();
        if (lastGoal == null) { //no lastGoal found
            System.err.println("failed to find lastgoal");
            this.graphIsClosed = false;
        }
        drawGoalPath();
        return lastGoal;
    }

    protected void pruneLongestEdge(GoalObject C2, boolean isLast) { //if last, close loop without leaving goals "hanging" on just one edge
        Edge maxEdge = null;
        if (graph.get(C2).size() > 1 && this.getNonzeroEdges(C2) > 1) {
            //remove longest edge if C2 has more than 1 edge longer than 0 (not to itself)
            for (Edge e : graph.get(C2)) {
                if ((maxEdge == null || e.getLength() >  maxEdge.getLength()) && //first edge if all are equal
                        (!isLast || graph.get(e.getSink()).size() > 2)) //if last, make sure more than two edges
                    maxEdge = e;
            }
            if (maxEdge == null) { //none of the edges satisfy the islast condition
                for (Edge e : graph.get(C2)) {
                    if (maxEdge == null || e.getLength() >  maxEdge.getLength()) //first edge if all are equal
                        maxEdge = e;
                }
            }
            //remove all occurences of R1<-maxEdge->R2, in any direction
            graph.get(maxEdge.getSource()).remove(maxEdge);
            maxEdge.invert();
            graph.get(maxEdge.getSource()).remove(maxEdge);
            maxEdge.invert();
            if (debugging) drawEdge(maxEdge, Color.red);
            //this.graphIsClosed = false;
        }
    }

    public static void sleep() {
        try {
            if (debugging)
                Thread.sleep(debugSleepTime);
        }
        catch (Exception ex) {}
    }

    protected int getNonzeroEdges(GoalObject go) {
        if (!graph.containsKey(go)) return 0;
        int nonzeroedges = 0;
        List<Edge> checkedEdges = new ArrayList<Edge>();
        for (Edge e : graph.get(go)) {
            if (e!=null && e.getLength() > 0 && !checkedEdges.contains(e)) {
                nonzeroedges++;
                checkedEdges.add(e);
            }
        }
        return nonzeroedges;
    }

    List<GoalObject> lastGoals;
    protected GoalObject getLastGoal() {
        lastGoals = new ArrayList<GoalObject>();
        for (GoalObject go : graph.keySet()) {
            if (graph.get(go).size() == 1 || getNonzeroEdges(go) == 1) { //only one nonzero length edge (but other self edges) are also a lastgoal
                lastGoals.add(go);
            }
        }
        if (lastGoals.size() != 1) {
            //System.err.println("wrong lastgoal number");
            return null;
        }
        return lastGoals.get(lastGoals.size() - 1);
    }

    @Override
    public String toString() {
        String str = graph.toString();
        str = str.replaceAll("]", "]\n");
        return str;
    }

    /*
     * if C1 and C2 null, set them to closest goals in path1 and 2
     * if C1 or C2 not null, just search for the other one
     * if neither of them is null, do nothing
     *
     * only nodes with <= 2 unique edges considered
     */
    protected Edge getPathConnection(GoalPath path2, GoalObject C1, GoalObject C2) {
        boolean searchForC1 = C1==null, searchForC2 = C2==null;
        if (!searchForC1 && !searchForC2 || path2 == null) return null;
        GoalObject cg1 = null, cg2 = null;
        double minDistance = Double.MAX_VALUE, cDistance;

        for (GoalObject go1 : this.graph.keySet()) {
            if (searchForC1) {
                C1 = go1;
                if (this.getNonzeroEdges(C1) > 2)
                    continue;
            }
            for (GoalObject go2 : path2.graph.keySet()) {
                if (searchForC2) {
                    C2 = go2;
                    if (this.getNonzeroEdges(C2) > 2)
                        continue;
                }
                if (!C1.equals(C2)) {
                    if ((cDistance=TSPGrid.distance(C1, C2)) < minDistance) {
                        cg1 = C1;
                        cg2 = C2;
                        minDistance = cDistance;
                    }
                }
                if (!searchForC2)
                    break;
            }
            if (!searchForC1)
                break;
        }
        if (searchForC1) C1 = cg1;
        if (searchForC2) C2 = cg2;
        Edge edge = new Edge(C1, C2, 1);
        if (C1 == null || C2 == null) {
            System.err.println("No connection found");
            return null;
        }
        else if (debugging) {
            drawEdge(edge);
            if (!searchForC1)
                drawer.drawCircle(C1.getCurrentX(), C1.getCurrentY(), Color.black);
            if (!searchForC2)
                drawer.drawCircle(C2.getCurrentX(), C2.getCurrentY(), Color.black);
        }
        return edge;
    }

    public void setDrawer(TspDrawer drawer) {
        this.drawer = drawer;
    }

    public void drawGoalPath() {
        if (!debugging) return;
        for (GoalObject go : graph.keySet()) {
            for (Edge e : graph.get(go)) {
                drawer.drawGoalArrow(e.getSource().getCurrentX(), e.getSource().getCurrentY(), e.getSink().getCurrentX(), e.getSink().getCurrentY());
            }
        }
        sleep();
    }

    public void drawEdge(Edge e) {
        drawEdge(e, Color.orange);
    }

    public void drawEdge(Edge e, Color c) {
        if (debugging) {
            try {
                drawer.drawGoalArrow(e.getSource().getCurrentX(), e.getSource().getCurrentY(), e.getSink().getCurrentX(), e.getSink().getCurrentY(), c);
            }
            catch (Exception ex) {}
            sleep();
        }
    }

    public void removeEdges(GoalObject go1, GoalObject go2) {
        if (go1 == null || go2 == null) return;

        if (graph.get(go1) != null)
            for (Edge e : graph.get(go1)) {
                if (e.getSink().equals(go1) && e.getSource().equals(go2) || e.getSink().equals(go2) && e.getSource().equals(go1))
                    graph.get(go1).remove(e);
            }
        if (graph.get(go2) != null)
            for (Edge e : graph.get(go2)) {
                if (e.getSink().equals(go1) && e.getSource().equals(go2) || e.getSink().equals(go2) && e.getSource().equals(go1))
                    graph.get(go2).remove(e);
            }
    }

    public static double mergedPathDistance(List<GoalObject> path1, List<GoalObject> path2, GoalObject go1, GoalObject go2, List<GoalObject> mergedPath) {
        return mergedPathDistance(path1, path1, go1, go2, mergedPath, null);
    }

    //TODO !test whether this works
    public static double mergedPathDistance(List<GoalObject> path1, List<GoalObject> path2, GoalObject go1, GoalObject go2, List<GoalObject> mergedPath, TspDrawer drawer) {
        double distance = 0;

        TSPGrid.closePath(path1);
        TSPGrid.closePath(path2);

        GoalPath goalpath1 = new GoalPath(path1);
        GoalPath goalpath2 = new GoalPath(path2);

        if (goalpath1.getGraph().get(go1) == null || goalpath2.getGraph().get(go2) == null) {
            System.err.println("merge failed - tried to merge on nonexistent goalobjects");
            goalpath1 = new GoalPath(path1);
            goalpath2 = new GoalPath(path2);
        }

        GoalObject go1b = null, go2b = null;
        for (Edge e : (goalpath1.getGraph().get(go1))) {
            if (e.getDirection() >= 0) {
                go1b = e.getSink();
                break;
            }
        }
        if (go1b == null) {
            for (Edge e : (goalpath1.getGraph().get(go1))) {
                if (e.getDirection() < 0 && e.getSink() != go1) {
                    go1b = e.getSink();
                    break;
                }
            }
        }
        for (Edge e : (goalpath2.getGraph().get(go2))) {
            if (e.getDirection() >= 0) {
                go2b = e.getSink();
                break;
            }
        }
        if (go2b == null) {
            for (Edge e : (goalpath2.getGraph().get(go2))) {
                if (e.getDirection() < 0 && e.getSink() != go2) {
                    go2b = e.getSink();
                    break;
                }
            }
        }

        if (go1b == null) go1b = go1;
        if (go2b == null) go2b = go2;

        //open edges between go1 and go1b, and go2 and go2b
        goalpath1.removeEdges(go1, go1b);
        goalpath2.removeEdges(go2, go2b);
        //connect go1 and go2 and go1b and go2b
        goalpath1.connectOn(goalpath2, go1, go2b, go2, go1b);
        if (goalpath1.getLastGoal() == null && goalpath1.lastGoals.size() > 0) {
            if (goalpath1.lastGoals.size() == 2) {
                GoalObject from = goalpath1.lastGoals.get(0);
                GoalObject to = goalpath1.lastGoals.get(1);
                goalpath1.getGraph().get(from).add(new Edge(from, to, 1));
                goalpath1.getGraph().get(to).add(new Edge(to, from, -1));
            }
            else {
                Edge e = new Edge(go1b, go1, 1);
                if (!go1.equals(go1b) || !goalpath1.hasConnection(e)) {
                    goalpath1.getGraph().get(go1b).add(e);
                    goalpath1.getGraph().get(go1).add(new Edge(go1, go1b, -1));
                }
                e = new Edge(go2, go2b, 1);
                if (!go2.equals(go2b) && !goalpath1.hasConnection(e)) {
                    goalpath1.getGraph().get(go2).add(e);
                    goalpath1.getGraph().get(go2b).add(new Edge(go2b, go2, -1));
                }
            }
        }

        if (debugging && drawer != null) {
           for (GoalObject go : goalpath1.getGraph().keySet()) {
             Color c = Color.orange;
             for (Edge e : goalpath1.getGraph().get(go)) {
                drawer.drawGoalArrow(e.getSource().getCurrentX(), e.getSource().getCurrentY(), e.getSink().getCurrentX(), e.getSink().getCurrentY(), c);
             }
             c = Color.blue;
             drawer.drawCircle(go1.getCurrentX(), go1.getCurrentY(), c, drawer.getGoalSize()+8);
             drawer.drawCircle(go2.getCurrentX(), go2.getCurrentY(), c, drawer.getGoalSize()+8);
           }
        }

        List<GoalObject> path = goalpath1.getGoalObjectPath();
        TSPGrid.removeDuplicates(path);
        TSPGrid.closePath(path);
        if (mergedPath != null) mergedPath.addAll(path);

        distance = TSPGrid.goalObjectPathDistance(path);

        return distance;
    }

    public void connectOn(GoalPath newGoalObjectPath, GoalObject from1, GoalObject from2, GoalObject to1, GoalObject to2) {
        for (GoalObject newGoal : newGoalObjectPath.graph.keySet()) {
            if (!graph.keySet().contains(newGoal))
                graph.put(new GoalObject(newGoal.getOriginalX(), newGoal.getOriginalY(), lastOrderNumber++), newGoalObjectPath.graph.get(newGoal));
        }
        if (from1 != null && to1 != null) {
            graph.get(from1).add(new Edge(from1, to1, 1));
            graph.get(to1).add(new Edge(to1, from1, -1));
        }

        if (from2 != null && to2 != null) {
            graph.get(from2).add(new Edge(from2, to2, 1));
            graph.get(to2).add(new Edge(to2, from2, -1));
        }
    }

    public static List<GoalObject> removeIntersections(List<GoalObject> goalObjectPath) {
        GoalPath goalPath = new GoalPath(goalObjectPath);

        for (GoalObject go1 : goalPath.getGraph().keySet()) {
            for (Edge e1 : goalPath.getGraph().get(go1)) {
                for (GoalObject go2 : goalPath.getGraph().keySet()) {
                    for (Edge e2 : goalPath.getGraph().get(go2)) {
                        if (e1.intersects(e2)) {
                            Edge newedge1 = new Edge(e1.getSource(), e2.getSink(), 0);
                            if (goalPath.hasConnection(newedge1))
                                newedge1 = new Edge(e1.getSource(), e2.getSource(), 0);
                            Edge newedge2 = new Edge(e2.getSource(), e1.getSink(), 0);
                            if (goalPath.hasConnection(newedge2))
                                newedge1 = new Edge(e1.getSink(), e2.getSink(), 0);
                            //remove e1 and e2 and inverts
                            goalPath.getGraph().get(go1).remove(e1);
                            e1.invert();
                            e1.setDirection(e1.getDirection()*-1);
                            goalPath.getGraph().get(e1.getSource()).remove(e1);
                            goalPath.getGraph().get(go2).remove(e2);
                            e2.invert();
                            e2.setDirection(e2.getDirection()*-1);
                            goalPath.getGraph().get(e2.getSource()).remove(e2);
                            //add new edges
                            goalPath.getGraph().get(newedge1.getSource()).add(newedge1);
                            goalPath.getGraph().get(newedge2.getSource()).add(newedge2);
                            newedge1 = new Edge(newedge1);
                            newedge1.invert();
                            newedge2 = new Edge(newedge1);
                            newedge2.invert();
                            //add edge inverts
                            goalPath.getGraph().get(newedge1.getSource()).add(newedge1);
                            goalPath.getGraph().get(newedge2.getSource()).add(newedge2);
                        }
                    }
                }
            }
        }

        return goalPath.getGoalObjectPath();
    }
}
