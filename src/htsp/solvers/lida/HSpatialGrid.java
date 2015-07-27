/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.HTSPLogger;
import htsp.TspDrawer;
import htsp.solvers.GoalPoint;
import htsp.solvers.TSPGrid;
import htsp.solvers.lida.excitation.ExcitationStrategy;
import htsp.solvers.lida.excitation.LinearExcitation;
import htsp.solvers.lida.excitation.SigmoidExcitation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author madlt
 */
public class HSpatialGrid {
    //private static final Color EMPTYCELLCOLOR = new Color(220, 220, 255);

    private static final Object DEFAULTCONTENT = 1; //actual content of grid cells to be implemented later (PAM node structure)
    private static final int MAXADJACENTNEIGHBORS = 8; //rectangular grid
    //^ try hexagonal grid (6) !!!! - will have to overwrite initNeighbors

    public static int SPREADRADIUS = 6; //10 how far the activation can spread... generally the bigger the better
    public static final double DEFAULTWEIGHT = 0.6; //0.6^4 = 0.13
    public static final double DEFAULTC = 1, DEFAULTK = 2; //2       c*e^-kx

    public static final double DEFAULTGOALACTIVATION = 1; // 0.1
    public static final double DEFAULTGOALINHIBITION = 0;//-DEFAULTGOALACTIVATION / 2; // - DEFAULTGOALACTIVATION / 2

    public static final double INHIBITIONDISTANCE = Double.MAX_VALUE, INHIBITORSIGMOIDA = 1;
   

    protected int width, height;
    protected GridNode[][] grid;
    protected GridNode globalInhibitor;
    protected GoalObject[] goals;

    public HSpatialGrid() {
        this(htsp.HTSP.DEFAULTWIDTH, htsp.HTSP.DEFAULTHEIGHT);
    }

    public HSpatialGrid(int width, int height, boolean randomgoals, double crossConnectionMultiplier, ExcitationStrategy strategy) {
        if (!randomgoals) init(width, height, null, crossConnectionMultiplier, strategy);
        else init(width, height, TSPGrid.randomGoals((int)Math.sqrt(width*height), width, height), crossConnectionMultiplier, strategy);
    }

    public HSpatialGrid(int width, int height, boolean randomgoals) {
        if (!randomgoals) init(width, height, null);
        else init(width, height, TSPGrid.randomGoals((int)Math.sqrt(width*height), width, height));
    }

    public HSpatialGrid(int width, int height) {
        this(width, height, TSPGrid.randomGoals((int)Math.sqrt(width*height), width, height));
    }

    public HSpatialGrid(int width, int height, GoalPoint[] goalpoints) {
        init(width, height, goalpoints);
    }

    public HSpatialGrid(GoalPoint[] goalpoints) {
        int maxX = 0, maxY = 0;
        for (GoalPoint n : goalpoints) {
            if (n.getX() > maxX) maxX = n.getX();
            if (n.getY() > maxY) maxY = n.getY();
        }
        width=maxX+1;
        height=maxY+1;
        init(width, height, goalpoints);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    protected final void init(int width, int height, GoalPoint[] goalpoints) {
        init(width, height, goalpoints, 1);
    }

    protected final void init(int width, int height, GoalPoint[] goalpoints, double crossConnectionMultiplier) {
        init(width, height, goalpoints, 1, new SigmoidExcitation());
    }


    protected final void init(int width, int height, GoalPoint[] goalpoints, double crossConnectionMultiplier, ExcitationStrategy strategy) {
        GridNode.resetNumbering();
        this.width = width;
        this.height = height;

        grid = new GridNode[height][];
        for (int i = 0; i < height; i++) {
            grid[i] = new GridNode[width];
            for (int j = 0; j < width; j++) {
                grid[i][j] = new GridNode(j, i, strategy);
            }
        }

        if (crossConnectionMultiplier!=0) initNeighbors(crossConnectionMultiplier);

        if (goalpoints != null) addGoalPoints(goalpoints, DEFAULTGOALACTIVATION);
    }

    //(Csizmadia & Muller, 2008)
    public static double getWeight(double distance) {
        return DEFAULTC*Math.exp(-DEFAULTK*distance);
    }

    public final void initNeighbors(double crossConnectionMultiplier) {
        int maxNoOfNeighbors = (int)Math.pow(SPREADRADIUS*2+1, 2);
        HTSPLogger.logline("Setting up neighbor connections on grid... ");
        globalInhibitor = new InhibitorGridNode(-1, -1, new SigmoidExcitation(INHIBITORSIGMOIDA, INHIBITORSIGMOIDA/2));
        Connection[] inhibitingConnections = new Connection[height*width];
        int ic = 0;
        for (int i = 0; i < height; i++) {
            HTSPLogger.log(Math.round(1000.0/height*i)/10.0 + " % ");
            for (int j = 0; j < width; j++) { //each node
                Connection[] neighbors = new Connection[maxNoOfNeighbors+1];
                int n = 0;
                for (int k = i - SPREADRADIUS; k <= i + SPREADRADIUS; k++) {
                    for (int l = j - SPREADRADIUS; l <= j + SPREADRADIUS; l++) {
                            int nx = l, ny = k;
                            if (ny<0) ny+=height;
                            if (ny>=height) ny-=height;
                            if (nx<0) nx+=width;
                            if (nx>=width) nx-=width;
                            if ((nx!=j || ny!=i) && nx >= 0 && ny >= 0 && nx < width && ny < height) {
                                try {
                                    double d = TSPGrid.distance(j, i, l, k);
                                    double w = getWeight(d) * crossConnectionMultiplier;
                                    neighbors[n++] = new Connection(w, d, grid[i][j], grid[ny][nx]);
                                }
                                catch (ArrayIndexOutOfBoundsException ex) {
                                    System.out.println("Error: Too many neighbors...");
                                    ex.printStackTrace();
                                }
                            }
                    }
                }
                inhibitingConnections[ic++]=new Connection(getWeight(INHIBITIONDISTANCE), INHIBITIONDISTANCE, globalInhibitor, grid[i][j]);
                neighbors[n++] = new Connection(getWeight(INHIBITIONDISTANCE), INHIBITIONDISTANCE, grid[i][j], globalInhibitor);
                Connection[] truncatedNeighbors = Arrays.copyOf(neighbors, n);
                Arrays.sort(truncatedNeighbors);
                //grid[i][j].setNeighbors(truncatedNeighbors);
                grid[i][j].addConnections(truncatedNeighbors);
            }
        }
        globalInhibitor.setNeighbors(inhibitingConnections);
        HTSPLogger.logline("100 %");
    }

    public void addConnections(int x, int y, Connection[] connections) {
        grid[y][x].addConnections(connections);
    }

    public GridNode getNode(int x, int y) {
        return grid[y][x];
    }

    public List<GridNode> getNodes() {
        List<GridNode> nodes = new ArrayList<GridNode>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                nodes.add(grid[i][j]);
            }
        }
        return nodes;
    }

    public void reset() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j].setActivation(0);
            }
        }
        Connection[] neighbors = grid[height/2][width/2].getNeighbors();
        if (neighbors == null || neighbors.length == 0 || neighbors[0].isCrosshierarchy()) {
            initNeighbors(1);
        }
    }

    public void addGoalPoints(GoalPoint[] goalpoints, double activation) {
        goals = new GoalObject[goalpoints.length];
        int g = 0;
        for (GoalPoint gp : goalpoints) {
            if (gp.getX() < width && gp.getY() < height) {
                goals[g]=new GoalObject(gp.getX(), gp.getY(), grid[gp.getY()][gp.getX()].getNumber());
                goals[g].setLabel(gp.getLabel());
                grid[gp.getY()][gp.getX()].addGoal(goals[g], activation);
                g++;
            }
        }
    }

    public void addGoalObjects(GoalObject[] newgoals, double activation) {
        goals = newgoals;
        for (GoalObject go : newgoals) {
            if (go.getCurrentX() < width && go.getCurrentY() < height) {
                grid[go.getCurrentY()][go.getCurrentX()].addGoal(go, activation);
            }
        }
    }

    public void setGoalObjects(GoalObject[] goalobjects) {
        goals = goalobjects;
    }

    public GoalObject[] getGoalObjects() {
        return goals;
    }


    protected GoalPoint[] getGoalPoints() {
        List<GoalPoint> goalpoints = new ArrayList<GoalPoint>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                GoalPoint gp = new GoalPoint(j, i, TspDrawer.getColorFromActivation(grid[i][j].getActivation()));
                if (!grid[i][j].isEmpty())
                    gp.setMarked(true);
                goalpoints.add(gp);
            }
        }
        GoalPoint[] result = new GoalPoint[goalpoints.size()];
        goalpoints.toArray(result);
        return result;
    }
}
