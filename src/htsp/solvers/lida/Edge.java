/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.solvers.TSPGrid;

/**
 *
 * @author Tom
 */
public class Edge {
    private GoalObject source, sink;
    private int direction = 0;
    private double length = 0;

    public double getLength() {
        return length;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Edge(GoalObject source, GoalObject sink, int dir) {
        init(source, sink);
        direction = dir;
    }

    public Edge(GoalObject source, GoalObject sink) {
        init(source, sink);
    }

    public Edge(Edge e) {
        this(e.getSource(), e.getSink(), e.getDirection());
    }

    private void init(GoalObject source, GoalObject sink) {
        this.source = source;
        this.sink = sink;
        if (source != null && sink != null) {
            this.length = TSPGrid.distance(source.getOriginalX(), source.getOriginalY(), sink.getOriginalX(), sink.getOriginalY(), true);
        }
        else {
            System.err.println("Edge to null");
            this.length = Double.MAX_VALUE;
        }
    }

    public GoalObject getSink() {
        return sink;
    }

    public void setSink(GoalObject sink) {
        this.sink = sink;
    }

    public GoalObject getSource() {
        return source;
    }

    public void setSource(GoalObject source) {
        this.source = source;
    }

    public void invert() {
        //direction *= -1;
        GoalObject oldSink = sink;
        this.sink = source;
        this.source = oldSink;
    }

    public double getSlope() {
        GoalObject startPoint, endPoint;
        if (this.direction >= 0) {
            startPoint = source; endPoint = sink;
        }else {
            startPoint = sink; endPoint = source;
        }
        return ((double)endPoint.getCurrentY()-(double)startPoint.getCurrentY())/((double)endPoint.getCurrentX()-(double)startPoint.getCurrentX());
    }

    public boolean hasPoint(int x, int y) {
        if (source.getCurrentX()==x && source.getCurrentY()==y || sink.getCurrentX()==x && sink.getCurrentY()==y)
            return true;
        return false;
    }

    public double getIntercept() {
        GoalObject startPoint;
        if (this.direction >= 0)
            startPoint = source;
        else
            startPoint = sink;
        return (double)startPoint.getCurrentY()-getSlope()*startPoint.getCurrentX();
    }

    public boolean intersects(Edge edge) {
        double epsilon = 1e-6;
        double k1 = getSlope(), k2 = edge.getSlope();
        double xp1;
        boolean hasIntersection = true; //infinite k - vertical line - there is always an intersection somewhere
        if (!Double.isInfinite(k1) && !Double.isInfinite(k2)) {
            xp1 = (edge.getIntercept()-getIntercept())/(getSlope()-edge.getSlope());
            hasIntersection = false; 
        }
        else if (Double.isInfinite(k1)) {
            xp1 = getSource().getCurrentX();
        }
        else {
            xp1 = edge.getSource().getCurrentX();
        }
        double yp1 = getSlope()*xp1+getIntercept();
        double yp12 = edge.getSlope()*xp1+edge.getIntercept();
        if (!hasIntersection) {
            hasIntersection = (Math.round(yp1/epsilon)==Math.round(yp12/epsilon));
        }
        else {
            if (Double.isInfinite(yp1) || Double.isNaN(yp1)) yp1 = yp12;
        }
        if (hasIntersection) { //check whether intersection inside edge nodes
            int minX1 = Math.min(edge.getSource().getCurrentX(), edge.getSink().getCurrentX());
            int minX2 = Math.min(getSource().getCurrentX(), getSink().getCurrentX());
            int minY1 = Math.min(edge.getSource().getCurrentY(), edge.getSink().getCurrentY());
            int minY2 = Math.min(getSource().getCurrentY(), getSink().getCurrentY());
            int maxX1 = Math.max(edge.getSource().getCurrentX(), edge.getSink().getCurrentX());
            int maxX2 = Math.max(getSource().getCurrentX(), getSink().getCurrentX());
            int maxY1 = Math.max(edge.getSource().getCurrentY(), edge.getSink().getCurrentY());
            int maxY2 = Math.max(getSource().getCurrentY(), getSink().getCurrentY());
            int rxp1=(int)Math.round(xp1), ryp1=(int)Math.round(yp1);
            if (xp1 >= minX1 && xp1 <= maxX1 && yp1 >= minY1 && yp1 <= maxY1 &&
                    xp1 >= minX2 && xp1 <= maxX2 && yp1 >= minY2 && yp1 <= maxY2 &&
                    
                    //((Math.round(xp1)!=edge.getSource().getCurrentX() || Math.round(yp1)!=edge.getSource().getCurrentY()) && (Math.round(xp1)!=edge.getSink().getCurrentX() || Math.round(yp1)!=edge.getSink().getCurrentY()) && (Math.round(xp1)!=getSource().getCurrentX() || Math.round(yp1)!=getSource().getCurrentY()) && (Math.round(xp1)!=getSink().getCurrentX() || Math.round(yp1)!=getSink().getCurrentY()))
                    (!this.hasPoint(rxp1, ryp1) || !edge.hasPoint(rxp1, ryp1))
                )
                return true;
            else
                return false;
        }
        else
            return false;
    }

    @Override
    public String toString() {
        String dir = direction > 0 ? "(+)>" : "(-)<";
        return this.source.toString()+" -"+(int)this.length+"-"+dir+" "+this.sink.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge e = (Edge)o;
            return (e.getSink().equals(getSink()) && e.getSource().equals(getSource())/* && direction > 0*/ ||
                    e.getSink().equals(getSource()) && e.getSource().equals(getSink())/* && direction < 0*/);
        }
        else
            return false;
    }

    public static int hashgridsize = (int)Math.sqrt(Integer.MAX_VALUE - 1);
    @Override
    public int hashCode() {
        //return (hashgridsize*getCurrentY()+getCurrentX());
        return /*direction*/(hashgridsize*hashgridsize*hashgridsize*getSource().getCurrentX()+
                hashgridsize*hashgridsize*getSource().getCurrentY()+
                hashgridsize*getSink().getCurrentX()+
                getSink().getCurrentY());
    }
}

