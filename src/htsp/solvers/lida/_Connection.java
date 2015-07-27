/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

/**
 *
 * @author madlt
 */
public class _Connection extends Connection {

    public _Connection() {}

    public _Connection(double weight, GridNode source, GridNode sink, boolean crosshierarchy) {
        init(weight, source, sink, crosshierarchy);
    }

    public _Connection(double weight, GridNode source, GridNode sink) {
        //System.out.println("New Connection ("+weight+") from "+source+" to "+sink);
        init(weight, source, sink, false);
    }

    private void init(double weight, GridNode source, GridNode sink, boolean crosshierarchy) {
        this.weight = weight;
        this.source = source;
        this.sink = sink;
        this.crosshierarchy = crosshierarchy;
        this.length = getLength();
    }

    @Override
    public double getLength() {
       return Math.sqrt(Math.pow(getSink().getX()-getSource().getX(), 2)+Math.pow(getSink().getY()-getSource().getY(), 2));
    }
}
