/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

/**
 *
 * @author madlt
 */
public class Connection implements Comparable<Connection> {
    protected double weight = 0, threshold = -Double.MAX_VALUE, length = 0;
    protected GridNode source = null, sink = null;
    protected String label = "DEFAULTCONNECTION";
    protected boolean crosshierarchy = false;

    public boolean isCrosshierarchy() {
        return crosshierarchy;
    }

    public void setCrosshierarchy(boolean crosshierarchy) {
        this.crosshierarchy = crosshierarchy;
    }

    public Connection() {}

    public Connection(double weight, double length, GridNode source, GridNode sink, boolean crosshierarchy) {
        init(weight, length, source, sink, crosshierarchy);
    }

    public Connection(double weight, double length, GridNode source, GridNode sink) {
        //System.out.println("New Connection ("+weight+") from "+source+" to "+sink);
        init(weight, length, source, sink, false);
    }

    private void init(double weight, double length, GridNode source, GridNode sink, boolean crosshierarchy) {
        this.weight = weight;
        this.source = source;
        this.sink = sink;
        this.length = length;
        this.crosshierarchy = crosshierarchy;
    }

    public void setLabel(String l) {
        label = l;
    }

    public String getLabel() {return label;}

    public GridNode getSink() {
        return sink;
    }

    public void passActivation(double activation) {
        double a;
        if (activation >= threshold) {
            a = weight * activation;
            getSink().addActivation(a);
        }
    }

    public void setThreshold(double t) {
        threshold = t;
    }

    public void setSink(GridNode sink) {
        this.sink = sink;
    }

    public GridNode getSource() {
        return source;
    }

    public void setSource(GridNode source) {
        this.source = source;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getLength() {
        //return Math.sqrt(Math.pow(getSink().getX()-getSource().getX(), 2)+Math.pow(getSink().getY()-getSource().getY(), 2));
        return length;
    }

    @Override
    public int compareTo(Connection c) {
        return (int)((this.getLength() - c.getLength())*10);
    }

    @Override
    public String toString() {
        return "Connection "+label+" ("+weight+") from "+source+" to "+sink;
    }
}
