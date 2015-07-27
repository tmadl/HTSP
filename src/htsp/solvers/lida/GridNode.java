/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.solvers.lida.excitation.ExcitationStrategy;
import htsp.solvers.lida.excitation.SigmoidExcitation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author madlt
 */
public class GridNode  implements Comparable<GridNode> {
    public static boolean repassing = true; //false
    public static double repassThreshold = 0.0001; //0.0002 0.0005
    public double MIN = 0, MAX = 1;

    protected Connection[] neighbors = null;
    protected List<Connection> adjacentNeighbors = new ArrayList<Connection>();

    protected List<Object> content = new ArrayList<Object>();
    protected double activation = 0;
    //private ExcitationStrategy excitation = new SigmoidExcitation();
    protected ExcitationStrategy excitationStrategy;

    protected int number = 0;
    protected static int numberOfGridNodes = 0;
    
    protected int X = -1, Y = -1;
    
    //public static final double threshold = 0.03;

    public GridNode(int x, int y) {
        number = numberOfGridNodes;
        numberOfGridNodes++;
        X = x;
        Y = y;
        excitationStrategy = new SigmoidExcitation();
    }

    public GridNode(int x, int y, ExcitationStrategy excitation) {
        number = numberOfGridNodes;
        numberOfGridNodes++;
        X = x;
        Y = y;
        this.excitationStrategy = excitation;
    }

    public void setExcitationStrategy(ExcitationStrategy excitation) {
        this.excitationStrategy = excitation;
    }
    
    public int getX() {return X;}
    public int getY() {return Y;}
    
    public int getNumber() {
        return number;
    }
    public static void resetNumbering() {numberOfGridNodes = 0;}
    
    public boolean isEmpty() {
        return content.isEmpty();
    }

    public void setNeighbors(Connection[] neighbors) {
        this.neighbors = neighbors;
        addAdjacentNeighbors(neighbors);
    }
    public void addConnections(Connection[] connections) {
        if (this.neighbors == null) this.neighbors = connections;
        else {
            Connection[] oldNeighbors = this.neighbors;
            this.neighbors = new Connection[oldNeighbors.length+connections.length];
            //System.arraycopy(src, srcPos, dest, destPos, length);
            System.arraycopy(oldNeighbors, 0, this.neighbors, 0, oldNeighbors.length);
            System.arraycopy(connections, 0, this.neighbors, oldNeighbors.length, connections.length);
        }
        addAdjacentNeighbors(connections);
    }
    public void addAdjacentNeighbors(Connection[] neighbors) {
        List<Connection> adjacent = new ArrayList<Connection>();
        for (Connection con : neighbors) {
            if (!con.isCrosshierarchy() && con.getLength() <= 1 && con.getLength() <= 1)
                adjacent.add(con);
        }
        this.adjacentNeighbors.addAll(adjacent);
    }

    public Connection getConnection(int x, int y) {
        for (Connection n : neighbors) {
            if (n.getSink().getX() == x && n.getSink().getY() == y) return n;
        }
        return null;
    }

    public Connection[] getNeighbors() {
        return neighbors;
    }
    public Connection[] getAdjacentNeighbors() {
        Connection[] adjacent = new Connection[adjacentNeighbors.size()];
        adjacentNeighbors.toArray(adjacent);
        return adjacent;
    }

    public void setContent(Object content) {
        this.content = new ArrayList<Object>();
        this.content.add(content);
    }

    public void addContent(Object content) {
        this.content.add(content);
    }

    public void removeContent(Object content) {
        this.content.remove(content);
    }

    public void addGoal(Object goal, double goalActivation) {
        this.content.add(goal);
        passActivation(goalActivation);
    }

    public List<Object> getContent() {
        return content;
    }

    public double getActivation() {
        return activation;
    }

    public void setActivation(double a) {
        activation = a;
    }

    protected void addActivation(double a) {
        if (!repassing) {
            activation = excitationStrategy.calculateNewActivation(activation, a);
        }
        else {
            if(Math.abs(a) > repassThreshold) passActivation(a);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GridNode)
            return (((GridNode)o).getX()==X && ((GridNode)o).getY()==Y);
        else
            return false;
    }

    public static int hashgridsize = (int)Math.sqrt(Integer.MAX_VALUE);
    @Override
    public int hashCode() {
        return (hashgridsize*Y+X);
    }

    @Override
    public int compareTo(GridNode c) {
        int a = (int)((activation-c.getActivation())*10000);
        return a;
    }

    @Override
    public String toString() {
        return "GridNode"+number+"(x="+getX()+",y="+getY()+",a="+getActivation()+")";
    }

    /*
no re-passing - better performance
params used
params DEFAULTCONTENT = 1, MAXADJACENTNEIGHBORS = 8, SPREADRADIUS = 4, DEFAULTWEIGHT = 0.6; /*0.6^4 = 0.13* /, DEFAULTGOALACTIVATION = 0.3
    */
    public void passActivation(double a) {
        activation = excitationStrategy.calculateNewActivation(activation, a);
        if ((!repassing || Math.abs(a) > repassThreshold) && neighbors != null) {
            for (Connection con : neighbors) {
                if (con!=null && con.getSink()!=null) {
                    //con.getSink().addActivation(a * con.getWeight());
                    con.passActivation(a);
                }
            }
        }
    }

    /*
    with activation re-passing - worse performance, seems to be functionally equivalent
params used
params DEFAULTCONTENT = 1, MAXADJACENTNEIGHBORS = 8, SPREADRADIUS = 4, DEFAULTWEIGHT = 0.5, DEFAULTGOALACTIVATION = 0.3
threshold = 0.03

    protected void passActivation(double a, PlaceNode source) {
        activation = excitation.calculateNewActivation(activation, a);
        for (Connection con : neighbors) {
            if (con.getSink().getActivation() < 1 && a * con.getWeight() > threshold && con.getSink() != source) {
                con.getSink().passActivation(a * con.getWeight(), this);
            }
        }
    }*/
}
