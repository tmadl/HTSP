/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp;

import htsp.solvers.GoalPoint;
import htsp.solvers.lida.GoalObject;
import htsp.solvers.lida.GridNode;
import htsp.solvers.lida.LIDASolverHierarchicalSIExhaustive;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author madlt
 */
public class TspDrawer implements Drawer {
    private JPanel drawPanel;
    private int gridWidth, gridHeight, wInc, hInc;
    private Graphics graphics;

    public int gethInc() {
        return hInc;
    }

    public int getwInc() {
        return wInc;
    }
    
    private static final Color gridColor = Color.LIGHT_GRAY, circleColor = Color.BLACK, 
            lineColor = Color.DARK_GRAY, customCircleColor = Color.BLUE, pxColor = Color.RED,
            markColor = Color.BLUE;
    private static final int DEFAULTWIDTH = 10, DEFAULTHEIGHT = 10, pixelsize = 2;
    
    private GoalPoint[] goalpoints;
    private List<GoalPoint> customGoals = new ArrayList<GoalPoint>();
    private List<Integer> path;
    private boolean drawCustomGoals = true;

    public void setDrawCustomGoals(boolean b) {
        drawCustomGoals = b;
    }

    public GoalPoint[] getGoalpoints() {
        return goalpoints;
    }

    public List<Integer> getPath() {
        return path;
    }

    private List<Px> pixels = new ArrayList<Px>();
    class Px {
        public int X, Y;
        public Px(int x, int y) {X=x; Y=y;}
    }
    
    public TspDrawer(JPanel p) {
        drawPanel = p;
        graphics = p.getGraphics();
        setSize(DEFAULTWIDTH, DEFAULTHEIGHT);
    }
    
    public TspDrawer(JPanel p, int gridWidth, int gridHeight) {
        drawPanel = p;
        graphics = p.getGraphics();
        setSize(gridWidth, gridHeight);
    }

    public static Color getColorFromActivation(double activation) {
        int c = (int)((1.0-activation)*255.0);
        try {
            return new Color(c, c, c);
        }
        catch (Exception ex) {
            return Color.RED;
        }
    }

    public static double getActivationFromColor(Color col) {
        return (1.0 - col.getRed()/255.0);
    }

    public final void setSize(int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }
    
    public void setGoalpoints(GoalPoint[] goals) {
        this.goalpoints = goals;
    }

    public void clearCustomGoals() {
        this.customGoals.clear();
    }
    public void addCustomGoal(GoalPoint g) {
        customGoals.add(g);
    }
    public List<GoalPoint> getCustomGoals() {
        return customGoals;
    }
    
    public void setPath(List<Integer> path) {
        this.path = path;
    }
    
    public void draw() {
        graphics = drawPanel.getGraphics();
        draw(graphics);
    }
    
    public void draw(Graphics graphics) {
        this.graphics = graphics;
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, drawPanel.getWidth(), drawPanel.getHeight());
        drawGrid();

        graphics.setColor(circleColor);
        drawGoals(this.goalpoints);
        
        GoalPoint[] cgoals = new GoalPoint[customGoals.size()];
        customGoals.toArray(cgoals);
        if (this.drawCustomGoals) drawGoals(this.customGoals.toArray(cgoals), customCircleColor);

        drawPath();

        drawPixels();
    }

    public void clear() {
        clearCustomGoals();
        this.pixels.clear();
    }

    public void setGraphics(Graphics graphics) {
        this.graphics = graphics;
    }
    
    public void drawGrid() {
        int pW = drawPanel.getWidth(), pH = drawPanel.getHeight();
        wInc = pW / (gridWidth + 1); hInc = pH / (gridHeight + 1);
        
        graphics.setColor(gridColor);
        for (int i = 1; i < gridWidth+1; i++) {
            graphics.drawLine(i*wInc, 0, i*wInc, pH);
        }
        for (int j = 1; j < gridHeight+1; j++) {
            graphics.drawLine(0, j*hInc, pW, j*hInc);
        }

        graphics.setColor(new Color(200, 255, 255));

        for (int i = 0; i < gridWidth+1; i+=LIDASolverHierarchicalSIExhaustive.F) {
            graphics.drawLine(i*wInc+wInc/2, 0, i*wInc+wInc/2, pH);
        }
        for (int j = 0; j < gridHeight+1; j+=LIDASolverHierarchicalSIExhaustive.F) {
            graphics.drawLine(0, j*hInc+hInc/2, pW, j*hInc+hInc/2);
        }
    }

    public void drawGoalArrow(int sgX, int sgY, int egX, int egY) {
        this.drawGoalArrow(sgX, sgY, egX, egY, Color.pink);
    }

    public void drawGoalArrow(int sgX, int sgY, int egX, int egY, Color c) {
        graphics.setColor(c);
        drawCircle(egX, egY, c);
        graphics.drawLine((sgX+1)*wInc, (sgY+1)*hInc, (egX+1)*wInc, (egY+1)*hInc);
    }

    public void drawCircle(int x, int y, Color c) {
        graphics.setColor(c);
        graphics.drawOval((x+1)*wInc - 2, (y+1)*hInc - 2, 4, 4);
    }

    public void drawCircle(int x, int y, Color c, int size) {
        graphics.setColor(c);
        graphics.drawOval((x+1)*wInc - size/2, (y+1)*hInc - size/2, size, size);
    }

    public void addPx(int x, int y) {
        addPx(x, y, pxColor);
    }

    public void addPx(int x, int y, Color c) {
        this.pixels.add(new Px(x, y));
        graphics.setColor(c);
        graphics.drawRect(x - 1, y - 1, pixelsize, pixelsize);
    }

    public void drawPixels() {
        for (Px pixel : pixels) {
            graphics.setColor(pxColor);
            graphics.drawRect(pixel.X - 1, pixel.Y - 1, pixelsize, pixelsize);
        }
    }

    public int transformX(int X) { return transformX(X, true); }
    public int transformX(int X, boolean toPx) {
        wInc = drawPanel.getWidth() / (gridWidth + 1);
        if (toPx)
            return wInc + X * wInc;
        else
            return (int)((X-wInc/2.0)/wInc);
    }
    public int transformY(int Y) { return transformY(Y, true); }
    public int transformY(int Y, boolean toPx) {
        hInc = drawPanel.getHeight() / (gridHeight + 1);
        if (toPx)
            return hInc + Y * hInc;
        else
            return (int)((Y-hInc/2.0)/hInc);
    }
    
    public void drawGoals(GoalPoint[] goals) {
        drawGoals(goals, null);
    }

    public int getGoalSize() {
        return (int)(Math.min(wInc, hInc)/1.5);
    }

    public void drawGoals(GoalPoint[] goals, Color goalColor) {
        if (goals != null && goals.length > 0) {
            wInc = drawPanel.getWidth() / (gridWidth + 1); hInc = drawPanel.getHeight() / (gridHeight + 1);
            int circleSize = getGoalSize();
            for (GoalPoint g : goals) {
                int x = transformX(g.getX()) - circleSize/2;
                int y = transformY(g.getY()) - circleSize/2;
                if (goalColor == null)
                    graphics.setColor(g.getColor());
                else 
                    graphics.setColor(goalColor);
                graphics.fillOval(x, y, circleSize, circleSize);
                if (g.isMarked()) {
                    //graphics.setColor(Math.random()>0.5?markColor:Color.red);
                    graphics.setColor(markColor);
                    int rm = circleSize / 2;
                    graphics.fillOval(x+rm/2, y+rm/2, circleSize-rm, circleSize-rm);
                }
            }
        }
    }
    
    public void drawPath() {
        if (this.path != null && this.path.size() > 1 && this.goalpoints != null) {// && this.goalpoints.length == this.path.size() - 1) {
            //graphics.setColor(lineColor);
            int red = 0, redincrement = (int)(255.0/path.size());
            for (int i = 1; i<path.size(); i++) {
                graphics.setColor(new Color(red, 0, 0));
                red += redincrement;
                try {
                    GoalPoint prevN = goalpoints[path.get(i-1)], N = goalpoints[path.get(i)];
                    graphics.drawLine(transformX(prevN.getX()), transformY(prevN.getY()), transformX(N.getX()), transformY(N.getY()));
                }
                catch (Exception ex) {}
            }
        }
    }

    List<GridNode> cnodes = null;
    public void drawGoalObjectPath(List<GoalObject> gpath) {
        if (cnodes != null) drawGoalObjectPath(gpath, cnodes);
        else System.err.println("no nodes found for drawing path");
    }
    public void drawGoalObjectPath(List<GoalObject> gpath, List<GridNode> nodes) {
        List<Integer> cpath = new ArrayList<Integer>();
        cnodes = nodes;
        for (GoalObject go : gpath) {
            //solutionPaths[level-1].add(goalList.indexOf(go));
            GridNode gn = new GridNode(go.getCurrentX(), go.getCurrentY());
            cpath.add(cnodes.indexOf(gn));
        }
        setPath(cpath);
        draw();
    }
}