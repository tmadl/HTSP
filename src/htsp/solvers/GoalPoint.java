/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package htsp.solvers;

import java.awt.Color;

/**
 *
 * @author madlt
 */
public class GoalPoint implements Comparable {
    protected int X, Y;
    protected Color color = Color.BLACK;
    protected boolean marked = false;
    protected String label = "DEFAULTGOALPOINT";

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getX() {
        return X;
    }

    public void setX(int X) {
        this.X = X;
    }

    public int getY() {
        return Y;
    }

    public void setY(int Y) {
        this.Y = Y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
    
    public GoalPoint() {X=-1;Y=-1;}
    
    public GoalPoint(int x, int y) {
        X=x; Y=y;
    }

    public GoalPoint(int x, int y, Color col) {
        X=x; Y=y; color = col;
    }

    @Override
    public String toString() {
        return "GP("+X+","+Y+")";
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof GoalPoint)
            return (int)Math.sqrt(Math.pow(((GoalPoint)o).X-X,2)+Math.pow(((GoalPoint)o).Y-Y,2));
        else
            return Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GoalPoint)
            return (((GoalPoint)o).X==X&&((GoalPoint)o).Y==Y);
        else
            return false;
    }

    public static int hashgridsize = (int)Math.sqrt(Integer.MAX_VALUE);
    @Override
    public int hashCode() {
        return (hashgridsize*Y+X);
    }
}
