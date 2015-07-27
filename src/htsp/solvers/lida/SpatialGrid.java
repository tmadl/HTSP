/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.HTSPLogger;
import htsp.TspDrawer;
import htsp.solvers.GoalPoint;
import htsp.solvers.TSPGrid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author madlt
 */
public class SpatialGrid {
    //private static final Color EMPTYCELLCOLOR = new Color(220, 220, 255);

    private static final Object DEFAULTCONTENT = 1; //actual content of grid cells to be implemented later (PAM node structure)
    private static final int MAXADJACENTNEIGHBORS = 8; //rectangular grid
    //^ try hexagonal grid (6) !!!! - will have to overwrite initNeighbors

    public static int SPREADRADIUS = 6; //10 how far the activation can spread... generally the bigger the better
    public static final double DEFAULTWEIGHT = 0.6; //0.6^4 = 0.13
    public static final double DEFAULTK = 1; //1       e^-kx

    public static final double DEFAULTGOALACTIVATION = 0.1; // 0.1
    public static final double DEFAULTGOALINHIBITION = -DEFAULTGOALACTIVATION / 2; // - DEFAULTGOALACTIVATION / 2
   

    protected int width, height;
    protected GridNode[][] grid;
    protected GoalObject[] goals;

    public SpatialGrid() {
        this(htsp.HTSP.DEFAULTWIDTH, htsp.HTSP.DEFAULTHEIGHT);
    }

    public SpatialGrid(int width, int height, boolean randomgoals) {
        if (!randomgoals) init(width, height, null);
        else init(width, height, TSPGrid.randomGoals((int)Math.sqrt(width*height), width, height));
    }

    public SpatialGrid(int width, int height) {
        this(width, height, TSPGrid.randomGoals((int)Math.sqrt(width*height), width, height));
    }

    public SpatialGrid(int width, int height, GoalPoint[] goalpoints) {
        init(width, height, goalpoints);
    }

    public SpatialGrid(GoalPoint[] goalpoints) {
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
        GridNode.resetNumbering();
        this.width = width;
        this.height = height;

        //SPREADRADIUS = 1;//Math.max(width, height) / 2; //10  Math.max(width, height) / 2

        grid = new GridNode[height][];
        for (int i = 0; i < height; i++) {
            grid[i] = new GridNode[width];
            for (int j = 0; j < width; j++) {
                grid[i][j] = new GridNode(j, i);
            }
        }
        initNeighbors();
        //this.goals = goals;
        if (goalpoints != null) addGoalPoints(goalpoints);
    }

    //(Csizmadia & Muller, 2008)
    public static double getWeight(double distance) {
        return Math.exp(-DEFAULTK*distance);
    }

    protected final void initNeighbors() {
        //int maxNoOfNeighbors = ((SPREADRADIUS*SPREADRADIUS+SPREADRADIUS)/2)*MAXADJACENTNEIGHBORS;
        int maxNoOfNeighbors = (int)Math.pow(SPREADRADIUS*2+1, 2);
        HTSPLogger.logline("Setting up neighbor connections on grid... ");
        for (int i = 0; i < height; i++) {
            HTSPLogger.log(Math.round(1000.0/height*i)/10.0 + " % ");
            for (int j = 0; j < width; j++) { //each node
                Connection[] neighbors = new Connection[maxNoOfNeighbors];
                int n = 0;
                //for (int r = 1; r < SPREADRADIUS; r++)
                {
                    for (int k = i - SPREADRADIUS; k <= i + SPREADRADIUS; k++) {
                        for (int l = j - SPREADRADIUS; l <= j + SPREADRADIUS; l++) {
                            //if ((Math.abs(k-i) == r || Math.abs(l-j) == r))  //if on outer border
                            if (k!=i || l!=j)
                            {
                                    //k>=0 && l>=0 && k<height && l<width) {
                                int nx = l, ny = k;
                                if (ny<0) ny+=height;
                                if (ny>=height) ny-=height;
                                if (nx<0) nx+=width;
                                if (nx>=width) nx-=width;
                                if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                                    try {
                                        //double w = Math.pow(DEFAULTWEIGHT, r);
                                        double w = getWeight(TSPGrid.distance(grid[i][j].getX(), grid[i][j].getY(), grid[ny][nx].getX(), grid[ny][nx].getY()));
                                        neighbors[n++] = new _Connection(w, grid[i][j], grid[ny][nx]);
                                    }
                                    catch (ArrayIndexOutOfBoundsException ex) {
                                        System.out.println("Error: Too many neighbors...");
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
                Connection[] truncatedNeighbors = Arrays.copyOf(neighbors, n);
                Arrays.sort(truncatedNeighbors);
                grid[i][j].setNeighbors(truncatedNeighbors);
            }
        }
        HTSPLogger.logline("100 %");
    }

    private void addGoalPoints(GoalPoint[] goalpoints) {
        goals = new GoalObject[goalpoints.length];
        int g = 0;
        for (GoalPoint gp : goalpoints) {
            if (gp.getX() < width && gp.getY() < height) {
                //grid[gp.getY()][gp.getX()].setContent(DEFAULTCONTENT);
                goals[g]=new GoalObject(gp.getX(), gp.getY(), grid[gp.getY()][gp.getX()].getNumber());
                grid[gp.getY()][gp.getX()].addGoal(goals[g], DEFAULTGOALACTIVATION);
                g++;
            }
        }
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
