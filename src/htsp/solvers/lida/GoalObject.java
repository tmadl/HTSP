/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author madlt
 */
public class GoalObject implements Comparable, Cloneable {
    protected int originalX = -1, originalY = -1;
    protected int currentX = -1, currentY = -1;
    protected int originalOrderNumber = -1, orderNumber = -1;
    protected String label = "DEFAULTGO";
    protected List<GoalObject> subGoalObjects = new ArrayList<GoalObject>();

    public void addSubGoalObject(GoalObject sgp) {
        subGoalObjects.add(sgp);
    }

    public List<GoalObject> getSubGoalObjects() {
        return subGoalObjects;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public int getOriginalX() {
        return originalX;
    }

    public int getOriginalY() {
        return originalY;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    private GoalObject() {}

    public GoalObject(int X, int Y, int orderNumber) {
        originalX = currentX = X;
        originalY = currentY = Y;
        this.orderNumber = this.originalOrderNumber = orderNumber;
    }

    public void init(GoalObject goal) {
        originalX = goal.originalX;
        originalY = goal.originalY;
        currentX = goal.currentX;
        currentY = goal.currentY;
        this.orderNumber = goal.orderNumber;
        this.originalOrderNumber = goal.originalOrderNumber;
        this.subGoalObjects = goal.subGoalObjects;
    }

    public GoalObject clone() {
        GoalObject ret = new GoalObject();
        ret.init(this);
        return ret;
    }

    public int getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(int number) {
        orderNumber = number;
    }
    public int getOriginalOrderNumber() {
        return originalOrderNumber;
    }
    
    //goalobject are sorted by their current order number
    @Override
    public int compareTo(Object o) {
        if (o instanceof GoalObject)
            return orderNumber - ((GoalObject)o).getOrderNumber();
        else
            return Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GoalObject)
            return (((GoalObject)o).getCurrentX() == this.getCurrentX() && ((GoalObject)o).getCurrentY() == this.getCurrentY());
        else
            return false;
    }

    public static int hashgridsize = (int)Math.sqrt(Integer.MAX_VALUE);
    @Override
    public int hashCode() {
        return (hashgridsize*getCurrentY()+getCurrentX());
    }

    //@Override
    //public String toString() {return "GO(cX="+currentX+",cY="+currentY+")(oX="+originalX+",oY="+originalY+")";}

    @Override
    public String toString() {
        String sgpstr = "";
        if (!this.subGoalObjects.isEmpty()) {
         sgpstr=" SubGoalPoints (";
         for (GoalObject go : this.subGoalObjects) sgpstr += go;
         sgpstr+=");";
        }
        //return "GO(cX="+currentX+",cY="+currentY+")(oX="+originalX+",oY="+originalY+");;"+sgpstr;
        return "GO(cX="+currentX+",cY="+currentY+"); "+sgpstr;
    }
}
