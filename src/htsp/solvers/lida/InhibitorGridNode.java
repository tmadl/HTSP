/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package htsp.solvers.lida;

import htsp.solvers.lida.excitation.ExcitationStrategy;

/**
 *
 * @author Tom
 */
public class InhibitorGridNode extends GridNode {
    public InhibitorGridNode(int x, int y) {
        super(x, y);
    }

    public InhibitorGridNode(int x, int y, ExcitationStrategy excitation) {
        super(x, y, excitation);
    }

    /*
     * global inhibitor node can receive normal (positive) activation and pass it on negatively
     * received negative activation can only come through re-passing -> ignore
     */
    @Override
    public void passActivation(double a) {
        if (a > 0) {
            activation = excitationStrategy.calculateNewActivation(activation, a);
            if ((!repassing || a > repassThreshold) && neighbors != null) {
                for (Connection con : neighbors) {
                    if (con!=null && con.getSink()!=null && con.getSink() != this) {
                        //con.getSink().addActivation(a * con.getWeight());
                        con.passActivation(-a);
                    }
                }
            }
        }
    }
}
